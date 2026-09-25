-- Seed data for Govind
INSERT INTO public.categories (id, name, slug, description, active) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Fruits', 'fruits', 'Fresh and seasonal fruits', true),
    ('22222222-2222-2222-2222-222222222222', 'Vegetables', 'vegetables', 'Daily fresh vegetables', true),
    ('33333333-3333-3333-3333-333333333333', 'Leafy Vegetables', 'leafy-vegetables', 'Fresh leafy greens', true);

INSERT INTO public.products (id, name, slug, category_id, price, selling_price, unit, stock_quantity, featured, bestseller, fresh_today) VALUES
    (uuid_generate_v4(), 'Apples', 'apples', '11111111-1111-1111-1111-111111111111', 120.00, 89.00, '1kg', 100, true, true, true),
    (uuid_generate_v4(), 'Bananas', 'bananas', '11111111-1111-1111-1111-111111111111', 60.00, 45.00, '1 dozen', 100, false, true, false),
    (uuid_generate_v4(), 'Mangoes', 'mangoes', '11111111-1111-1111-1111-111111111111', 200.00, 150.00, '1kg', 50, true, false, true),
    (uuid_generate_v4(), 'Tomatoes', 'tomatoes', '22222222-2222-2222-2222-222222222222', 40.00, 30.00, '1kg', 200, false, true, true),
    (uuid_generate_v4(), 'Potatoes', 'potatoes', '22222222-2222-2222-2222-222222222222', 30.00, 25.00, '1kg', 500, false, true, false),
    (uuid_generate_v4(), 'Onions', 'onions', '22222222-2222-2222-2222-222222222222', 35.00, 30.00, '1kg', 500, false, true, false),
    (uuid_generate_v4(), 'Spinach', 'spinach', '33333333-3333-3333-3333-333333333333', 25.00, 20.00, '1 bundle', 50, true, false, true);

INSERT INTO public.delivery_settings (min_order_amount, delivery_charge, free_delivery_threshold)
VALUES (100, 40, 500);
