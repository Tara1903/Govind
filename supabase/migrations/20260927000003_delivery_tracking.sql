-- Migration 20260927000003_delivery_tracking.sql

-- Ensure canonical role column on profiles
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS phone TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS role TEXT NOT NULL DEFAULT 'customer' CHECK (role IN ('customer', 'admin', 'delivery'));

-- Add delivery info to orders
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS delivery_partner_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS dest_latitude DECIMAL(10, 8);
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS dest_longitude DECIMAL(10, 8);

-- Add lat/lng to addresses
ALTER TABLE public.addresses ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 8);
ALTER TABLE public.addresses ADD COLUMN IF NOT EXISTS longitude DECIMAL(10, 8);

-- Create delivery_locations table for live tracking
CREATE TABLE IF NOT EXISTS public.delivery_locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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

ALTER TABLE public.delivery_locations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Delivery partner can manage their own location" ON public.delivery_locations;
CREATE POLICY "Delivery partner can manage their own location" ON public.delivery_locations
    FOR ALL USING (auth.uid() = delivery_partner_id)
    WITH CHECK (auth.uid() = delivery_partner_id);

DROP POLICY IF EXISTS "Customers can view their order tracking" ON public.delivery_locations;
CREATE POLICY "Customers can view their order tracking" ON public.delivery_locations
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.orders WHERE id = delivery_locations.order_id AND customer_id = auth.uid())
    );

DROP POLICY IF EXISTS "Admins can view all tracking" ON public.delivery_locations;
CREATE POLICY "Admins can view all tracking" ON public.delivery_locations
    FOR ALL USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- Enable Realtime for delivery_locations safely
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'delivery_locations'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.delivery_locations;
    END IF;
END $$;

-- Create an RPC to calculate ETA and distance on location update
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
    FROM public.orders WHERE id = p_order_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Order not found: %', p_order_id;
    END IF;

    -- Authorization check when called by authenticated/anon JWT
    IF auth.uid() IS NOT NULL THEN
        SELECT role INTO v_caller_role FROM public.profiles WHERE id = auth.uid();
        IF v_caller_role IS DISTINCT FROM 'admin' AND (v_partner_id IS DISTINCT FROM auth.uid() OR v_caller_role IS DISTINCT FROM 'delivery') THEN
            RAISE EXCEPTION 'Unauthorized: only the assigned delivery partner or an admin can update delivery location';
        END IF;
    ELSIF COALESCE(current_setting('request.jwt.claim.role', true), '') = 'anon' THEN
        RAISE EXCEPTION 'Authentication required';
    END IF;

    -- Haversine distance calculation with clamping to prevent acos domain errors
    IF v_dest_lat IS NOT NULL AND v_dest_lng IS NOT NULL THEN
        v_distance_m := ROUND(
            6371000 * acos(
                LEAST(1.0, GREATEST(-1.0,
                    cos(radians(v_dest_lat)) * cos(radians(p_lat)) * cos(radians(p_lng) - radians(v_dest_lng)) +
                    sin(radians(v_dest_lat)) * sin(radians(p_lat))
                ))
            )
        )::INTEGER;
        -- Assume 25 km/h average speed in city if speed is 0 or low (approx 7 m/s)
        v_speed_ms := GREATEST(COALESCE(p_speed, 0), 7.0); 
        v_eta_sec := GREATEST(60, ROUND(v_distance_m / v_speed_ms)::INTEGER);
    END IF;

    INSERT INTO public.delivery_locations (
        order_id, delivery_partner_id, latitude, longitude, accuracy, speed, heading, estimated_distance_m, estimated_time_sec, created_at, updated_at
    ) VALUES (
        p_order_id, COALESCE(v_partner_id, auth.uid()), p_lat, p_lng, p_accuracy, p_speed, p_heading, v_distance_m, v_eta_sec, NOW(), NOW()
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

