-- Migration: Design Bible Execution (Schema Fixes)

-- 1. Fix the Cart Schema for Multi-Experience
-- Drop the existing unique constraint on profile_id
DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'public.carts'::regclass 
      AND contype = 'u';
      
    IF constraint_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE public.carts DROP CONSTRAINT ' || constraint_name;
    END IF;
END $$;

ALTER TABLE public.carts 
ADD COLUMN IF NOT EXISTS experience_type TEXT NOT NULL DEFAULT 'FRESH' CHECK (experience_type IN ('FRESH', 'KITCHEN', 'WHOLESALE'));

-- Create the new composite unique constraint
ALTER TABLE public.carts 
ADD CONSTRAINT carts_profile_id_experience_type_key UNIQUE (profile_id, experience_type);

-- 2. Add Signature Feature Support to Products
ALTER TABLE public.products
ADD COLUMN IF NOT EXISTS on_fresh_board BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS product_type TEXT DEFAULT 'SINGLE' CHECK (product_type IN ('SINGLE', 'PACK', 'COMBO')),
ADD COLUMN IF NOT EXISTS bundle_items JSONB; -- e.g., [{"product_id": "uuid", "qty": 1}]
