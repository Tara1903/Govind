-- Migration: Unified Global Cart

-- 1. Orders: allow MIXED experience type
ALTER TABLE public.orders 
DROP CONSTRAINT IF EXISTS orders_experience_type_check;

ALTER TABLE public.orders
ADD CONSTRAINT orders_experience_type_check CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE', 'MIXED'));

ALTER TABLE public.orders
ALTER COLUMN experience_type SET DEFAULT 'MIXED';

-- 2. Cart Items
ALTER TABLE public.cart_items 
ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH' CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE'));

-- Ensure uniqueness by cart_id, product_id, experience_type
-- First, let's remove duplicates if any exist
DELETE FROM public.cart_items
WHERE id IN (
    SELECT id
    FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY cart_id, product_id, experience_type ORDER BY created_at DESC) as rn
        FROM public.cart_items
    ) t
    WHERE t.rn > 1
);

-- Drop legacy unique constraint on (cart_id, product_id) and ensure idempotency
ALTER TABLE public.cart_items DROP CONSTRAINT IF EXISTS cart_items_cart_id_product_id_key;
ALTER TABLE public.cart_items DROP CONSTRAINT IF EXISTS cart_items_unique_product_exp;
ALTER TABLE public.cart_items ADD CONSTRAINT cart_items_unique_product_exp UNIQUE (cart_id, product_id, experience_type);

-- 3. Order Items
ALTER TABLE public.order_items
ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH' CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE')),
ADD COLUMN IF NOT EXISTS base_price DECIMAL(10, 2),
ADD COLUMN IF NOT EXISTS bulk_discount DECIMAL(10, 2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS promotion_discount DECIMAL(10, 2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS coupon_discount DECIMAL(10, 2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS effective_unit_price DECIMAL(10, 2),
ADD COLUMN IF NOT EXISTS line_total DECIMAL(10, 2);

-- 4. Update RPC create_order_and_decrement_stock
CREATE OR REPLACE FUNCTION public.create_order_and_decrement_stock(
    p_customer_id UUID,
    p_subtotal DECIMAL,
    p_discount DECIMAL,
    p_coupon_id UUID,
    p_delivery_charge DECIMAL,
    p_total DECIMAL,
    p_savings DECIMAL,
    p_address_snapshot JSONB,
    p_payment_method TEXT,
    p_items JSONB, -- [{product_id, product_name, unit, price, quantity, discount, experience_type, base_price, bulk_discount, promotion_discount, coupon_discount, effective_unit_price, line_total}]
    p_experience_type TEXT DEFAULT 'MIXED'
) RETURNS UUID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_order_id UUID;
    v_item JSONB;
    v_product_id UUID;
    v_quantity INTEGER;
    v_stock INTEGER;
BEGIN
    -- 1. Create Order
    INSERT INTO public.orders (
        customer_id, subtotal, discount, coupon_id, delivery_charge, 
        total, savings, address_snapshot, payment_method, payment_status, order_status, experience_type
    ) VALUES (
        p_customer_id, p_subtotal, p_discount, p_coupon_id, p_delivery_charge,
        p_total, p_savings, p_address_snapshot, p_payment_method, 
        CASE WHEN p_payment_method = 'COD' THEN 'PENDING' ELSE 'PENDING' END,
        'PLACED',
        p_experience_type
    ) RETURNING id INTO v_order_id;

    -- 2. Process Items and Decrement Stock
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items)
    LOOP
        v_product_id := (v_item->>'product_id')::UUID;
        v_quantity := (v_item->>'quantity')::INTEGER;

        -- Check stock with lock
        SELECT stock_quantity INTO v_stock
        FROM public.products
        WHERE id = v_product_id
        FOR UPDATE;

        IF v_stock < v_quantity THEN
            RAISE EXCEPTION 'Insufficient stock for product %', v_item->>'product_name';
        END IF;

        -- Decrement stock
        UPDATE public.products
        SET stock_quantity = stock_quantity - v_quantity
        WHERE id = v_product_id;

        -- Insert Order Item
        INSERT INTO public.order_items (
            order_id, product_id, product_name, unit, price, quantity, discount,
            experience_type, base_price, bulk_discount, promotion_discount, coupon_discount, effective_unit_price, line_total
        ) VALUES (
            v_order_id, 
            v_product_id, 
            v_item->>'product_name', 
            v_item->>'unit', 
            (v_item->>'price')::DECIMAL, 
            v_quantity, 
            COALESCE((v_item->>'discount')::DECIMAL, 0),
            COALESCE(v_item->>'experience_type', 'FRESH'),
            COALESCE((v_item->>'base_price')::DECIMAL, (v_item->>'price')::DECIMAL),
            COALESCE((v_item->>'bulk_discount')::DECIMAL, 0),
            COALESCE((v_item->>'promotion_discount')::DECIMAL, 0),
            COALESCE((v_item->>'coupon_discount')::DECIMAL, 0),
            COALESCE((v_item->>'effective_unit_price')::DECIMAL, (v_item->>'price')::DECIMAL),
            COALESCE((v_item->>'line_total')::DECIMAL, (v_item->>'price')::DECIMAL * v_quantity)
        );
    END LOOP;

    -- 3. Clear User's Cart Items across all experiences
    DELETE FROM public.cart_items
    WHERE cart_id IN (SELECT id FROM public.carts WHERE profile_id = p_customer_id);

    RETURN v_order_id;
END;
$$;

