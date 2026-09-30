# GOVIND — PHASE 0 SUPABASE & DATABASE BASELINE

**Recorded At:** 2026-09-29T15:54:00+05:30  
**Supabase Project Reference:** `crkuiuxajywlgmlnklvj` (`https://crkuiuxajywlgmlnklvj.supabase.co`)  
**Mode:** 100% Read-Only Introspection (Zero mutations performed)

---

## 1. MIGRATION FILES IN REPOSITORY (`C:\Web Apps\Govind\supabase\migrations`)

| Migration File | Git Status | Applied to Live DB? | Summary of Contents |
| :--- | :--- | :--- | :--- |
| `20260925000000_initial_schema.sql` | Tracked | Partially overwritten by `00000000000001` | Initial schema (`profiles`, `categories`, `products`, `product_images`, `carts`, `cart_items`, `addresses`, `orders`, `order_items`, `coupons`, `promotions`, `favorites`, `delivery_settings`, `business_settings`) |
| `20260925000001_consolidated_schema.sql` | Tracked | **YES** | Drops and recreates core tables (`profiles`, `business_profiles`, `categories`, `products`, `product_images`, `kitchen_variations`, `addresses`, `carts`, `cart_items`, `orders`, `order_items`, `favorites`, `coupons`, `promotions`, `delivery_settings`, `business_settings`) and `public.is_admin()`. Drops `on_auth_user_created` trigger without recreating it. |
| `20260925000002_storage_buckets.sql` | Tracked | **YES** | Creates public storage buckets (`products`, `categories`, `promotions`, `avatars`) and storage RLS policies. |
| `20260926000000_production_hardening.sql` | Tracked | **YES** | Adds `create_order_and_decrement_stock` RPC and indexes. |
| `20260926000001_design_bible_schema.sql` | Tracked | **YES** | Adds `on_fresh_board`, `product_type`, `bundle_items` to `public.products`. |
| `20260927000000_catalogue_updates.sql` | Untracked (`??`) | **YES** | Adds `bulk_tiers` JSONB column to `public.products`. |
| `20260927000001_test_sync.sql` | Untracked (`??`) | **YES** | Creates test RPC `public.test_sync()` (`SECURITY DEFINER`). |
| `20260927000002_unified_cart.sql` | Untracked (`??`) | **PARTIAL** | Updates `create_order_and_decrement_stock` RPC and `order_items` columns (`experience_type`, `base_price`, `bulk_discount`, `effective_unit_price`, `line_total`). |
| `20260927000003_delivery_tracking.sql` | Untracked (`??`) | **NO (`404` on live DB)** | Defines `public.delivery_locations` table, `orders.delivery_partner_id`, and `public.update_delivery_location` RPC. **Not applied to live DB.** |
| `20260927000004_order_history.sql` | Untracked (`??`) | **NO (`404` on live DB)** | Defines `public.order_status_history` table and `log_order_status_change` trigger. **Not applied to live DB.** |

---

## 2. LIVE PUBLIC TABLES & ROW COUNTS

| Table Name | Live Status | Row Count | Notes |
| :--- | :--- | :--- | :--- |
| `public.categories` | EXISTS | `16` | Contains duplicate Fresh category names (`Fruits` vs `Fresh Fruits`, `Vegetables` vs `Fresh Vegetables`, `Combos & Packs` vs `Fresh Combos`). |
| `public.products` | EXISTS | `85` | `53 FRESH`, `14 KITCHEN`, `18 WHOLESALE`. `83` have `on_rate_list = true`, `18` have `on_fresh_board = true`. |
| `public.product_images` | EXISTS | `79` | All 79 rows use `https://ui-avatars.com/api/?name=...` placeholder URLs. 6 products have no image row. |
| `public.delivery_settings` | EXISTS | `1` | `base_delivery_charge = 30`, `free_delivery_threshold = 500`, `is_delivery_enabled = true`. |
| `public.profiles` | EXISTS | `0` | Empty (`0` rows) despite `2` users in `auth.users`. |
| `public.business_profiles` | EXISTS | `0` | Empty. |
| `public.kitchen_variations` | EXISTS | `0` | Empty. |
| `public.addresses` | EXISTS | `0` | Empty. |
| `public.carts` | EXISTS | `0` | Empty. |
| `public.cart_items` | EXISTS | `0` | Empty. |
| `public.orders` | EXISTS | `0` | Empty. |
| `public.order_items` | EXISTS | `0` | Empty. |
| `public.favorites` | EXISTS | `0` | Empty. |
| `public.coupons` | EXISTS | `0` | Empty. |
| `public.promotions` | EXISTS | `0` | Empty. |
| `public.business_settings` | EXISTS | `0` | Empty. |
| `public.delivery_locations` | **MISSING (`404`)** | N/A | Defined in unapplied migration `20260927000003_delivery_tracking.sql`. |
| `public.order_status_history` | **MISSING (`404`)** | N/A | Defined in unapplied migration `20260927000004_order_history.sql`. |
| `public.subcategories` | **MISSING (`404`)** | N/A | Never created in any migration. |
| `public.product_bulk_tiers` | **MISSING (`404`)** | N/A | Bulk tiers are stored in `products.bulk_tiers` JSONB column instead. |

---

## 3. LIVE RPC FUNCTIONS (`public`)

| Function / RPC Name | Live Status | Security Mode | Notes |
| :--- | :--- | :--- | :--- |
| `public.is_admin()` | LIVE | `SECURITY DEFINER` | Checks `EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')`. |
| `public.create_order_and_decrement_stock(...)` | LIVE | `SECURITY DEFINER` | Creates `orders` + `order_items` and decrements `products.stock`. Fails with FK `23503` if user has no row in `public.profiles`. |
| `public.test_sync()` | LIVE | `SECURITY DEFINER` | Temporary debug RPC that sets price of `'Fresh Ginger (Adrak)'` to `99`. Should be dropped in Phase 1. |
| `public.update_delivery_location(...)` | **MISSING (`404`)** | N/A | Defined in unapplied migration `20260927000003_delivery_tracking.sql`. |

---

## 4. AUTH USERS & STORAGE BUCKETS SUMMARY

- **Auth Users (`auth.users`):** `2` registered users (emails and PII redacted per security rules).
- **Profiles (`public.profiles`):** `0` rows.
- **Storage Buckets (`storage.buckets`):** `6` public buckets exist:
  1. `Products` (public, standard, created `2026-09-25T09:29:17Z`)
  2. `Banners` (public, standard, created `2026-09-25T09:29:31Z`)
  3. `products` (public, standard, created `2026-09-25T20:48:08Z`)
  4. `categories` (public, standard, created `2026-09-25T20:48:08Z`)
  5. `promotions` (public, standard, created `2026-09-25T20:48:08Z`)
  6. `avatars` (public, standard, created `2026-09-25T20:48:08Z`)
- **Edge Functions (`C:\Web Apps\Govind\supabase\functions`):**
  - `razorpay-webhook/index.ts` (local source present; verifies Razorpay HMAC signature and updates `orders.payment_status = 'PAID'`).

---

## 5. RLS & SCHEMA RISK BASELINE

1. **Tables Missing `ENABLE ROW LEVEL SECURITY`:** `public.coupons` and `public.delivery_settings` did not have `ALTER TABLE ... ENABLE ROW LEVEL SECURITY` executed in `20260925000001_consolidated_schema.sql`.
2. **Self-Role Escalation on `public.profiles`:** The `Users can update own profile` policy (`FOR UPDATE USING (auth.uid() = id)`) lacks a `WITH CHECK` clause preventing users from updating `role`.
3. **Enum / Constraint Mismatches:**
   - `profiles.role`: `'customer' | 'admin' | 'delivery'` in `00000000000001` vs `'CUSTOMER' | 'ADMIN' | 'DELIVERY_PARTNER'` in `0003` and Admin UI.
   - `orders.order_status`: `'PLACED' | 'CONFIRMED' | 'PREPARING' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED'` in `00000000000001` vs `'PENDING'` and `'READY_FOR_DELIVERY'` in `0003`, Admin `/orders/[id]`, and Android `OrderDetailsScreen`.
   - `orders.experience_type`: `'FRESH' | 'KITCHEN' | 'WHOLESALE'` in `00000000000001` vs `'UNIFIED'` sent by Android and Admin `/api/checkout`.
