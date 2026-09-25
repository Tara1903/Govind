-- Migration: 3-Experience Architecture Backend Schema
-- Adding explicit columns and structures for Fresh, Kitchen, and Wholesale

-- 1. Explicitly type categories and products for multi-experience
ALTER TABLE public.categories 
ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH' CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE'));

ALTER TABLE public.products 
ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH' CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE')),
ADD COLUMN IF NOT EXISTS min_order_quantity INTEGER DEFAULT 1,     -- For Wholesale
ADD COLUMN IF NOT EXISTS bulk_price NUMERIC(10, 2),                -- For Wholesale
ADD COLUMN IF NOT EXISTS is_veg BOOLEAN DEFAULT TRUE;              -- Inherited from Sardar Ji

-- 2. Expand orders table to support the 3 contexts natively
ALTER TABLE public.orders
ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH' CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE')),
ADD COLUMN IF NOT EXISTS kitchen_status TEXT CHECK (kitchen_status IN ('ORDER_PLACED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED')), -- Adapted from Sardar Ji
ADD COLUMN IF NOT EXISTS wholesale_status TEXT CHECK (wholesale_status IN ('QUOTE_REQUESTED', 'QUOTE_SENT', 'ORDER_CONFIRMED', 'DELIVERED'));

-- 3. B2B / Wholesale Profiles
CREATE TABLE IF NOT EXISTS public.business_profiles (
    id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    business_name TEXT NOT NULL,
    business_type TEXT,
    contact_person TEXT NOT NULL,
    gstin TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS for Business Profiles
ALTER TABLE public.business_profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can read own business profile" ON public.business_profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own business profile" ON public.business_profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can insert own business profile" ON public.business_profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Admins can manage all business profiles" ON public.business_profiles FOR ALL USING (
    EXISTS (SELECT 1 FROM public.profiles WHERE profiles.id = auth.uid() AND profiles.role = 'admin')
);

-- 4. Kitchen Menu Combinations / Variations (from Sardar Ji)
CREATE TABLE IF NOT EXISTS public.kitchen_variations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    additional_price NUMERIC(10, 2) DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.kitchen_variations ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read kitchen variations" ON public.kitchen_variations FOR SELECT USING (TRUE);
CREATE POLICY "Admins can manage variations" ON public.kitchen_variations FOR ALL USING (
    EXISTS (SELECT 1 FROM public.profiles WHERE profiles.id = auth.uid() AND profiles.role = 'admin')
);
