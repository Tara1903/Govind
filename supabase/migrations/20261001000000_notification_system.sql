CREATE TABLE public.notification_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    fcm_token TEXT NOT NULL,
    platform TEXT NOT NULL DEFAULT 'android' CHECK (platform IN ('android', 'ios', 'web')),
    device_model TEXT,
    os_version TEXT,
    app_version TEXT,
    locale TEXT DEFAULT 'en',
    timezone TEXT DEFAULT 'Asia/Kolkata',
    enabled BOOLEAN NOT NULL DEFAULT true,
    last_seen_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
-- Unique per token (one device per FCM token)
CREATE UNIQUE INDEX notification_devices_token_idx ON public.notification_devices(fcm_token);
-- Index for user lookup
CREATE INDEX notification_devices_user_idx ON public.notification_devices(user_id);

CREATE TABLE public.notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES public.profiles(id) ON DELETE CASCADE,
    -- Transactional (should default to true, never fully disable order updates)
    order_updates BOOLEAN NOT NULL DEFAULT true,
    delivery_updates BOOLEAN NOT NULL DEFAULT true,
    -- Promotional
    cart_reminders BOOLEAN NOT NULL DEFAULT true,
    offers BOOLEAN NOT NULL DEFAULT true,
    marketing BOOLEAN NOT NULL DEFAULT false,
    new_products BOOLEAN NOT NULL DEFAULT true,
    favorite_alerts BOOLEAN NOT NULL DEFAULT true,
    wholesale_updates BOOLEAN NOT NULL DEFAULT true,
    -- Quiet hours (stored as HH:MM, evaluated in Asia/Kolkata)
    quiet_hours_enabled BOOLEAN NOT NULL DEFAULT false,
    quiet_hours_start TEXT DEFAULT '22:00',
    quiet_hours_end TEXT DEFAULT '07:00',
    timezone TEXT DEFAULT 'Asia/Kolkata',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX notification_preferences_user_idx ON public.notification_preferences(user_id);

CREATE TABLE public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN (
        'ORDER', 'DELIVERY', 'CART', 'OFFER', 'MARKETING',
        'PRODUCT', 'FAVORITE', 'ACCOUNT', 'WHOLESALE', 'SYSTEM'
    )),
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    image_url TEXT,
    deep_link TEXT,
    order_id UUID REFERENCES public.orders(id) ON DELETE SET NULL,
    product_id UUID,
    campaign_id UUID,
    metadata JSONB DEFAULT '{}',
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    expires_at TIMESTAMPTZ
);
CREATE INDEX notifications_user_idx ON public.notifications(user_id, created_at DESC);
CREATE INDEX notifications_unread_idx ON public.notifications(user_id, read_at) WHERE read_at IS NULL;

CREATE TABLE public.notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    type TEXT NOT NULL CHECK (type IN (
        'ORDER_PLACED', 'ORDER_CONFIRMED', 'ORDER_PREPARING', 'ORDER_READY',
        'ORDER_OUT_FOR_DELIVERY', 'ORDER_DELIVERED', 'ORDER_CANCELLED',
        'REFUND_INITIATED', 'REFUND_COMPLETED', 'CART_REMINDER',
        'BACK_IN_STOCK', 'PRICE_DROP', 'OFFER', 'MARKETING', 'WHOLESALE_UPDATE'
    )),
    title_template TEXT NOT NULL,
    body_template TEXT NOT NULL,
    deep_link_template TEXT,
    channel TEXT NOT NULL DEFAULT 'govind_orders' CHECK (channel IN (
        'govind_orders', 'govind_delivery', 'govind_cart',
        'govind_offers', 'govind_marketing', 'govind_account', 'govind_system'
    )),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

INSERT INTO public.notification_templates (name, type, title_template, body_template, deep_link_template, channel) VALUES
('order_placed', 'ORDER_PLACED', 'Order placed successfully 🛒', 'Your GOVIND order #{{order_number}} has been placed. We will confirm it shortly.', 'govind://order/{{order_id}}', 'govind_orders'),
('order_confirmed', 'ORDER_CONFIRMED', 'Order confirmed ✅', 'Your GOVIND order #{{order_number}} is confirmed and being prepared.', 'govind://order/{{order_id}}', 'govind_orders'),
('order_preparing', 'ORDER_PREPARING', 'Your order is being prepared 👨‍🍳', 'Your GOVIND order #{{order_number}} is being packed fresh for you.', 'govind://order/{{order_id}}', 'govind_orders'),
('order_ready', 'ORDER_READY', 'Order packed & ready 📦', 'Your GOVIND order #{{order_number}} is packed and ready for dispatch.', 'govind://order/{{order_id}}', 'govind_orders'),
('order_out_for_delivery', 'ORDER_OUT_FOR_DELIVERY', 'Your order is on the way 🚴', 'Your GOVIND order is out for delivery. Track your rider in real time.', 'govind://order/{{order_id}}/tracking', 'govind_delivery'),
('order_delivered', 'ORDER_DELIVERED', 'Order delivered 🎉', 'Your GOVIND order #{{order_number}} has been delivered. Enjoy your fresh produce!', 'govind://order/{{order_id}}', 'govind_orders'),
('order_cancelled', 'ORDER_CANCELLED', 'Order cancelled', 'Your GOVIND order #{{order_number}} has been cancelled. Contact support if needed.', 'govind://order/{{order_id}}', 'govind_orders'),
('cart_reminder', 'CART_REMINDER', 'Your cart is waiting 🛒', 'You have fresh picks waiting in your cart. Complete your order before they sell out!', 'govind://cart', 'govind_cart'),
('offer_notification', 'OFFER', 'Fresh picks are here 🌿', 'New offers are waiting for you at GOVIND. Check out today''s specials.', 'govind://home', 'govind_offers'),
('marketing_generic', 'MARKETING', 'New at GOVIND', 'Discover what''s new at GOVIND today.', 'govind://home', 'govind_marketing'),
('wholesale_update', 'WHOLESALE_UPDATE', 'Wholesale update 📋', 'Your GOVIND wholesale order update is ready.', 'govind://order/{{order_id}}', 'govind_orders');

CREATE TABLE public.notification_campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    template_id UUID REFERENCES public.notification_templates(id) ON DELETE SET NULL,
    -- Custom overrides (if not using template)
    custom_title TEXT,
    custom_body TEXT,
    custom_image_url TEXT,
    custom_deep_link TEXT,
    -- Targeting
    audience_type TEXT NOT NULL DEFAULT 'ALL_CUSTOMERS' CHECK (audience_type IN (
        'ALL_CUSTOMERS', 'FRESH_CUSTOMERS', 'KITCHEN_CUSTOMERS', 'WHOLESALE_CUSTOMERS',
        'CUSTOMERS_WITH_CART', 'CUSTOMERS_NO_ORDER_7_DAYS', 'CUSTOMERS_NO_ORDER_30_DAYS',
        'CUSTOM_USER_LIST'
    )),
    audience_filter JSONB DEFAULT '{}',
    -- Scheduling
    status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN (
        'DRAFT', 'SCHEDULED', 'PROCESSING', 'SENT', 'PARTIAL', 'FAILED', 'CANCELLED', 'EXPIRED'
    )),
    scheduled_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    -- Stats (updated by dispatcher)
    audience_size INTEGER DEFAULT 0,
    sent_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    opened_count INTEGER DEFAULT 0,
    -- Meta
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX notification_campaigns_status_idx ON public.notification_campaigns(status, scheduled_at);

CREATE TABLE public.notification_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Deduplication key (e.g. "order:uuid:CONFIRMED")
    dedup_key TEXT NOT NULL UNIQUE,
    -- Target
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    campaign_id UUID REFERENCES public.notification_campaigns(id) ON DELETE CASCADE,
    template_name TEXT,
    -- Payload
    notification_type TEXT NOT NULL,
    payload JSONB NOT NULL DEFAULT '{}',
    -- Lifecycle
    status TEXT NOT NULL DEFAULT 'QUEUED' CHECK (status IN (
        'QUEUED', 'SENDING', 'SENT', 'FAILED', 'SKIPPED', 'SUPPRESSED'
    )),
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    -- Scheduling
    process_after TIMESTAMPTZ DEFAULT now(),
    sent_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX notification_outbox_queue_idx ON public.notification_outbox(status, process_after) WHERE status = 'QUEUED';

CREATE TABLE public.notification_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID REFERENCES public.notifications(id) ON DELETE CASCADE,
    outbox_id UUID REFERENCES public.notification_outbox(id) ON DELETE CASCADE,
    device_id UUID REFERENCES public.notification_devices(id) ON DELETE SET NULL,
    fcm_message_id TEXT,
    status TEXT NOT NULL DEFAULT 'QUEUED' CHECK (status IN (
        'QUEUED', 'SENDING', 'SENT', 'FAILED', 'INVALID_TOKEN', 'SKIPPED', 'SUPPRESSED'
    )),
    error_code TEXT,
    attempt_count INTEGER DEFAULT 0,
    sent_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    opened_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX notification_deliveries_notif_idx ON public.notification_deliveries(notification_id);

CREATE OR REPLACE FUNCTION public.create_default_notification_preferences()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    INSERT INTO public.notification_preferences (user_id)
    VALUES (NEW.id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$;

CREATE TRIGGER on_profile_created_notification_prefs
    AFTER INSERT ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.create_default_notification_preferences();

CREATE OR REPLACE FUNCTION public.queue_order_notification()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE
    v_template_name TEXT;
    v_deep_link TEXT;
    v_order_short TEXT;
BEGIN
    -- Only fire on status changes
    IF TG_OP = 'UPDATE' AND OLD.order_status = NEW.order_status THEN
        RETURN NEW;
    END IF;

    v_order_short := UPPER(SUBSTRING(NEW.id::TEXT, 1, 8));
    
    -- Map status to template
    v_template_name := CASE NEW.order_status
        WHEN 'PLACED'            THEN 'order_placed'
        WHEN 'CONFIRMED'         THEN 'order_confirmed'
        WHEN 'PREPARING'         THEN 'order_preparing'
        WHEN 'READY_FOR_DELIVERY' THEN 'order_ready'
        WHEN 'OUT_FOR_DELIVERY'  THEN 'order_out_for_delivery'
        WHEN 'DELIVERED'         THEN 'order_delivered'
        WHEN 'CANCELLED'         THEN 'order_cancelled'
        ELSE NULL
    END;

    IF v_template_name IS NULL THEN
        RETURN NEW;
    END IF;

    -- Insert into outbox with dedup key
    INSERT INTO public.notification_outbox (
        dedup_key,
        user_id,
        template_name,
        notification_type,
        payload
    ) VALUES (
        'order:' || NEW.id::TEXT || ':' || NEW.order_status,
        NEW.customer_id,
        v_template_name,
        'ORDER',
        jsonb_build_object(
            'order_id', NEW.id,
            'order_number', v_order_short,
            'order_status', NEW.order_status,
            'experience_type', NEW.experience_type,
            'total', NEW.total
        )
    )
    ON CONFLICT (dedup_key) DO NOTHING; -- deduplication

    RETURN NEW;
END;
$$;

CREATE TRIGGER on_order_status_queue_notification
    AFTER INSERT OR UPDATE OF order_status ON public.orders
    FOR EACH ROW
    EXECUTE FUNCTION public.queue_order_notification();

CREATE OR REPLACE FUNCTION public.mark_notification_read(p_notification_id UUID)
RETURNS VOID LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    UPDATE public.notifications
    SET read_at = now()
    WHERE id = p_notification_id AND user_id = auth.uid() AND read_at IS NULL;
END;
$$;

CREATE OR REPLACE FUNCTION public.mark_all_notifications_read()
RETURNS VOID LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    UPDATE public.notifications
    SET read_at = now()
    WHERE user_id = auth.uid() AND read_at IS NULL;
END;
$$;

CREATE OR REPLACE FUNCTION public.upsert_device_token(
    p_fcm_token TEXT,
    p_device_model TEXT DEFAULT NULL,
    p_os_version TEXT DEFAULT NULL,
    p_app_version TEXT DEFAULT NULL
)
RETURNS UUID LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE
    v_device_id UUID;
BEGIN
    INSERT INTO public.notification_devices (user_id, fcm_token, device_model, os_version, app_version, last_seen_at)
    VALUES (auth.uid(), p_fcm_token, p_device_model, p_os_version, p_app_version, now())
    ON CONFLICT (fcm_token) DO UPDATE
        SET user_id = auth.uid(),
            device_model = COALESCE(EXCLUDED.device_model, notification_devices.device_model),
            os_version = COALESCE(EXCLUDED.os_version, notification_devices.os_version),
            app_version = COALESCE(EXCLUDED.app_version, notification_devices.app_version),
            last_seen_at = now(),
            enabled = true,
            updated_at = now()
    RETURNING id INTO v_device_id;
    RETURN v_device_id;
END;
$$;

ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;
