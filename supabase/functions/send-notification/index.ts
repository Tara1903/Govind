import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const FIREBASE_PROJECT_ID = 'govind-826d0'

// Get OAuth2 access token for Firebase HTTP v1 API
async function getFirebaseAccessToken(serviceAccountJson: string): Promise<string> {
  const serviceAccount = JSON.parse(serviceAccountJson)
  
  const header = { alg: 'RS256', typ: 'JWT' }
  const now = Math.floor(Date.now() / 1000)
  const payload = {
    iss: serviceAccount.client_email,
    sub: serviceAccount.client_email,
    aud: 'https://oauth2.googleapis.com/token',
    iat: now,
    exp: now + 3600,
    scope: 'https://www.googleapis.com/auth/firebase.messaging',
  }

  // Create JWT
  const encoder = new TextEncoder()
  const headerB64 = btoa(JSON.stringify(header)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  const payloadB64 = btoa(JSON.stringify(payload)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  const signingInput = `${headerB64}.${payloadB64}`

  // Import private key
  const privateKeyPem = serviceAccount.private_key
  const pemContents = privateKeyPem.replace(/-----BEGIN PRIVATE KEY-----/, '').replace(/-----END PRIVATE KEY-----/, '').replace(/\n/g, '')
  const keyData = Uint8Array.from(atob(pemContents), c => c.charCodeAt(0))
  
  const cryptoKey = await crypto.subtle.importKey(
    'pkcs8',
    keyData,
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false,
    ['sign']
  )
  
  const signature = await crypto.subtle.sign(
    'RSASSA-PKCS1-v1_5',
    cryptoKey,
    encoder.encode(signingInput)
  )
  
  const signatureB64 = btoa(String.fromCharCode(...new Uint8Array(signature))).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  const jwt = `${signingInput}.${signatureB64}`

  // Exchange JWT for access token
  const tokenResponse = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=${jwt}`,
  })

  const tokenData = await tokenResponse.json()
  if (!tokenData.access_token) {
    throw new Error(`Failed to get access token: ${JSON.stringify(tokenData)}`)
  }
  return tokenData.access_token
}

// Substitute template variables
function substituteTemplate(template: string, variables: Record<string, string>): string {
  return template.replace(/\{\{(\w+)\}\}/g, (_, key) => variables[key] || '')
}

serve(async (req: Request) => {
  try {
    // Verify this is an authorized call (from Supabase cron or admin)
    const authHeader = req.headers.get('Authorization')
    const cronSecret = Deno.env.get('CRON_SECRET') || ''
    const serviceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') || ''
    
    const isAuthorized = 
      authHeader === `Bearer ${cronSecret}` ||
      authHeader === `Bearer ${serviceKey}`

    if (!isAuthorized) {
      return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401 })
    }

    const serviceAccountJson = Deno.env.get('FCM_SERVICE_ACCOUNT_JSON')
    if (!serviceAccountJson || serviceAccountJson.includes('YOUR_FIREBASE')) {
      return new Response(JSON.stringify({ 
        error: 'FCM_SERVICE_ACCOUNT_JSON not configured',
        status: 'FIREBASE_CONFIGURATION_REQUIRED'
      }), { status: 503 })
    }

    const supabase = createClient(
      Deno.env.get('SUPABASE_URL')!,
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!
    )

    // Get Firebase access token
    const accessToken = await getFirebaseAccessToken(serviceAccountJson)

    // Fetch QUEUED outbox items ready for processing
    const { data: outboxItems, error: outboxError } = await supabase
      .from('notification_outbox')
      .select('*')
      .eq('status', 'QUEUED')
      .lte('process_after', new Date().toISOString())
      .lt('attempt_count', supabase.rpc ? 3 : 3) // max_attempts check
      .order('created_at', { ascending: true })
      .limit(50)

    if (outboxError) throw outboxError
    if (!outboxItems || outboxItems.length === 0) {
      return new Response(JSON.stringify({ processed: 0, message: 'No items in queue' }), { status: 200 })
    }

    let totalSent = 0
    let totalFailed = 0
    let totalSkipped = 0

    for (const item of outboxItems) {
      // Mark as SENDING to prevent double-processing
      await supabase
        .from('notification_outbox')
        .update({ status: 'SENDING', attempt_count: item.attempt_count + 1 })
        .eq('id', item.id)
        .eq('status', 'QUEUED') // optimistic lock

      try {
        if (!item.user_id && !item.campaign_id) {
          await supabase.from('notification_outbox').update({ status: 'SKIPPED' }).eq('id', item.id)
          totalSkipped++
          continue
        }

        // Get template
        const { data: template } = await supabase
          .from('notification_templates')
          .select('*')
          .eq('name', item.template_name)
          .eq('is_active', true)
          .single()

        if (!template) {
          await supabase.from('notification_outbox')
            .update({ status: 'FAILED', error_message: 'Template not found: ' + item.template_name, failed_at: new Date().toISOString() })
            .eq('id', item.id)
          totalFailed++
          continue
        }

        // Prepare template variables
        const payload = item.payload || {}
        const templateVars: Record<string, string> = {
          order_id: payload.order_id || '',
          order_number: payload.order_number || '',
          order_status: payload.order_status || '',
          experience_type: payload.experience_type || 'FRESH',
          product_name: payload.product_name || '',
          eta: payload.eta || '',
          customer_name: payload.customer_name || '',
          delivery_area: payload.delivery_area || '',
        }

        const title = substituteTemplate(template.title_template, templateVars)
        const body = substituteTemplate(template.body_template, templateVars)
        const deepLink = template.deep_link_template ? substituteTemplate(template.deep_link_template, templateVars) : null

        // Check user notification preferences
        const { data: prefs } = await supabase
          .from('notification_preferences')
          .select('*')
          .eq('user_id', item.user_id)
          .single()

        // Check if preference allows this notification type
        const notifType = item.notification_type
        let isAllowed = true
        if (prefs) {
          if (notifType === 'MARKETING' && !prefs.marketing) isAllowed = false
          if (notifType === 'CART' && !prefs.cart_reminders) isAllowed = false
          if (notifType === 'OFFER' && !prefs.offers) isAllowed = false
          if (notifType === 'FAVORITE' && !prefs.favorite_alerts) isAllowed = false
          if (notifType === 'WHOLESALE' && !prefs.wholesale_updates) isAllowed = false
          // Never suppress ORDER or DELIVERY transactional
        }

        if (!isAllowed) {
          await supabase.from('notification_outbox').update({ status: 'SUPPRESSED' }).eq('id', item.id)
          totalSkipped++
          continue
        }

        // Quiet hours check for marketing only
        if (notifType === 'MARKETING' && prefs?.quiet_hours_enabled) {
          const now = new Date()
          const kolkataHour = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Kolkata' })).getHours()
          const startHour = parseInt(prefs.quiet_hours_start?.split(':')[0] || '22')
          const endHour = parseInt(prefs.quiet_hours_end?.split(':')[0] || '7')
          
          let inQuiet = false
          if (startHour > endHour) {
            inQuiet = kolkataHour >= startHour || kolkataHour < endHour
          } else {
            inQuiet = kolkataHour >= startHour && kolkataHour < endHour
          }

          if (inQuiet) {
            // Reschedule for after quiet hours end
            const reschedule = new Date()
            reschedule.setHours(endHour, 0, 0, 0)
            if (reschedule <= now) reschedule.setDate(reschedule.getDate() + 1)
            await supabase.from('notification_outbox')
              .update({ status: 'QUEUED', process_after: reschedule.toISOString(), attempt_count: item.attempt_count })
              .eq('id', item.id)
            totalSkipped++
            continue
          }
        }

        // Create notification history record
        const { data: notifRecord } = await supabase
          .from('notifications')
          .insert({
            user_id: item.user_id,
            type: notifType,
            title,
            body,
            deep_link: deepLink,
            order_id: payload.order_id || null,
            product_id: payload.product_id || null,
            campaign_id: item.campaign_id || null,
            metadata: payload,
          })
          .select('id')
          .single()

        // Get user's active devices
        const { data: devices } = await supabase
          .from('notification_devices')
          .select('id, fcm_token')
          .eq('user_id', item.user_id)
          .eq('enabled', true)
          .limit(10)

        if (!devices || devices.length === 0) {
          await supabase.from('notification_outbox')
            .update({ status: 'SKIPPED', error_message: 'No active devices', sent_at: new Date().toISOString() })
            .eq('id', item.id)
          totalSkipped++
          continue
        }

        let itemSent = 0
        let itemFailed = 0

        for (const device of devices) {
          const fcmPayload = {
            message: {
              token: device.fcm_token,
              notification: { title, body },
              data: {
                notification_id: notifRecord?.id || '',
                notification_type: notifType,
                deep_link: deepLink || '',
                order_id: payload.order_id || '',
                channel: template.channel,
              },
              android: {
                notification: {
                  channel_id: template.channel,
                  priority: (notifType === 'ORDER' || notifType === 'DELIVERY') ? 'HIGH' : 'DEFAULT',
                  color: '#064520',
                },
                priority: (notifType === 'ORDER' || notifType === 'DELIVERY') ? 'high' : 'normal',
              },
            },
          }

          const fcmResponse = await fetch(
            `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
            {
              method: 'POST',
              headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
              },
              body: JSON.stringify(fcmPayload),
            }
          )

          const fcmResult = await fcmResponse.json()
          const fcmMessageId = fcmResult.name || null
          const fcmError = fcmResult.error

          let deliveryStatus = 'SENT'
          let errorCode: string | null = null

          if (!fcmResponse.ok || fcmError) {
            const errorReason = fcmError?.details?.[0]?.errorCode || fcmError?.status || 'UNKNOWN'
            errorCode = errorReason
            
            if (errorReason === 'UNREGISTERED' || errorReason === 'INVALID_ARGUMENT') {
              deliveryStatus = 'INVALID_TOKEN'
              // Disable stale device
              await supabase.from('notification_devices')
                .update({ enabled: false, updated_at: new Date().toISOString() })
                .eq('id', device.id)
            } else {
              deliveryStatus = 'FAILED'
            }
            itemFailed++
          } else {
            itemSent++
          }

          // Record delivery attempt
          await supabase.from('notification_deliveries').insert({
            notification_id: notifRecord?.id,
            outbox_id: item.id,
            device_id: device.id,
            fcm_message_id: fcmMessageId,
            status: deliveryStatus,
            error_code: errorCode,
            attempt_count: 1,
            sent_at: deliveryStatus === 'SENT' ? new Date().toISOString() : null,
            failed_at: deliveryStatus !== 'SENT' ? new Date().toISOString() : null,
          })
        }

        const finalStatus = itemSent > 0 ? 'SENT' : 'FAILED'
        await supabase.from('notification_outbox').update({
          status: finalStatus,
          sent_at: new Date().toISOString(),
        }).eq('id', item.id)

        if (itemSent > 0) totalSent++
        else totalFailed++

      } catch (err: any) {
        console.error('Error processing outbox item:', item.id, err)
        await supabase.from('notification_outbox').update({
          status: item.attempt_count >= item.max_attempts - 1 ? 'FAILED' : 'QUEUED',
          error_message: err.message,
          failed_at: new Date().toISOString(),
        }).eq('id', item.id)
        totalFailed++
      }
    }

    return new Response(JSON.stringify({
      processed: outboxItems.length,
      sent: totalSent,
      failed: totalFailed,
      skipped: totalSkipped,
    }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })

  } catch (err: any) {
    console.error('send-notification fatal error:', err)
    return new Response(JSON.stringify({ error: err.message }), { status: 500 })
  }
})
