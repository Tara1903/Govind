import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

serve(async (req) => {
  try {
    const signature = req.headers.get('x-razorpay-signature');
    const bodyText = await req.text();
    const secret = Deno.env.get('RAZORPAY_WEBHOOK_SECRET');

    if (!signature || !secret) {
      throw new Error('Missing signature or secret');
    }

    // Verify HMAC SHA256 signature using Web Crypto API
    const encoder = new TextEncoder();
    const keyForSign = await crypto.subtle.importKey(
      'raw',
      encoder.encode(secret),
      { name: 'HMAC', hash: 'SHA-256' },
      false,
      ['sign']
    );
    
    const signatureBuffer = await crypto.subtle.sign(
      'HMAC',
      keyForSign,
      encoder.encode(bodyText)
    );
    
    const expectedSignature = Array.from(new Uint8Array(signatureBuffer))
      .map(b => b.toString(16).padStart(2, '0'))
      .join('');

    if (expectedSignature !== signature) {
      throw new Error('Invalid signature');
    }

    const event = JSON.parse(bodyText)

    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    if (event.event === 'payment.captured' || event.event === 'order.paid') {
      const paymentEntity = event.payload.payment.entity;
      const paymentId = paymentEntity.id;
      const orderId = paymentEntity.order_id;
      
      // Update order status in DB
      const { error } = await supabaseClient
        .from('orders')
        .update({ 
          payment_status: 'SUCCESS', 
          order_status: 'CONFIRMED', 
          razorpay_payment_id: paymentId 
        })
        .eq('razorpay_order_id', orderId);
        
      if (error) {
        throw new Error(error.message);
      }
    } else if (event.event === 'payment.failed') {
      const paymentEntity = event.payload.payment.entity;
      const orderId = paymentEntity.order_id;
      
      await supabaseClient
        .from('orders')
        .update({ payment_status: 'FAILED' })
        .eq('razorpay_order_id', orderId);
    }

    return new Response(JSON.stringify({ received: true }), {
      headers: { 'Content-Type': 'application/json' },
      status: 200,
    })
  } catch (error: any) {
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { 'Content-Type': 'application/json' },
      status: 400,
    })
  }
})
