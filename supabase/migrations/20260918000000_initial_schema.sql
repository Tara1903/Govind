-- Initial Schema for Govind - Fresh and Healthy Food

-- Set up proper UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- PROFILES (Extended User Data)
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    avatar_url TEXT,
    role TEXT DEFAULT 'customer' CHECK (role IN ('customer', 'admin', 'delivery')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ADDRESSES
CREATE TABLE public.addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    house TEXT NOT NULL,
    street TEXT NOT NULL,
    area TEXT NOT NULL,
    landmark TEXT,
    city TEXT NOT NULL,
    pincode TEXT NOT NULL,
    delivery_instructions TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- CATEGORIES
CREATE TABLE public.categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    slug TEXT UNIQUE NOT NULL,
    image TEXT,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- PRODUCTS
CREATE TABLE public.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    slug TEXT UNIQUE NOT NULL,
    description TEXT,
    category_id UUID REFERENCES public.categories(id) ON DELETE SET NULL,
    price DECIMAL(10, 2) NOT NULL,
    selling_price DECIMAL(10, 2) NOT NULL,
    unit TEXT NOT NULL, -- e.g., '1kg', '500g', 'piece'
    stock_quantity INTEGER DEFAULT 0,
    low_stock_threshold INTEGER DEFAULT 5,
    min_order_quantity INTEGER DEFAULT 1,
    max_order_quantity INTEGER DEFAULT 10,
    discount_type TEXT CHECK (discount_type IN ('percentage', 'fixed', NULL)),
    discount_value DECIMAL(10, 2),
    active BOOLEAN DEFAULT TRUE,
    featured BOOLEAN DEFAULT FALSE,
    bestseller BOOLEAN DEFAULT FALSE,
    seasonal BOOLEAN DEFAULT FALSE,
    fresh_today BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- PRODUCT IMAGES
CREATE TABLE public.product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID REFERENCES public.products(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- FAVORITES
CREATE TABLE public.favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(profile_id, product_id)
);

-- CARTS
CREATE TABLE public.carts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID UNIQUE REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- CART ITEMS
CREATE TABLE public.cart_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID REFERENCES public.carts(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(cart_id, product_id)
);

-- COUPONS
CREATE TABLE public.coupons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code TEXT UNIQUE NOT NULL,
    discount_type TEXT NOT NULL CHECK (discount_type IN ('percentage', 'fixed')),
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2) DEFAULT 0,
    max_discount_amount DECIMAL(10, 2),
    expiration_date TIMESTAMPTZ,
    usage_limit INTEGER,
    per_user_limit INTEGER DEFAULT 1,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ORDERS
CREATE TABLE public.orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0,
    coupon_id UUID REFERENCES public.coupons(id) ON DELETE SET NULL,
    delivery_charge DECIMAL(10, 2) DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    savings DECIMAL(10, 2) DEFAULT 0,
    address_snapshot JSONB NOT NULL,
    payment_method TEXT NOT NULL CHECK (payment_method IN ('COD', 'ONLINE')),
    payment_status TEXT NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    order_status TEXT NOT NULL DEFAULT 'PENDING' CHECK (order_status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED')),
    razorpay_order_id TEXT,
    razorpay_payment_id TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ORDER ITEMS
CREATE TABLE public.order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE SET NULL,
    product_name TEXT NOT NULL,
    unit TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- PROMOTIONS
CREATE TABLE public.promotions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    subtitle TEXT,
    image TEXT NOT NULL,
    cta TEXT,
    deep_link TEXT,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ NOT NULL,
    priority INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- DELIVERY SETTINGS
CREATE TABLE public.delivery_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    min_order_amount DECIMAL(10, 2) DEFAULT 0,
    delivery_charge DECIMAL(10, 2) DEFAULT 40,
    free_delivery_threshold DECIMAL(10, 2) DEFAULT 500,
    serviceable_pincodes JSONB,
    active BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Setup (Basic Templates, needs expansion)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.product_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.carts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cart_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.promotions ENABLE ROW LEVEL SECURITY;

-- Profiles: Users can read and update their own profile. Admins can read all.
CREATE POLICY "Users can view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Addresses: Users can manage their own addresses.
CREATE POLICY "Users can manage own addresses" ON public.addresses FOR ALL USING (auth.uid() = profile_id);

-- Categories & Products: Anyone can read active. Admins can manage all.
CREATE POLICY "Public read active categories" ON public.categories FOR SELECT USING (active = TRUE);
CREATE POLICY "Public read active products" ON public.products FOR SELECT USING (active = TRUE);
CREATE POLICY "Public read product images" ON public.product_images FOR SELECT USING (TRUE);

-- Favorites
CREATE POLICY "Users manage own favorites" ON public.favorites FOR ALL USING (auth.uid() = profile_id);

-- Cart
CREATE POLICY "Users manage own cart" ON public.carts FOR ALL USING (auth.uid() = profile_id);
CREATE POLICY "Users manage own cart items" ON public.cart_items FOR ALL USING (
    EXISTS (SELECT 1 FROM public.carts WHERE id = cart_items.cart_id AND profile_id = auth.uid())
);

-- Orders
CREATE POLICY "Users view own orders" ON public.orders FOR SELECT USING (auth.uid() = customer_id);
CREATE POLICY "Users view own order items" ON public.order_items FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.orders WHERE id = order_items.order_id AND customer_id = auth.uid())
);

-- Promotions
CREATE POLICY "Public read active promotions" ON public.promotions FOR SELECT USING (active = TRUE AND start_date <= NOW() AND end_date >= NOW());

-- Storage
-- Need to create storage buckets for products, categories, promotions, avatars manually or via script.
