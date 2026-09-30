-- Migration 20260927000004_order_history.sql

CREATE TABLE IF NOT EXISTS public.order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    status TEXT NOT NULL CHECK (status IN ('PLACED', 'PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED')),
    updated_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    changed_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    changed_at TIMESTAMPTZ DEFAULT NOW(),
    note TEXT
);

ALTER TABLE public.order_status_history ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Customers view own order history" ON public.order_status_history;
CREATE POLICY "Customers view own order history" ON public.order_status_history
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.orders WHERE id = order_status_history.order_id AND customer_id = auth.uid())
    );

DROP POLICY IF EXISTS "Admins manage all order history" ON public.order_status_history;
CREATE POLICY "Admins manage all order history" ON public.order_status_history
    FOR ALL USING (public.is_admin())
    WITH CHECK (public.is_admin());

DROP POLICY IF EXISTS "Delivery partner can view and insert order history" ON public.order_status_history;
CREATE POLICY "Delivery partner can view and insert order history" ON public.order_status_history
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.orders WHERE id = order_status_history.order_id AND delivery_partner_id = auth.uid())
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
BEGIN
    IF auth.uid() IS NOT NULL AND EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid()) THEN
        v_actor := auth.uid();
    ELSE
        v_actor := NULL;
    END IF;

    IF (TG_OP = 'INSERT') OR (TG_OP = 'UPDATE' AND OLD.order_status IS DISTINCT FROM NEW.order_status) THEN
        INSERT INTO public.order_status_history (
            order_id, status, updated_by, notes, created_at, changed_by, changed_at, note
        ) VALUES (
            NEW.id,
            NEW.order_status,
            v_actor,
            CASE WHEN TG_OP = 'INSERT' THEN 'Order created' ELSE 'Status updated to ' || NEW.order_status END,
            NOW(),
            v_actor,
            NOW(),
            CASE WHEN TG_OP = 'INSERT' THEN 'Order created' ELSE 'Status updated to ' || NEW.order_status END
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

