-- ============================================================
-- GOVIND — PHASE 2 CORRECTIVE MIGRATION: PRODUCT BULK TIERS
-- ============================================================

CREATE TABLE IF NOT EXISTS public.product_bulk_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    minimum_quantity INTEGER NOT NULL CHECK (minimum_quantity > 0),
    discount_percentage NUMERIC(5, 2) DEFAULT NULL CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    discounted_unit_price NUMERIC(10, 2) DEFAULT NULL CHECK (discounted_unit_price >= 0),
    pricing_type TEXT NOT NULL DEFAULT 'percentage' CHECK (pricing_type IN ('percentage', 'fixed')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT product_bulk_tiers_unique_tier UNIQUE (product_id, minimum_quantity)
);

-- Index for fast tier resolution during checkout
CREATE INDEX IF NOT EXISTS idx_product_bulk_tiers_product_qty 
ON public.product_bulk_tiers (product_id, minimum_quantity);

-- Enable RLS
ALTER TABLE public.product_bulk_tiers ENABLE ROW LEVEL SECURITY;

-- Drop any conflicting policies if re-run
DROP POLICY IF EXISTS "Public read bulk tiers" ON public.product_bulk_tiers;
DROP POLICY IF EXISTS "Admin manage bulk tiers" ON public.product_bulk_tiers;

-- Public can read active bulk pricing tiers
CREATE POLICY "Public read bulk tiers"
ON public.product_bulk_tiers
FOR SELECT
USING (is_active = true OR public.is_admin());

-- Only admins can insert, update, or delete bulk tiers
CREATE POLICY "Admin manage bulk tiers"
ON public.product_bulk_tiers
FOR ALL
USING (public.is_admin())
WITH CHECK (public.is_admin());

-- Permissions
GRANT SELECT ON public.product_bulk_tiers TO anon, authenticated;
GRANT ALL ON public.product_bulk_tiers TO service_role;
