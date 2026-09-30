import { NextResponse } from 'next/server';
import { createClient } from '@supabase/supabase-js';
import { calculateProductPrice } from '@/lib/pricingEngine';

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { 
      customer_id, 
      cart_items, 
      coupon_id, 
      delivery_charge = 0, 
      address_snapshot, 
      payment_method,
      experienceType = 'FRESH'
    } = body;

    if (!customer_id || !cart_items || cart_items.length === 0) {
      return NextResponse.json({ error: 'Invalid payload' }, { status: 400 });
    }

    const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL!;
    const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY!;
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Fetch product details
    const productIds = cart_items.map((item: any) => item.product_id);
    const { data: products, error: productsError } = await supabase
      .from('products')
      .select('*')
      .in('id', productIds);

    if (productsError || !products) {
      throw new Error('Failed to fetch products');
    }

    let subtotal = 0;
    let savings = 0;
    let totalDiscount = 0; // if we want to separate coupon vs bulk
    const finalItems: any[] = [];

    for (const item of cart_items) {
      const product = products.find(p => p.id === item.product_id);
      if (!product) {
        throw new Error('Product not found: ' + item.product_id);
      }

      const itemExperienceType = item.experienceType || experienceType;

      const pricing = calculateProductPrice(
        product.selling_price,
        item.quantity,
        product.bundle_items?.wholesale_pricing,
        itemExperienceType
      );

      subtotal += pricing.subtotal;
      savings += pricing.totalDiscount;

      finalItems.push({
        product_id: product.id,
        product_name: product.name,
        unit: product.unit,
        price: pricing.effectiveUnitPrice,
        quantity: item.quantity,
        discount: pricing.discountAmountPerUnit,
        experience_type: itemExperienceType,
        base_price: product.selling_price,
        bulk_discount: pricing.discountAmountPerUnit,
        effective_unit_price: pricing.effectiveUnitPrice,
        line_total: pricing.subtotal
      });
    }

    const total = subtotal + Number(delivery_charge);

    // Call the RPC
    const { data: orderId, error: rpcError } = await supabase.rpc('create_order_and_decrement_stock', {
      p_customer_id: customer_id,
      p_subtotal: subtotal,
      p_discount: 0, // Additional coupon discounts could be applied here
      p_coupon_id: coupon_id,
      p_delivery_charge: delivery_charge,
      p_total: total,
      p_savings: savings,
      p_address_snapshot: address_snapshot,
      p_payment_method: payment_method,
      p_experience_type: 'MIXED',
      p_items: finalItems
    });

    if (rpcError) {
      throw new Error(rpcError.message);
    }

    return NextResponse.json({ success: true, order_id: orderId });
  } catch (error: any) {
    console.error('Checkout error:', error);
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}

