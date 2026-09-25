import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

// Note: Requires crypto module to verify razorpay signature

serve(async (req) => {
  try {
    const signature = req.headers.get('x-razorpay-signature')
    const bodyText = await req.text()
    
    // In real implementation: verify signature using RAZORPAY_WEBHOOK_SECRET
    // const expectedSignature = crypto.createHmac('sha256', secret).update(bodyText).digest('hex')

    const event = JSON.parse(bodyText)

    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    if (event.event === 'payment.captured') {
      const paymentId = event.payload.payment.entity.id
      const orderId = event.payload.payment.entity.order_id
      
      // Update order status in DB
      await supabaseClient
        .from('orders')
        .update({ payment_status: 'SUCCESS', order_status: 'CONFIRMED', razorpay_payment_id: paymentId })
        .eq('razorpay_order_id', orderId)
    }

    return new Response(JSON.stringify({ received: true }), {
      headers: { 'Content-Type': 'application/json' },
      status: 200,
    })
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { 'Content-Type': 'application/json' },
      status: 400,
    })
  }
})
