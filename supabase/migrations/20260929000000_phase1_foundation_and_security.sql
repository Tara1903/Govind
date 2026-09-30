-- ============================================================================
-- GOVIND — PHASE 1: DATABASE FOUNDATION, SCHEMA SYNC & SECURITY RECOVERY
-- Migration: 20260929000000_phase1_foundation_and_security.sql
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. CANONICAL ROLE HELPER FUNCTIONS (SECURITY DEFINER, NO RLS RECURSION)
-- ============================================================================

CREATE OR REPLACE FUNCTION public.get_my_role()
RETURNS TEXT
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT role FROM public.profiles WHERE id = auth.uid();
$$;

CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.profiles
    WHERE id = auth.uid() AND role = 'admin'
  );
$$;

CREATE OR REPLACE FUNCTION public.is_delivery_or_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.profiles
    WHERE id = auth.uid() AND role IN ('delivery', 'admin')
  );
$$;

-- ============================================================================
-- 2. PROFILES SCHEMA, CANONICAL ROLE STANDARDIZATION & BACKFILL
-- ============================================================================

ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS name TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS full_name TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS phone TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS email TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS role TEXT DEFAULT 'customer';
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Drop any existing role check constraints on profiles before normalizing
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE nsp.nspname = 'public'
          AND rel.relname = 'profiles'
          AND con.contype = 'c'
          AND pg_get_constraintdef(con.oid) ILIKE '%role%'
    ) LOOP
        EXECUTE format('ALTER TABLE public.profiles DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;

-- Normalize existing profile roles to canonical lowercase values: 'customer', 'admin', 'delivery'
UPDATE public.profiles
SET role = CASE
    WHEN LOWER(TRIM(COALESCE(role, ''))) = 'admin' THEN 'admin'
    WHEN LOWER(TRIM(COALESCE(role, ''))) IN ('delivery', 'delivery_partner') THEN 'delivery'
    ELSE 'customer'
END;

-- Ensure name and full_name are populated on existing profiles
UPDATE public.profiles
SET
    name = COALESCE(NULLIF(TRIM(name), ''), NULLIF(TRIM(full_name), ''), SPLIT_PART(COALESCE(email, ''), '@', 1), 'Govind Customer'),
    full_name = COALESCE(NULLIF(TRIM(full_name), ''), NULLIF(TRIM(name), ''), SPLIT_PART(COALESCE(email, ''), '@', 1), 'Govind Customer')
WHERE name IS NULL OR TRIM(name) = '' OR full_name IS NULL OR TRIM(full_name) = '';

ALTER TABLE public.profiles
    ALTER COLUMN role SET DEFAULT 'customer',
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE public.profiles
    ADD CONSTRAINT profiles_role_check CHECK (role IN ('customer', 'admin', 'delivery'));

-- Trigger function: auto-create public.profiles row on auth.users insert
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_name TEXT;
    v_phone TEXT;
BEGIN
    v_name := COALESCE(
        NULLIF(TRIM(NEW.raw_user_meta_data->>'full_name'), ''),
        NULLIF(TRIM(NEW.raw_user_meta_data->>'name'), ''),
        NULLIF(SPLIT_PART(COALESCE(NEW.email, ''), '@', 1), ''),
        'Govind Customer'
    );
    v_phone := COALESCE(
        NULLIF(TRIM(NEW.phone), ''),
        NULLIF(TRIM(NEW.raw_user_meta_data->>'phone'), '')
    );

    INSERT INTO public.profiles (
        id, email, phone, name, full_name, role, created_at, updated_at
    ) VALUES (
        NEW.id,
        NEW.email,
        v_phone,
        v_name,
        v_name,
        'customer',
         COALESCE(NEW.created_at, NOW()),
        NOW()
    )
    ON CONFLICT (id) DO UPDATE SET
        email = COALESCE(EXCLUDED.email, public.profiles.email),
        phone = COALESCE(public.profiles.phone, EXCLUDED.phone),
        name = COALESCE(NULLIF(TRIM(public.profiles.name), ''), EXCLUDED.name),
        full_name = COALESCE(NULLIF(TRIM(public.profiles.full_name), ''), EXCLUDED.full_name),
        updated_at = NOW();

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- Idempotent backfill of existing auth.users into public.profiles
INSERT INTO public.profiles (id, email, phone, name, full_name, role, created_at, updated_at)
SELECT
    u.id,
    u.email,
    COALESCE(NULLIF(TRIM(u.phone), ''), NULLIF(TRIM(u.raw_user_meta_data->>'phone'), '')),
    COALESCE(
        NULLIF(TRIM(u.raw_user_meta_data->>'full_name'), ''),
        NULLIF(TRIM(u.raw_user_meta_data->>'name'), ''),
        NULLIF(SPLIT_PART(COALESCE(u.email, ''), '@', 1), ''),
        'Govind Customer'
    ),
    COALESCE(
        NULLIF(TRIM(u.raw_user_meta_data->>'full_name'), ''),
        NULLIF(TRIM(u.raw_user_meta_data->>'name'), ''),
        NULLIF(SPLIT_PART(COALESCE(u.email, ''), '@', 1), ''),
        'Govind Customer'
    ),
    'customer',
    COALESCE(u.created_at, NOW()),
    NOW()
FROM auth.users u
ON CONFLICT (id) DO UPDATE SET
    email = COALESCE(public.profiles.email, EXCLUDED.email),
    name = COALESCE(NULLIF(TRIM(public.profiles.name), ''), EXCLUDED.name),
    full_name = COALESCE(NULLIF(TRIM(public.profiles.full_name), ''), EXCLUDED.full_name);

-- ============================================================================
-- 3. PROFILE RLS & ROLE ESCALATION PREVENTION
-- ============================================================================

CREATE OR REPLACE FUNCTION public.prevent_profile_role_escalation()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NEW.role IS DISTINCT FROM OLD.role THEN
        -- If called with an authenticated user JWT, only admins may change role
        IF auth.uid() IS NOT NULL AND NOT public.is_admin() THEN
            RAISE EXCEPTION 'Unauthorized: cannot change profile role';
        END IF;
        -- Block anon/authenticated JWTs if auth.uid() is somehow null
        IF auth.uid() IS NULL AND COALESCE(current_setting('request.jwt.claim.role', true), '') IN ('anon', 'authenticated') THEN
            RAISE EXCEPTION 'Unauthorized: cannot change profile role';
        END IF;
    END IF;
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS prevent_profile_role_escalation_trigger ON public.profiles;
CREATE TRIGGER prevent_profile_role_escalation_trigger
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.prevent_profile_role_escalation();

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view their own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can insert their own profile" ON public.profiles;
DROP POLICY IF EXISTS "Profiles read" ON public.profiles;
DROP POLICY IF EXISTS "Profiles update" ON public.profiles;
DROP POLICY IF EXISTS "Profiles insert" ON public.profiles;
DROP POLICY IF EXISTS "Profiles delete" ON public.profiles;
DROP POLICY IF EXISTS "Profiles update own" ON public.profiles;
DROP POLICY IF EXISTS "Profiles admin update" ON public.profiles;

CREATE POLICY "Profiles read" ON public.profiles
    FOR SELECT USING (auth.uid() = id OR public.is_admin());

CREATE POLICY "Profiles insert" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id AND role = 'customer');

CREATE POLICY "Profiles update own" ON public.profiles
    FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id AND role = public.get_my_role());

CREATE POLICY "Profiles admin update" ON public.profiles
    FOR UPDATE
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "Profiles delete" ON public.profiles
    FOR DELETE USING (public.is_admin());

-- ============================================================================
-- 4. ENABLE RLS & STRICT POLICIES ON COUPONS AND DELIVERY_SETTINGS
-- ============================================================================

ALTER TABLE public.coupons ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.delivery_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.business_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.promotions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Coupons read" ON public.coupons;
DROP POLICY IF EXISTS "Coupons insert" ON public.coupons;
DROP POLICY IF EXISTS "Coupons update" ON public.coupons;
DROP POLICY IF EXISTS "Coupons delete" ON public.coupons;
DROP POLICY IF EXISTS "Public can view active coupons" ON public.coupons;
DROP POLICY IF EXISTS "Admins have full access to coupons" ON public.coupons;

CREATE POLICY "Coupons read" ON public.coupons
    FOR SELECT USING (active = TRUE OR public.is_admin());

CREATE POLICY "Coupons insert" ON public.coupons
    FOR INSERT WITH CHECK (public.is_admin());

CREATE POLICY "Coupons update" ON public.coupons
    FOR UPDATE USING (public.is_admin()) WITH CHECK (public.is_admin());

CREATE POLICY "Coupons delete" ON public.coupons
    FOR DELETE USING (public.is_admin());

DROP POLICY IF EXISTS "Delivery Settings read" ON public.delivery_settings;
DROP POLICY IF EXISTS "Delivery Settings insert" ON public.delivery_settings;
DROP POLICY IF EXISTS "Delivery Settings update" ON public.delivery_settings;
DROP POLICY IF EXISTS "Delivery Settings delete" ON public.delivery_settings;
DROP POLICY IF EXISTS "Public can view delivery settings" ON public.delivery_settings;
DROP POLICY IF EXISTS "Admins can update delivery settings" ON public.delivery_settings;

CREATE POLICY "Delivery Settings read" ON public.delivery_settings
    FOR SELECT USING (active = TRUE OR public.is_admin());

CREATE POLICY "Delivery Settings insert" ON public.delivery_settings
    FOR INSERT WITH CHECK (public.is_admin());

CREATE POLICY "Delivery Settings update" ON public.delivery_settings
    FOR UPDATE USING (public.is_admin()) WITH CHECK (public.is_admin());

CREATE POLICY "Delivery Settings delete" ON public.delivery_settings
    FOR DELETE USING (public.is_admin());

-- ============================================================================
-- 5. GLOBAL CART & MULTI-EXPERIENCE CART_ITEMS UNIQUENESS
-- ============================================================================

ALTER TABLE public.carts
    ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH';

ALTER TABLE public.carts DROP CONSTRAINT IF EXISTS carts_profile_id_key;
ALTER TABLE public.carts DROP CONSTRAINT IF EXISTS carts_profile_experience_unique;
ALTER TABLE public.carts
    ADD CONSTRAINT carts_profile_experience_unique UNIQUE (profile_id, experience_type);

ALTER TABLE public.cart_items
    ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH';

ALTER TABLE public.cart_items DROP CONSTRAINT IF EXISTS cart_items_experience_type_check;
ALTER TABLE public.cart_items
    ADD CONSTRAINT cart_items_experience_type_check CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE'));

-- Deduplicate any conflicting rows on (cart_id, product_id, experience_type)
DELETE FROM public.cart_items
WHERE id IN (
    SELECT id
    FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY cart_id, product_id, experience_type ORDER BY created_at DESC) AS rn
        FROM public.cart_items
    ) t
    WHERE t.rn > 1
);

-- Drop legacy (cart_id, product_id) unique constraint that blocked multi-experience items
ALTER TABLE public.cart_items DROP CONSTRAINT IF EXISTS cart_items_cart_id_product_id_key;
ALTER TABLE public.cart_items DROP CONSTRAINT IF EXISTS cart_items_unique_product_exp;
ALTER TABLE public.cart_items
    ADD CONSTRAINT cart_items_unique_product_exp UNIQUE (cart_id, product_id, experience_type);

ALTER TABLE public.carts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cart_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Carts read" ON public.carts;
DROP POLICY IF EXISTS "Carts insert" ON public.carts;
DROP POLICY IF EXISTS "Carts update" ON public.carts;
DROP POLICY IF EXISTS "Carts delete" ON public.carts;

CREATE POLICY "Carts read" ON public.carts
    FOR SELECT USING (auth.uid() = profile_id OR public.is_admin());
CREATE POLICY "Carts insert" ON public.carts
    FOR INSERT WITH CHECK (auth.uid() = profile_id);
CREATE POLICY "Carts update" ON public.carts
    FOR UPDATE USING (auth.uid() = profile_id) WITH CHECK (auth.uid() = profile_id);
CREATE POLICY "Carts delete" ON public.carts
    FOR DELETE USING (auth.uid() = profile_id OR public.is_admin());

DROP POLICY IF EXISTS "Cart Items read" ON public.cart_items;
DROP POLICY IF EXISTS "Cart Items insert" ON public.cart_items;
DROP POLICY IF EXISTS "Cart Items update" ON public.cart_items;
DROP POLICY IF EXISTS "Cart Items delete" ON public.cart_items;

CREATE POLICY "Cart Items read" ON public.cart_items
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid())
        OR public.is_admin()
    );
CREATE POLICY "Cart Items insert" ON public.cart_items
    FOR INSERT WITH CHECK (
        EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid())
    );
CREATE POLICY "Cart Items update" ON public.cart_items
    FOR UPDATE USING (
        EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid())
    ) WITH CHECK (
        EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid())
    );
CREATE POLICY "Cart Items delete" ON public.cart_items
    FOR DELETE USING (
        EXISTS (SELECT 1 FROM public.carts WHERE carts.id = cart_items.cart_id AND carts.profile_id = auth.uid())
        OR public.is_admin()
    );

-- ============================================================================
-- 6. ORDERS, ORDER_ITEMS & ADDRESSES SCHEMA SYNCHRONIZATION
-- ============================================================================

ALTER TABLE public.addresses ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 8);
ALTER TABLE public.addresses ADD COLUMN IF NOT EXISTS longitude DECIMAL(10, 8);

ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS delivery_partner_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS dest_latitude DECIMAL(10, 8);
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS dest_longitude DECIMAL(10, 8);
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS address_id UUID REFERENCES public.addresses(id) ON DELETE SET NULL;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'MIXED';

-- Add compatibility FK constraint name orders_user_id_fkey if missing so PostgREST joins on orders_user_id_fkey succeed
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'orders_user_id_fkey'
    ) THEN
        ALTER TABLE public.orders
            ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (customer_id) REFERENCES public.profiles(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Normalize any non-canonical order_status values before applying canonical constraint
UPDATE public.orders
SET order_status = CASE
    WHEN order_status = 'READY' THEN 'READY_FOR_DELIVERY'
    WHEN order_status = 'INITIATED' THEN 'PLACED'
    WHEN order_status IN ('PLACED', 'PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') THEN order_status
    ELSE 'PLACED'
END;

ALTER TABLE public.orders DROP CONSTRAINT IF EXISTS orders_order_status_check;
ALTER TABLE public.orders
    ALTER COLUMN order_status SET DEFAULT 'PLACED',
    ADD CONSTRAINT orders_order_status_check
        CHECK (order_status IN ('PLACED', 'PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'));

ALTER TABLE public.orders DROP CONSTRAINT IF EXISTS orders_experience_type_check;
ALTER TABLE public.orders
    ALTER COLUMN experience_type SET DEFAULT 'MIXED',
    ADD CONSTRAINT orders_experience_type_check
        CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE', 'MIXED'));

-- Ensure order_items pricing snapshot columns exist
ALTER TABLE public.order_items
    ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH',
    ADD COLUMN IF NOT EXISTS base_price DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS bulk_discount DECIMAL(10, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS promotion_discount DECIMAL(10, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS coupon_discount DECIMAL(10, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS effective_unit_price DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS line_total DECIMAL(10, 2);

ALTER TABLE public.order_items DROP CONSTRAINT IF EXISTS order_items_experience_type_check;
ALTER TABLE public.order_items
    ADD CONSTRAINT order_items_experience_type_check
        CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE'));

-- ============================================================================
-- 7. ORDERS & ORDER_ITEMS RLS AND DELIVERY PARTNER UPDATE GUARD
-- ============================================================================

CREATE OR REPLACE FUNCTION public.enforce_order_update_permissions()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_role TEXT;
BEGIN
    IF auth.uid() IS NOT NULL AND NOT public.is_admin() THEN
        SELECT role INTO v_role FROM public.profiles WHERE id = auth.uid();
        IF v_role = 'delivery' AND OLD.delivery_partner_id = auth.uid() THEN
            -- Delivery partner can only update order_status (to OUT_FOR_DELIVERY or DELIVERED) and updated_at
            IF NEW.customer_id IS DISTINCT FROM OLD.customer_id
               OR NEW.total IS DISTINCT FROM OLD.total
               OR NEW.subtotal IS DISTINCT FROM OLD.subtotal
               OR NEW.delivery_partner_id IS DISTINCT FROM OLD.delivery_partner_id
               OR NEW.order_status NOT IN ('READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
                RAISE EXCEPTION 'Unauthorized: delivery partners may only update delivery status on assigned orders';
            END IF;
        ELSE
            RAISE EXCEPTION 'Unauthorized: only admins or assigned delivery partners can update orders';
        END IF;
    END IF;
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS enforce_order_update_permissions_trigger ON public.orders;
CREATE TRIGGER enforce_order_update_permissions_trigger
    BEFORE UPDATE ON public.orders
    FOR EACH ROW
    EXECUTE FUNCTION public.enforce_order_update_permissions();

ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Orders read" ON public.orders;
DROP POLICY IF EXISTS "Orders insert" ON public.orders;
DROP POLICY IF EXISTS "Orders update" ON public.orders;
DROP POLICY IF EXISTS "Orders delete" ON public.orders;
DROP POLICY IF EXISTS "Orders delivery update" ON public.orders;

CREATE POLICY "Orders read" ON public.orders
    FOR SELECT USING (
        auth.uid() = customer_id
        OR (auth.uid() = delivery_partner_id AND public.is_delivery_or_admin())
        OR public.is_admin()
    );

CREATE POLICY "Orders insert" ON public.orders
    FOR INSERT WITH CHECK (auth.uid() = customer_id OR public.is_admin());

CREATE POLICY "Orders update" ON public.orders
    FOR UPDATE USING (
        public.is_admin()
        OR (auth.uid() = delivery_partner_id AND public.is_delivery_or_admin())
    )
    WITH CHECK (
        public.is_admin()
        OR (auth.uid() = delivery_partner_id AND public.is_delivery_or_admin())
    );

CREATE POLICY "Orders delete" ON public.orders
    FOR DELETE USING (public.is_admin());

DROP POLICY IF EXISTS "Order Items read" ON public.order_items;
DROP POLICY IF EXISTS "Order Items insert" ON public.order_items;
DROP POLICY IF EXISTS "Order Items update" ON public.order_items;
DROP POLICY IF EXISTS "Order Items delete" ON public.order_items;

CREATE POLICY "Order Items read" ON public.order_items
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = order_items.order_id
              AND (orders.customer_id = auth.uid() OR orders.delivery_partner_id = auth.uid())
        )
        OR public.is_admin()
    );

CREATE POLICY "Order Items insert" ON public.order_items
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = order_items.order_id AND orders.customer_id = auth.uid()
        )
        OR public.is_admin()
    );

CREATE POLICY "Order Items update" ON public.order_items
    FOR UPDATE USING (public.is_admin()) WITH CHECK (public.is_admin());

CREATE POLICY "Order Items delete" ON public.order_items
    FOR DELETE USING (public.is_admin());

-- ============================================================================
-- 8. AUTHORITATIVE CHECKOUT / ORDER CREATION RPC
-- ============================================================================

-- Drop older function signatures to avoid PostgREST overload ambiguity
DROP FUNCTION IF EXISTS public.create_order_and_decrement_stock(UUID, DECIMAL, DECIMAL, UUID, DECIMAL, DECIMAL, DECIMAL, JSONB, TEXT, JSONB);
DROP FUNCTION IF EXISTS public.create_order_and_decrement_stock(UUID, DECIMAL, DECIMAL, UUID, DECIMAL, DECIMAL, DECIMAL, JSONB, TEXT, JSONB, TEXT);

CREATE OR REPLACE FUNCTION public.create_order_and_decrement_stock(
    p_customer_id UUID,
    p_subtotal DECIMAL,
    p_discount DECIMAL DEFAULT 0,
    p_coupon_id UUID DEFAULT NULL,
    p_delivery_charge DECIMAL DEFAULT 0,
    p_total DECIMAL DEFAULT 0,
    p_payment_method TEXT DEFAULT 'COD',
    p_items JSONB DEFAULT '[]'::jsonb,
    p_savings DECIMAL DEFAULT 0,
    p_address_snapshot JSONB DEFAULT NULL,
    p_experience_type TEXT DEFAULT 'MIXED',
    p_tax DECIMAL DEFAULT 0,
    p_address_id UUID DEFAULT NULL
) RETURNS UUID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_order_id UUID;
    v_item JSONB;
    v_product_id UUID;
    v_quantity INTEGER;
    v_stock INTEGER;
    v_db_name TEXT;
    v_db_unit TEXT;
    v_db_mrp DECIMAL(10, 2);
    v_db_selling DECIMAL(10, 2);
    v_item_price DECIMAL(10, 2);
    v_base_price DECIMAL(10, 2);
    v_bulk_discount DECIMAL(10, 2);
    v_promo_discount DECIMAL(10, 2);
    v_coupon_discount DECIMAL(10, 2);
    v_effective_price DECIMAL(10, 2);
    v_line_total DECIMAL(10, 2);
    v_item_exp TEXT;
    v_resolved_address JSONB;
    v_dest_lat DECIMAL(10, 8);
    v_dest_lng DECIMAL(10, 8);
    v_order_exp TEXT;
BEGIN
    -- 1. Validate caller identity / customer ownership
    IF p_customer_id IS NULL THEN
        RAISE EXCEPTION 'customer_id is required';
    END IF;

    IF auth.uid() IS NOT NULL THEN
        IF auth.uid() <> p_customer_id AND NOT public.is_admin() THEN
            RAISE EXCEPTION 'Unauthorized: customer_id must match authenticated user';
        END IF;
    ELSIF COALESCE(current_setting('request.jwt.claim.role', true), '') = 'anon' THEN
        RAISE EXCEPTION 'Authentication required to place an order';
    END IF;

    -- 2. Validate items array is non-empty
    IF p_items IS NULL OR jsonb_typeof(p_items) <> 'array' OR jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'Order must contain at least one item';
    END IF;

    -- 3. Resolve address_snapshot and destination coordinates
    v_resolved_address := p_address_snapshot;
    IF v_resolved_address IS NULL AND p_address_id IS NOT NULL THEN
        SELECT to_jsonb(a), a.latitude, a.longitude
        INTO v_resolved_address, v_dest_lat, v_dest_lng
        FROM public.addresses a
        WHERE a.id = p_address_id AND (a.profile_id = p_customer_id OR public.is_admin());
    END IF;

    IF v_resolved_address IS NULL THEN
        v_resolved_address := jsonb_build_object('address_id', p_address_id);
    END IF;

    IF v_dest_lat IS NULL AND (v_resolved_address ? 'latitude') AND (v_resolved_address->>'latitude') IS NOT NULL THEN
        v_dest_lat := NULLIF(v_resolved_address->>'latitude', '')::DECIMAL(10, 8);
        v_dest_lng := NULLIF(v_resolved_address->>'longitude', '')::DECIMAL(10, 8);
    END IF;

    -- 4. Normalize order experience_type
    v_order_exp := UPPER(COALESCE(NULLIF(TRIM(p_experience_type), ''), 'MIXED'));
    IF v_order_exp NOT IN ('FRESH', 'KITCHEN', 'WHOLESALE', 'MIXED') THEN
        v_order_exp := 'MIXED';
    END IF;

    -- 5. Create Order with canonical status 'PLACED'
    INSERT INTO public.orders (
        customer_id,
        subtotal,
        discount,
        coupon_id,
        delivery_charge,
        total,
        savings,
        address_snapshot,
        address_id,
        dest_latitude,
        dest_longitude,
        payment_method,
        payment_status,
        order_status,
        experience_type
    ) VALUES (
        p_customer_id,
        COALESCE(p_subtotal, 0),
        COALESCE(p_discount, 0),
        p_coupon_id,
        COALESCE(p_delivery_charge, 0),
        COALESCE(p_total, 0),
        COALESCE(p_savings, 0),
        v_resolved_address,
        p_address_id,
        v_dest_lat,
        v_dest_lng,
        COALESCE(NULLIF(TRIM(p_payment_method), ''), 'COD'),
        'PENDING',
        'PLACED',
        v_order_exp
    ) RETURNING id INTO v_order_id;

    -- 6. Process Items, Lock & Validate Stock, Decrement Stock, Snapshot Prices
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items)
    LOOP
        v_product_id := (v_item->>'product_id')::UUID;
        v_quantity := COALESCE((v_item->>'quantity')::INTEGER, 0);

        IF v_product_id IS NULL OR v_quantity <= 0 THEN
            RAISE EXCEPTION 'Invalid product_id or quantity in order items';
        END IF;

        SELECT stock_quantity, name, unit, price, selling_price
        INTO v_stock, v_db_name, v_db_unit, v_db_mrp, v_db_selling
        FROM public.products
        WHERE id = v_product_id
        FOR UPDATE;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Product not found: %', v_product_id;
        END IF;

        IF COALESCE(v_stock, 0) < v_quantity THEN
            RAISE EXCEPTION 'Insufficient stock for product % (available: %, requested: %)',
                COALESCE(v_item->>'product_name', v_db_name), COALESCE(v_stock, 0), v_quantity;
        END IF;

        UPDATE public.products
        SET stock_quantity = stock_quantity - v_quantity,
            updated_at = NOW()
        WHERE id = v_product_id;

        v_item_exp := UPPER(COALESCE(NULLIF(TRIM(v_item->>'experience_type'), ''), 'FRESH'));
        IF v_item_exp NOT IN ('FRESH', 'KITCHEN', 'WHOLESALE') THEN
            v_item_exp := 'FRESH';
        END IF;

        v_item_price := COALESCE((v_item->>'price')::DECIMAL(10, 2), v_db_selling, v_db_mrp, 0);
        v_base_price := COALESCE((v_item->>'base_price')::DECIMAL(10, 2), v_db_selling, v_item_price);
        v_bulk_discount := COALESCE((v_item->>'bulk_discount')::DECIMAL(10, 2), 0);
        v_promo_discount := COALESCE((v_item->>'promotion_discount')::DECIMAL(10, 2), 0);
        v_coupon_discount := COALESCE((v_item->>'coupon_discount')::DECIMAL(10, 2), 0);
        v_effective_price := COALESCE((v_item->>'effective_unit_price')::DECIMAL(10, 2), v_item_price);
        v_line_total := COALESCE((v_item->>'line_total')::DECIMAL(10, 2), ROUND(v_effective_price * v_quantity, 2));

        INSERT INTO public.order_items (
            order_id,
            product_id,
            product_name,
            unit,
            price,
            quantity,
            discount,
            experience_type,
            base_price,
            bulk_discount,
            promotion_discount,
            coupon_discount,
            effective_unit_price,
            line_total
        ) VALUES (
            v_order_id,
            v_product_id,
            COALESCE(NULLIF(TRIM(v_item->>'product_name'), ''), v_db_name),
            COALESCE(NULLIF(TRIM(v_item->>'unit'), ''), v_db_unit, 'unit'),
            v_item_price,
            v_quantity,
            COALESCE((v_item->>'discount')::DECIMAL(10, 2), 0),
            v_item_exp,
            v_base_price,
            v_bulk_discount,
            v_promo_discount,
            v_coupon_discount,
            v_effective_price,
            v_line_total
        );
    END LOOP;

    -- 7. Clear customer's server-side cart items across all experiences
    DELETE FROM public.cart_items
    WHERE cart_id IN (
        SELECT id FROM public.carts WHERE profile_id = p_customer_id
    );

    RETURN v_order_id;
END;
$$;

-- ============================================================================
-- 9. DELIVERY TRACKING TABLE, RLS & RPC (PHASE 1.12)
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.delivery_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE UNIQUE,
    delivery_partner_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(10, 8) NOT NULL,
    accuracy DECIMAL(10, 2),
    speed DECIMAL(10, 2),
    heading DECIMAL(10, 2),
    estimated_distance_m INTEGER NOT NULL DEFAULT 0,
    estimated_time_sec INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.delivery_locations ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE public.delivery_locations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE public.delivery_locations ALTER COLUMN estimated_distance_m SET DEFAULT 0;
ALTER TABLE public.delivery_locations ALTER COLUMN estimated_time_sec SET DEFAULT 0;

ALTER TABLE public.delivery_locations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Delivery partner can manage their own location" ON public.delivery_locations;
CREATE POLICY "Delivery partner can manage their own location" ON public.delivery_locations
    FOR ALL
    USING (auth.uid() = delivery_partner_id AND public.is_delivery_or_admin())
    WITH CHECK (auth.uid() = delivery_partner_id AND public.is_delivery_or_admin());

DROP POLICY IF EXISTS "Customers can view their order tracking" ON public.delivery_locations;
CREATE POLICY "Customers can view their order tracking" ON public.delivery_locations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = delivery_locations.order_id AND orders.customer_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Admins can view all tracking" ON public.delivery_locations;
CREATE POLICY "Admins can view all tracking" ON public.delivery_locations
    FOR ALL USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE OR REPLACE FUNCTION public.update_delivery_location(
    p_order_id UUID,
    p_lat DECIMAL,
    p_lng DECIMAL,
    p_accuracy DECIMAL DEFAULT NULL,
    p_speed DECIMAL DEFAULT NULL,
    p_heading DECIMAL DEFAULT NULL
) RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_partner_id UUID;
    v_dest_lat DECIMAL;
    v_dest_lng DECIMAL;
    v_distance_m INTEGER := 1500;
    v_eta_sec INTEGER := 900;
    v_speed_ms DECIMAL;
    v_caller_role TEXT;
BEGIN
    SELECT delivery_partner_id, dest_latitude, dest_longitude
    INTO v_partner_id, v_dest_lat, v_dest_lng
    FROM public.orders
    WHERE id = p_order_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Order not found: %', p_order_id;
    END IF;

    IF auth.uid() IS NOT NULL THEN
        SELECT role INTO v_caller_role FROM public.profiles WHERE id = auth.uid();
        IF v_caller_role IS DISTINCT FROM 'admin' AND (v_partner_id IS DISTINCT FROM auth.uid() OR v_caller_role IS DISTINCT FROM 'delivery') THEN
            RAISE EXCEPTION 'Unauthorized: only the assigned delivery partner or an admin can update delivery location';
        END IF;
    ELSIF COALESCE(current_setting('request.jwt.claim.role', true), '') = 'anon' THEN
        RAISE EXCEPTION 'Authentication required';
    END IF;

    IF v_dest_lat IS NOT NULL AND v_dest_lng IS NOT NULL THEN
        v_distance_m := ROUND(
            6371000 * acos(
                LEAST(1.0, GREATEST(-1.0,
                    cos(radians(v_dest_lat)) * cos(radians(p_lat)) * cos(radians(p_lng) - radians(v_dest_lng)) +
                    sin(radians(v_dest_lat)) * sin(radians(p_lat))
                ))
            )
        )::INTEGER;
        v_speed_ms := GREATEST(COALESCE(p_speed, 0), 7.0);
        v_eta_sec := GREATEST(60, ROUND(v_distance_m / v_speed_ms)::INTEGER);
    END IF;

    INSERT INTO public.delivery_locations (
        order_id,
        delivery_partner_id,
        latitude,
        longitude,
        accuracy,
        speed,
        heading,
        estimated_distance_m,
        estimated_time_sec,
        created_at,
        updated_at
    ) VALUES (
        p_order_id,
        COALESCE(v_partner_id, auth.uid()),
        p_lat,
        p_lng,
        p_accuracy,
        p_speed,
        p_heading,
        v_distance_m,
        v_eta_sec,
        NOW(),
        NOW()
    ) ON CONFLICT (order_id) DO UPDATE SET
        delivery_partner_id = COALESCE(EXCLUDED.delivery_partner_id, public.delivery_locations.delivery_partner_id),
        latitude = EXCLUDED.latitude,
        longitude = EXCLUDED.longitude,
        accuracy = EXCLUDED.accuracy,
        speed = EXCLUDED.speed,
        heading = EXCLUDED.heading,
        estimated_distance_m = EXCLUDED.estimated_distance_m,
        estimated_time_sec = EXCLUDED.estimated_time_sec,
        updated_at = NOW();
END;
$$;

-- ============================================================================
-- 10. ORDER STATUS HISTORY TABLE, TRIGGER & REALTIME (PHASE 1.13)
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    status TEXT NOT NULL,
    updated_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    changed_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    changed_at TIMESTAMPTZ DEFAULT NOW(),
    note TEXT
);

ALTER TABLE public.order_status_history ADD COLUMN IF NOT EXISTS updated_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL;
ALTER TABLE public.order_status_history ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE public.order_status_history ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE public.order_status_history ADD COLUMN IF NOT EXISTS changed_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL;
ALTER TABLE public.order_status_history ADD COLUMN IF NOT EXISTS changed_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE public.order_status_history ADD COLUMN IF NOT EXISTS note TEXT;

ALTER TABLE public.order_status_history DROP CONSTRAINT IF EXISTS order_status_history_status_check;
ALTER TABLE public.order_status_history
    ADD CONSTRAINT order_status_history_status_check
        CHECK (status IN ('PLACED', 'PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'));

ALTER TABLE public.order_status_history ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Customers view own order history" ON public.order_status_history;
CREATE POLICY "Customers view own order history" ON public.order_status_history
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = order_status_history.order_id AND orders.customer_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Admins manage all order history" ON public.order_status_history;
CREATE POLICY "Admins manage all order history" ON public.order_status_history
    FOR ALL USING (public.is_admin())
    WITH CHECK (public.is_admin());

DROP POLICY IF EXISTS "Delivery partner can view and insert order history" ON public.order_status_history;
CREATE POLICY "Delivery partner can view and insert order history" ON public.order_status_history
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = order_status_history.order_id AND orders.delivery_partner_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Delivery partner can insert assigned order history" ON public.order_status_history;
CREATE POLICY "Delivery partner can insert assigned order history" ON public.order_status_history
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.orders o
            JOIN public.profiles p ON p.id = auth.uid()
            WHERE o.id = order_status_history.order_id
              AND o.delivery_partner_id = auth.uid()
              AND p.role = 'delivery'
        )
    );

CREATE OR REPLACE FUNCTION public.log_order_status_change()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_actor UUID;
    v_note TEXT;
BEGIN
    IF auth.uid() IS NOT NULL AND EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid()) THEN
        v_actor := auth.uid();
    ELSE
        v_actor := NULL;
    END IF;

    IF (TG_OP = 'INSERT') OR (TG_OP = 'UPDATE' AND OLD.order_status IS DISTINCT FROM NEW.order_status) THEN
        v_note := CASE WHEN TG_OP = 'INSERT' THEN 'Order created' ELSE 'Status updated to ' || NEW.order_status END;
        INSERT INTO public.order_status_history (
            order_id, status, updated_by, notes, created_at, changed_by, changed_at, note
        ) VALUES (
            NEW.id, NEW.order_status, v_actor, v_note, NOW(), v_actor, NOW(), v_note
        );
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_order_status_change ON public.orders;
CREATE TRIGGER on_order_status_change
    AFTER INSERT OR UPDATE ON public.orders
    FOR EACH ROW
    EXECUTE FUNCTION public.log_order_status_change();

-- Backfill initial status history for any existing orders that have no history rows
INSERT INTO public.order_status_history (order_id, status, notes, created_at, changed_at, note)
SELECT o.id, o.order_status, 'Initial status backfill', COALESCE(o.created_at, NOW()), COALESCE(o.created_at, NOW()), 'Initial status backfill'
FROM public.orders o
WHERE NOT EXISTS (
    SELECT 1 FROM public.order_status_history h WHERE h.order_id = o.id
);

-- Enable Realtime on orders, delivery_locations, and order_status_history safely
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY['orders', 'delivery_locations', 'order_status_history'] LOOP
        IF NOT EXISTS (
            SELECT 1 FROM pg_publication_tables
            WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = t
        ) THEN
            EXECUTE format('ALTER PUBLICATION supabase_realtime ADD TABLE public.%I', t);
        END IF;
    END LOOP;
END $$;

-- ============================================================================
-- 11. REMOVE TEMPORARY DEBUG FUNCTION test_sync() (PHASE 1.16)
-- ============================================================================

DROP FUNCTION IF EXISTS public.test_sync();
