import { NextResponse } from 'next/server'
import { createClient } from '@supabase/supabase-js'

export async function POST(req: Request) {
  try {
    const body = await req.json()
    const { 
      user_id, // For direct user notification
      template_name,
      custom_title,
      custom_body,
      custom_deep_link,
      notification_type = 'MARKETING',
      payload = {},
      campaign_id,
    } = body

    if (!user_id && !campaign_id) {
      return NextResponse.json({ error: 'user_id or campaign_id required' }, { status: 400 })
    }
    if (!template_name && (!custom_title || !custom_body)) {
      return NextResponse.json({ error: 'template_name or custom_title+body required' }, { status: 400 })
    }

    const supabase = createClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL!,
      process.env.SUPABASE_SERVICE_ROLE_KEY!
    )

    // Insert into notification_outbox
    const dedup_key = `manual:${user_id || campaign_id}:${Date.now()}`
    const { data, error } = await supabase
      .from('notification_outbox')
      .insert({
        dedup_key,
        user_id: user_id || null,
        campaign_id: campaign_id || null,
        template_name: template_name || null,
        notification_type,
        payload: {
          ...payload,
          custom_title,
          custom_body,
          custom_deep_link,
        },
        process_after: new Date().toISOString(),
      })
      .select()
      .single()

    if (error) throw error

    // Trigger the dispatcher
    const functionsUrl = process.env.SUPABASE_FUNCTIONS_URL || `${process.env.NEXT_PUBLIC_SUPABASE_URL}/functions/v1`
    const dispatchResult = await fetch(`${functionsUrl}/send-notification`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${process.env.SUPABASE_SERVICE_ROLE_KEY}`,
        'Content-Type': 'application/json',
      },
    })
    const dispatchData = await dispatchResult.json()

    return NextResponse.json({ success: true, outbox_id: data?.id, dispatch: dispatchData })
  } catch (error: any) {
    console.error('Notification send error:', error)
    return NextResponse.json({ error: error.message }, { status: 500 })
  }
}
