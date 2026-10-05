-- Migration: 20261001000001_sync_product_bulk_tiers.sql
-- Function and trigger to automatically synchronize product_bulk_tiers table into products.bundle_items.wholesale_pricing

CREATE OR REPLACE FUNCTION public.sync_product_bulk_tiers_to_bundle()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE
    v_product_id UUID;
    v_tiers JSONB;
    v_bundle JSONB;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_product_id := OLD.product_id;
    ELSE
        v_product_id := NEW.product_id;
    END IF;

    -- Aggregate active tiers for this product
    SELECT jsonb_agg(
        jsonb_build_object(
            'min_qty', minimum_quantity,
            'type', COALESCE(pricing_type, 'percentage'),
            'value', discount_percentage,
            'status', CASE WHEN is_active THEN 'active' ELSE 'inactive' END
        ) ORDER BY minimum_quantity ASC
    )
    INTO v_tiers
    FROM public.product_bulk_tiers
    WHERE product_id = v_product_id AND is_active = true;

    -- Get existing bundle_items
    SELECT COALESCE(bundle_items, '{}'::jsonb)
    INTO v_bundle
    FROM public.products
    WHERE id = v_product_id;

    IF v_tiers IS NOT NULL AND jsonb_array_length(v_tiers) > 0 THEN
        v_bundle := jsonb_set(
            v_bundle,
            '{wholesale_pricing}',
            jsonb_build_object(
                'wholesale_eligible', true,
                'tiers', v_tiers
            )
        );
    ELSE
        -- Remove or set inactive
        v_bundle := v_bundle - 'wholesale_pricing';
    END IF;

    -- Update product
    UPDATE public.products
    SET bundle_items = v_bundle,
        bulk_available = (v_tiers IS NOT NULL AND jsonb_array_length(v_tiers) > 0),
        updated_at = now()
    WHERE id = v_product_id;

    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_product_bulk_tiers ON public.product_bulk_tiers;

CREATE TRIGGER trg_sync_product_bulk_tiers
    AFTER INSERT OR UPDATE OR DELETE ON public.product_bulk_tiers
    FOR EACH ROW
    EXECUTE FUNCTION public.sync_product_bulk_tiers_to_bundle();
