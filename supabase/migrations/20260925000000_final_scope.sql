-- Migration: Govind Final Scope Update

-- 1. Add fields for Punjabi Food Menu Management
ALTER TABLE public.products 
ADD COLUMN menu_date DATE,
ADD COLUMN bulk_available BOOLEAN DEFAULT FALSE,
ADD COLUMN combo BOOLEAN DEFAULT FALSE,
ADD COLUMN daily_special BOOLEAN DEFAULT FALSE;

-- 2. Wholesale Contact / Settings
CREATE TABLE public.business_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    whatsapp_number TEXT NOT NULL DEFAULT '+919630937033',
    support_phone TEXT NOT NULL DEFAULT '+919630937033',
    wholesale_contact TEXT NOT NULL DEFAULT '+919630937033',
    delivery_announcement TEXT DEFAULT 'FREE HOME DELIVERY',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS
ALTER TABLE public.business_settings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read business settings" ON public.business_settings FOR SELECT USING (TRUE);

-- Ensure everyone can view products
DROP POLICY IF EXISTS "Public read active products" ON public.products;
CREATE POLICY "Public read active products" ON public.products FOR SELECT USING (active = TRUE);
