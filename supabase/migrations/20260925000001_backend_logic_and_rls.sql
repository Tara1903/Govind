-- PHASE 6, 11, 12, 20, 21: Backend Logic and Strict RLS

-- PHASE 6: Create Storage buckets
INSERT INTO storage.buckets (id, name, public) 
VALUES 
  ('products', 'products', true),
  ('categories', 'categories', true),
  ('promotions', 'promotions', true),
  ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

-- Drop existing storage policies if any to prevent conflict
DO $$
BEGIN
    DROP POLICY IF EXISTS "Public Access" ON storage.objects;
    DROP POLICY IF EXISTS "Admin Upload Access" ON storage.objects;
    DROP POLICY IF EXISTS "Admin Update Access" ON storage.objects;
    DROP POLICY IF EXISTS "Admin Delete Access" ON storage.objects;
    DROP POLICY IF EXISTS "User Avatar Upload" ON storage.objects;
END
$$;

-- Policies for Storage
-- Allow public read access to all objects in these buckets
CREATE POLICY "Public Access" ON storage.objects FOR SELECT USING ( bucket_id IN ('products', 'categories', 'promotions', 'avatars') );

-- Only authenticated users with role 'admin' can insert/update/delete objects
CREATE POLICY "Admin Upload Access" ON storage.objects FOR INSERT TO authenticated WITH CHECK (
  bucket_id IN ('products', 'categories', 'promotions') AND 
  (EXISTS (SELECT 1 FROM public.profiles WHERE profiles.id = auth.uid() AND profiles.role = 'admin'))
);

CREATE POLICY "Admin Update Access" ON storage.objects FOR UPDATE TO authenticated USING (
  bucket_id IN ('products', 'categories', 'promotions') AND 
  (EXISTS (SELECT 1 FROM public.profiles WHERE profiles.id = auth.uid() AND profiles.role = 'admin'))
);

CREATE POLICY "Admin Delete Access" ON storage.objects FOR DELETE TO authenticated USING (
  bucket_id IN ('products', 'categories', 'promotions') AND 
  (EXISTS (SELECT 1 FROM public.profiles WHERE profiles.id = auth.uid() AND profiles.role = 'admin'))
);

-- Users can upload their own avatars
CREATE POLICY "User Avatar Upload" ON storage.objects FOR INSERT TO authenticated WITH CHECK (
  bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]
);

-- PHASE 11-12: Transactional Order Creation and Inventory Decrement

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
    p_items JSONB -- [{product_id, product_name, unit, price, quantity, discount}]
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
        total, savings, address_snapshot, payment_method, payment_status, order_status
    ) VALUES (
        p_customer_id, p_subtotal, p_discount, p_coupon_id, p_delivery_charge,
        p_total, p_savings, p_address_snapshot, p_payment_method, 
        CASE WHEN p_payment_method = 'COD' THEN 'PENDING' ELSE 'PENDING' END,
        'PENDING'
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

        -- Insert order item
        INSERT INTO public.order_items (
            order_id, product_id, product_name, unit, price, quantity, discount
        ) VALUES (
            v_order_id, 
            v_product_id, 
            v_item->>'product_name', 
            v_item->>'unit', 
            (v_item->>'price')::DECIMAL, 
            v_quantity, 
            (v_item->>'discount')::DECIMAL
        );
    END LOOP;

    -- 3. Clear User's Cart
    DELETE FROM public.cart_items 
    WHERE cart_id = (SELECT id FROM public.carts WHERE profile_id = p_customer_id);

    RETURN v_order_id;
END;
$$;


-- PHASE 20-21: Audit and strict RLS

-- Helper function to check if user is admin safely without triggering RLS recursion
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.profiles
    WHERE id = auth.uid() AND role = 'admin'
  );
$$;

-- Drop all existing policies on public tables to apply strict ones
DO $$
DECLARE
    t_name text;
    p_name text;
BEGIN
    FOR t_name IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        FOR p_name IN (SELECT policyname FROM pg_policies WHERE schemaname = 'public' AND tablename = t_name) LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.%I', p_name, t_name);
        END LOOP;
    END LOOP;
END
$$;

-- Apply Strict RLS

-- Profiles
CREATE POLICY "Profiles read" ON public.profiles FOR SELECT USING (auth.uid() = id OR public.is_admin());
CREATE POLICY "Profiles update" ON public.profiles FOR UPDATE USING (auth.uid() = id OR public.is_admin());
CREATE POLICY "Profiles insert" ON public.profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Profiles delete" ON public.profiles FOR DELETE USING (public.is_admin());

-- Addresses
CREATE POLICY "Addresses read" ON public.addresses FOR SELECT USING (auth.uid() = profile_id OR public.is_admin());
CREATE POLICY "Addresses insert" ON public.addresses FOR INSERT WITH CHECK (auth.uid() = profile_id);
CREATE POLICY "Addresses update" ON public.addresses FOR UPDATE USING (auth.uid() = profile_id OR public.is_admin());
CREATE POLICY "Addresses delete" ON public.addresses FOR DELETE USING (auth.uid() = profile_id OR public.is_admin());

-- Categories
CREATE POLICY "Categories read" ON public.categories FOR SELECT USING (active = TRUE OR public.is_admin());
CREATE POLICY "Categories insert" ON public.categories FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Categories update" ON public.categories FOR UPDATE USING (public.is_admin());
CREATE POLICY "Categories delete" ON public.categories FOR DELETE USING (public.is_admin());

-- Products
CREATE POLICY "Products read" ON public.products FOR SELECT USING (active = TRUE OR public.is_admin());
CREATE POLICY "Products insert" ON public.products FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Products update" ON public.products FOR UPDATE USING (public.is_admin());
CREATE POLICY "Products delete" ON public.products FOR DELETE USING (public.is_admin());

-- Product Images
CREATE POLICY "Product Images read" ON public.product_images FOR SELECT USING (TRUE);
CREATE POLICY "Product Images insert" ON public.product_images FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Product Images update" ON public.product_images FOR UPDATE USING (public.is_admin());
CREATE POLICY "Product Images delete" ON public.product_images FOR DELETE USING (public.is_admin());

-- Favorites
CREATE POLICY "Favorites read" ON public.favorites FOR SELECT USING (auth.uid() = profile_id);
CREATE POLICY "Favorites insert" ON public.favorites FOR INSERT WITH CHECK (auth.uid() = profile_id);
CREATE POLICY "Favorites delete" ON public.favorites FOR DELETE USING (auth.uid() = profile_id);

-- Carts
CREATE POLICY "Carts read" ON public.carts FOR SELECT USING (auth.uid() = profile_id);
CREATE POLICY "Carts insert" ON public.carts FOR INSERT WITH CHECK (auth.uid() = profile_id);
CREATE POLICY "Carts update" ON public.carts FOR UPDATE USING (auth.uid() = profile_id);
CREATE POLICY "Carts delete" ON public.carts FOR DELETE USING (auth.uid() = profile_id);

-- Cart Items
CREATE POLICY "Cart Items read" ON public.cart_items FOR SELECT USING (EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid()));
CREATE POLICY "Cart Items insert" ON public.cart_items FOR INSERT WITH CHECK (EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid()));
CREATE POLICY "Cart Items update" ON public.cart_items FOR UPDATE USING (EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid()));
CREATE POLICY "Cart Items delete" ON public.cart_items FOR DELETE USING (EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid()));

-- Coupons
CREATE POLICY "Coupons read" ON public.coupons FOR SELECT USING (active = TRUE OR public.is_admin());
CREATE POLICY "Coupons insert" ON public.coupons FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Coupons update" ON public.coupons FOR UPDATE USING (public.is_admin());
CREATE POLICY "Coupons delete" ON public.coupons FOR DELETE USING (public.is_admin());

-- Orders
CREATE POLICY "Orders read" ON public.orders FOR SELECT USING (auth.uid() = customer_id OR public.is_admin());
CREATE POLICY "Orders insert" ON public.orders FOR INSERT WITH CHECK (auth.uid() = customer_id);
CREATE POLICY "Orders update" ON public.orders FOR UPDATE USING (public.is_admin());
CREATE POLICY "Orders delete" ON public.orders FOR DELETE USING (public.is_admin());

-- Order Items
CREATE POLICY "Order Items read" ON public.order_items FOR SELECT USING (EXISTS (SELECT 1 FROM public.orders WHERE orders.id = order_items.order_id AND orders.customer_id = auth.uid()) OR public.is_admin());
CREATE POLICY "Order Items insert" ON public.order_items FOR INSERT WITH CHECK (EXISTS (SELECT 1 FROM public.orders WHERE orders.id = order_items.order_id AND orders.customer_id = auth.uid()));
CREATE POLICY "Order Items update" ON public.order_items FOR UPDATE USING (public.is_admin());
CREATE POLICY "Order Items delete" ON public.order_items FOR DELETE USING (public.is_admin());

-- Promotions
CREATE POLICY "Promotions read" ON public.promotions FOR SELECT USING (active = TRUE AND start_date <= NOW() AND end_date >= NOW() OR public.is_admin());
CREATE POLICY "Promotions insert" ON public.promotions FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Promotions update" ON public.promotions FOR UPDATE USING (public.is_admin());
CREATE POLICY "Promotions delete" ON public.promotions FOR DELETE USING (public.is_admin());

-- Delivery Settings
CREATE POLICY "Delivery Settings read" ON public.delivery_settings FOR SELECT USING (active = TRUE OR public.is_admin());
CREATE POLICY "Delivery Settings insert" ON public.delivery_settings FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Delivery Settings update" ON public.delivery_settings FOR UPDATE USING (public.is_admin());
CREATE POLICY "Delivery Settings delete" ON public.delivery_settings FOR DELETE USING (public.is_admin());

-- Business Settings
CREATE POLICY "Business Settings read" ON public.business_settings FOR SELECT USING (TRUE);
CREATE POLICY "Business Settings insert" ON public.business_settings FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Business Settings update" ON public.business_settings FOR UPDATE USING (public.is_admin());
CREATE POLICY "Business Settings delete" ON public.business_settings FOR DELETE USING (public.is_admin());
