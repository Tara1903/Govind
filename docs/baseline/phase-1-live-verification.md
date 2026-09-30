# GOVIND — LIVE SUPABASE PHASE 1 VERIFICATION

## 1. Verification timestamp
- **Verification Date & Time:** `2026-09-29T18:30:00+05:30` (IST) / `2026-09-29T13:00:00Z` (UTC)
- **Auditor:** Lead Full-Stack & Security QA Engineer (Antigravity Forensic Verification Suite)
- **Environment:** Live Supabase Production Project

## 2. Project identity
- **Supabase Project Ref / ID:** `crkuiuxajywlgmlnklvj`
- **Project Name:** `Govind`
- **Region:** `ap-southeast-1` (Singapore)
- **Database Status:** `ACTIVE_HEALTHY`
- **Database Endpoint:** `db.crkuiuxajywlgmlnklvj.supabase.co`
- **API Endpoint:** `https://crkuiuxajywlgmlnklvj.supabase.co`
- **PostgreSQL Version:** `PostgreSQL 17.6.1.166 on aarch64-unknown-linux-gnu`
- **Connected Client:** Supabase Management API + Postgres Direct Query API

## 3. Authentication/access result
- **API Authentication:** Verified HTTP 200 OK using Supabase Personal Access Token (`sbp_...`) via Management API.
- **Project Scope:** Verified accessible project is exclusively `crkuiuxajywlgmlnklvj`.
- **Credential Storage:** Token was verified securely through process memory and local credential store. No token value was written into repository code, documentation, git history, or logs.
- **SQL Execution Access:** Full superuser SQL execution verified against `pg_catalog`, `information_schema`, `auth`, `public`, and `storage`.
- **Supabase CLI:** Linked and authenticated to project `crkuiuxajywlgmlnklvj`.

## 4. Migration verification
- **Local Migration Files in `supabase/migrations/`:**
  1. `20260925000000_initial_schema.sql`
  2. `20260926000000_multi_experience_schema.sql`
  3. `20260926000001_design_bible_schema.sql`
  4. `20260927000000_catalogue_updates.sql`
  5. `20260927000001_test_sync.sql`
  6. `20260927000002_unified_cart.sql`
  7. `20260927000003_delivery_tracking.sql`
  8. `20260927000004_order_history.sql`
  9. `20260929000000_phase1_foundation_and_security.sql`
- **Live Migration Tracking Table (`supabase_migrations.schema_migrations`):**
  - **State:** Empty (`0` rows). Migrations were executed via the Supabase Dashboard SQL Editor rather than `supabase db push`.
- **Live Verification of Phase 1 Migration Objects:**
  - `public.create_order_and_decrement_stock` — **VERIFIED LIVE** (13 arguments with defaults, `SECURITY DEFINER`, `search_path=public`).
  - `public.enforce_order_update_permissions` & trigger `enforce_order_update_permissions_trigger` — **VERIFIED LIVE**.
  - `public.log_order_status_change` & trigger `on_order_status_change` — **VERIFIED LIVE**.
  - `public.prevent_profile_role_escalation` & trigger `prevent_profile_role_escalation_trigger` — **VERIFIED LIVE**.
  - `public.handle_new_user` & trigger `on_auth_user_created` on `auth.users` — **VERIFIED LIVE**.
  - `public.update_delivery_location` — **VERIFIED LIVE**.
  - `public.get_my_role`, `public.is_admin`, `public.is_delivery_or_admin` — **VERIFIED LIVE**.
  - `supabase_realtime` publication — **VERIFIED LIVE** (`delivery_locations`, `order_status_history`, `orders`).
- **Drift Analysis:**
  - `public.product_bulk_tiers`: Defined in local `20260926000000_multi_experience_schema.sql`, but does NOT exist in live DB. In live DB, wholesale pricing tiers are stored inside `products.bundle_items->'wholesale_pricing'`.
  - `public.carts` constraint: Contains two identical unique btree constraints on `(profile_id, experience_type)` (`carts_profile_experience_unique` and `carts_profile_id_experience_type_key`). Non-breaking.

## 5. Live schema verification
- **Total Public Tables:** `18` tables.
- **Row Level Security (RLS) State:** `rls_enabled = true` on ALL `18` tables (100% RLS coverage).
- **Public Table Inventory:**
  1. `addresses` (`id, profile_id, name, phone, house, street, area, landmark, city, pincode, delivery_instructions, is_default, created_at, updated_at, latitude, longitude`)
  2. `business_profiles` (`id, business_name, business_type, contact_person, gstin, created_at, updated_at`)
  3. `business_settings` (`id, whatsapp_number, support_phone, wholesale_contact, delivery_announcement, updated_at`)
  4. `cart_items` (`id, cart_id, product_id, quantity, created_at, updated_at, experience_type`)
  5. `carts` (`id, profile_id, created_at, updated_at, experience_type`)
  6. `categories` (`id, name, slug, image, description, display_order, active, created_at, updated_at, experience_type`)
  7. `coupons` (`id, code, discount_type, discount_value, min_order_amount, max_discount_amount, expiration_date, usage_limit, per_user_limit, active, created_at, updated_at`)
  8. `delivery_locations` (`id, order_id, delivery_partner_id, latitude, longitude, accuracy, speed, heading, estimated_distance_m, estimated_time_sec, created_at, updated_at`)
  9. `delivery_settings` (`id, min_order_amount, delivery_charge, free_delivery_threshold, serviceable_pincodes, active, updated_at`)
  10. `favorites` (`id, profile_id, product_id, created_at`)
  11. `kitchen_variations` (`id, product_id, name, additional_price, created_at`)
  12. `order_items` (`id, order_id, product_id, product_name, unit, price, quantity, discount, created_at, experience_type, base_price, bulk_discount, promotion_discount, coupon_discount, effective_unit_price, line_total`)
  13. `order_status_history` (`id, order_id, status, updated_by, notes, created_at, changed_by, changed_at, note`)
  14. `orders` (`id, customer_id, subtotal, discount, coupon_id, delivery_charge, total, savings, address_snapshot, payment_method, payment_status, order_status, razorpay_order_id, razorpay_payment_id, created_at, updated_at, experience_type, kitchen_status, wholesale_status, delivery_partner_id, dest_latitude, dest_longitude, address_id`)
  15. `product_images` (`id, product_id, image_url, display_order, created_at`)
  16. `products` (`id, name, slug, description, category_id, price, selling_price, unit, stock_quantity, low_stock_threshold, min_order_quantity, max_order_quantity, discount_type, discount_value, active, featured, bestseller, seasonal, fresh_today, created_at, updated_at, menu_date, bulk_available, combo, daily_special, experience_type, bulk_price, is_veg, on_fresh_board, product_type, bundle_items, subcategory_id, image_url, gallery_images, exotic, premium, short_description`)
  17. `profiles` (`id, name, phone, email, avatar_url, role, created_at, updated_at, full_name, address`)
  18. `promotions` (`id, title, subtitle, image, cta, deep_link, start_date, end_date, priority, active, created_at, updated_at`)

## 6. Profiles/auth trigger
- **Auth Users vs Profiles Parity:**
  - `auth.users` count: `2`
  - `public.profiles` count: `2`
  - Unmatched Auth Users (`auth_without_profile`): `0`
  - Unmatched Profiles (`profile_without_auth`): `0`
  - Duplicate Profile IDs: `0`
- **New User Trigger (`on_auth_user_created`):**
  - Event: `AFTER INSERT ON auth.users FOR EACH ROW EXECUTE FUNCTION handle_new_user()`
  - Function Logic: Reads `raw_user_meta_data->>'full_name'`, `raw_user_meta_data->>'name'`, phone, email; sets default role to `'customer'`.
  - Verified Live via Transactional Test: New auth user creation immediately created corresponding profile with role `'customer'`.
- **Self-Escalation Prevention Trigger (`prevent_profile_role_escalation_trigger`):**
  - Event: `BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE FUNCTION prevent_profile_role_escalation()`
  - Function Logic: When `NEW.role IS DISTINCT FROM OLD.role`, enforces `auth.uid() IS NOT NULL AND is_admin()`. Rejects customer and anonymous JWTs.
  - Verified Live via Transactional Test: Customer attempting `UPDATE profiles SET role = 'admin'` was blocked with SQL error: `"Unauthorized: cannot change profile role"`.

## 7. Role security
- **Canonical Role Vocabulary:**
  - Normalized to lowercase: `'customer'`, `'admin'`, `'delivery'`.
  - Constraint: `profiles_role_check` enforces `CHECK (role = ANY (ARRAY['customer'::text, 'admin'::text, 'delivery'::text]))`.
  - Column Default on `profiles.role`: `'customer'::text`.
- **Role Consistency Across All Functions & Policies:**
  - `handle_new_user()` assigns `'customer'`.
  - `is_admin()` checks `role = 'admin'`.
  - `is_delivery_or_admin()` checks `role IN ('delivery', 'admin')`.
  - `enforce_order_update_permissions()` checks `role = 'delivery'`.
  - `update_delivery_location()` checks `role IN ('delivery', 'admin')`.
  - `storage.objects` policies check `profiles.role = 'admin'::text`.
  - `order_status_history` insert policy checks `p.role = 'delivery'::text`.
  - `business_profiles` and `kitchen_variations` policies check `profiles.role = 'admin'::text`.
  - Zero mixed-casing found across live PostgreSQL catalog.

## 8. RLS audit
- **Summary:** Full audit of all 18 tables + `storage.objects` completed.
- **Specific Table Findings:**
  - `profiles`:
    - SELECT: `(auth.uid() = id) OR is_admin()`
    - INSERT: `(auth.uid() = id) AND (role = 'customer')`
    - UPDATE: `(auth.uid() = id)` with check `(auth.uid() = id) AND (role = get_my_role())` + admin override.
    - DELETE: `is_admin()`
  - `addresses`:
    - SELECT: `(auth.uid() = profile_id) OR is_admin()`
    - INSERT: `auth.uid() = profile_id`
    - UPDATE: `(auth.uid() = profile_id) OR is_admin()`
    - DELETE: `(auth.uid() = profile_id) OR is_admin()`
  - `carts` & `cart_items`:
    - Enforces ownership through `carts.profile_id = auth.uid()` across all operations.
  - `orders`:
    - SELECT: `(auth.uid() = customer_id) OR ((auth.uid() = delivery_partner_id) AND is_delivery_or_admin()) OR is_admin()`
    - INSERT: `(auth.uid() = customer_id) OR is_admin()`
    - UPDATE: `is_admin() OR ((auth.uid() = delivery_partner_id) AND is_delivery_or_admin())`
    - DELETE: `is_admin()`
  - `order_items`:
    - SELECT: Allowed for owning customer (`orders.customer_id = auth.uid()`), assigned delivery partner, or admin.
    - INSERT: Allowed for owning customer or admin.
    - UPDATE/DELETE: Restricted to `is_admin()`.
  - `order_status_history`:
    - SELECT: Customer views own orders; assigned delivery partner views assigned orders; admin views all.
    - INSERT: Admin, or assigned delivery partner with `profiles.role = 'delivery'`.
    - ALL: Admin only.
  - `delivery_locations`:
    - SELECT: Customer views tracking for their own order (`orders.customer_id = auth.uid()`); assigned delivery partner views own; admin views all.
    - ALL: `((auth.uid() = delivery_partner_id) AND is_delivery_or_admin())` or `is_admin()`.
  - `products`, `categories`, `promotions`:
    - Public read restricted to `active = true` (and valid date range for promotions); admin views all.
    - Writes strictly restricted to `is_admin()`.
  - `coupons`, `business_settings`, `delivery_settings`:
    - Public/authenticated read allowed for active rows; writes restricted to `is_admin()`.
- **Vulnerabilities Checked:**
  - Anonymous privileged writes: **BLOCKED**
  - Customer modifying another customer's profile/address/order: **BLOCKED**
  - Unauthenticated access to tracking/addresses: **BLOCKED**

## 9. Cart schema
- **Tables:** `public.carts` and `public.cart_items`.
- **Multi-Experience Support:**
  - `carts.experience_type` (`TEXT NOT NULL DEFAULT 'FRESH'`)
  - `cart_items.experience_type` (`TEXT NOT NULL DEFAULT 'FRESH'`)
- **Foreign Keys:**
  - `cart_items.cart_id -> carts(id) ON DELETE CASCADE`
  - `cart_items.product_id -> products(id) ON DELETE CASCADE`
  - `carts.profile_id -> profiles(id) ON DELETE CASCADE`
- **Unique Constraints:**
  - `cart_items_unique_product_exp` ON `cart_items(cart_id, product_id, experience_type)`
  - `carts_profile_experience_unique` ON `carts(profile_id, experience_type)`

## 10. Order RPC
- **Function Name:** `public.create_order_and_decrement_stock`
- **Exact Live Signature:**
  ```sql
  create_order_and_decrement_stock(
    p_customer_id uuid,
    p_subtotal numeric,
    p_discount numeric DEFAULT 0,
    p_coupon_id uuid DEFAULT NULL::uuid,
    p_delivery_charge numeric DEFAULT 0,
    p_total numeric DEFAULT 0,
    p_payment_method text DEFAULT 'COD'::text,
    p_items jsonb DEFAULT '[]'::jsonb,
    p_savings numeric DEFAULT 0,
    p_address_snapshot jsonb DEFAULT NULL::jsonb,
    p_experience_type text DEFAULT 'MIXED'::text,
    p_tax numeric DEFAULT 0,
    p_address_id uuid DEFAULT NULL::uuid
  ) RETURNS uuid
  LANGUAGE plpgsql SECURITY DEFINER SET search_path TO 'public'
  ```
- **Security Check:** Validates `auth.uid() = p_customer_id OR is_admin()`. Rejects anonymous calls.
- **Stock Decrement & Validation:** Locks products with `SELECT ... FROM public.products WHERE id = v_product_id FOR UPDATE`, validates `stock_quantity >= quantity`, and decrements stock.
- **Historical Snapshots:** Populates `orders` (`subtotal, discount, coupon_id, delivery_charge, total, savings, address_snapshot, address_id, dest_latitude, dest_longitude, payment_method, payment_status, order_status='PLACED', experience_type`) and `order_items` (`experience_type, base_price, bulk_discount, promotion_discount, coupon_discount, effective_unit_price, line_total`).
- **Cart Cleanup:** Automatically deletes user's server-side `cart_items` upon order creation.

## 11. Order history
- **Audit Table:** `public.order_status_history`
- **Columns:** `id, order_id, status, updated_by, notes, created_at, changed_by, changed_at, note`
- **Automated Trigger:** `on_order_status_change` fires `AFTER INSERT OR UPDATE ON public.orders FOR EACH ROW EXECUTE FUNCTION log_order_status_change()`.
- **Live Verification:** Tested transition `PLACED -> READY_FOR_DELIVERY -> OUT_FOR_DELIVERY`. Trigger logged 3 status change entries with timestamps and actor IDs.
- **Realtime Integration:** Table is published in `supabase_realtime`.

## 12. Delivery tracking
- **Table:** `public.delivery_locations`
- **Columns:** `id, order_id, delivery_partner_id, latitude, longitude, accuracy, speed, heading, estimated_distance_m, estimated_time_sec, created_at, updated_at`
- **Unique Constraint:** `delivery_locations_order_id_key UNIQUE (order_id)`
- **Function:** `public.update_delivery_location(p_order_id uuid, p_lat numeric, p_lng numeric, p_accuracy numeric DEFAULT NULL, p_speed numeric DEFAULT NULL, p_heading numeric DEFAULT NULL) RETURNS void SECURITY DEFINER SET search_path TO 'public'`
- **Validation:** Only assigned delivery partner (`orders.delivery_partner_id = auth.uid()`) or admin can execute. Unassigned partners or customers are rejected.
- **ETA & Distance Calculation:** Computes haversine distance between partner `(p_lat, p_lng)` and customer `(dest_latitude, dest_longitude)`, calculates ETA based on speed, and upserts `delivery_locations`.
- **Canonical Status Vocabulary in `orders_order_status_check`:**
  - `PLACED`
  - `PENDING`
  - `CONFIRMED`
  - `PREPARING`
  - `READY_FOR_DELIVERY`
  - `OUT_FOR_DELIVERY`
  - `DELIVERED`
  - `CANCELLED`

## 13. Storage
- **Buckets Present in Live Storage:**
  1. `avatars` (`public: true`)
  2. `categories` (`public: true`)
  3. `products` (`public: true`)
  4. `Products` (`public: true` — duplicate legacy bucket with capital P)
  5. `promotions` (`public: true`)
  6. `Banners` (`public: true`)
- **Storage Policies on `storage.objects`:**
  - `Public Access`: Read allowed on `'products'`, `'categories'`, `'promotions'`, `'avatars'`.
  - `Admin Upload Access`: Insert allowed only for `profiles.role = 'admin'` on `'products'`, `'categories'`, `'promotions'`.
  - `Admin Update Access`: Update allowed only for `profiles.role = 'admin'` on `'products'`, `'categories'`, `'promotions'`.
  - `Admin Delete Access`: Delete allowed only for `profiles.role = 'admin'` on `'products'`, `'categories'`, `'promotions'`.
  - `User Avatar Upload`: Insert allowed for authenticated user into `'avatars'` matching their user folder.

## 14. Secret audit
- **Codebase Secret Scan:**
  | File | Secret Type | Status |
  | :--- | :--- | :--- |
  | `seed.js`, `check.mjs`, `count.js`, `test-cat.mjs`, `test-insert.mjs`, `test-read.mjs`, `test-rpc.js` | `process.env.SUPABASE_SERVICE_ROLE_KEY` reference | **REMOVED** from hardcode; reads from env |
  | `admin/.env.local` | `SUPABASE_SERVICE_ROLE_KEY` (local development) | **IGNORED** by `.gitignore` (not committed) |
  | `android/local.properties` | `SUPABASE_ANON_KEY` (public client key) | **IGNORED** by `.gitignore` (not committed) |
  | `docs/baseline/phase-0/repository-state.txt` | Text reference to keyword `service_role` | **DOCUMENTATION** (no secret values) |
  | Tracked repository files | `sbp_` personal access tokens | **ZERO FOUND** |
  | Tracked repository files | `service_role` JWTs | **ZERO FOUND** |
- **Git Ignore Verification:**
  - Root `.gitignore` explicitly ignores `.env`, `.env.local`, `.env.*.local`, `android/local.properties`, `android/.kotlin/`, `android/app/release.keystore`, and `skills-lock.json`.
  - `admin/.gitignore` explicitly ignores `.env*`.
  - No secrets tracked in Git.

## 15. Data consistency
- **Current Live Production Counts:**
  - `auth.users`: `2`
  - `public.profiles`: `2`
  - `public.products`: `85` (all `active = true`, `experience_type = 'FRESH'`, `product_type = 'SINGLE'`, stock 50–500; 79 have `bundle_items` JSON)
  - `public.categories`: `16` (all `active = true`, `experience_type = 'FRESH'`)
  - `public.product_images`: `79`
  - `public.delivery_settings`: `1`
  - `public.orders`: `0`
  - `public.order_items`: `0`
  - `public.order_status_history`: `0`
  - `public.delivery_locations`: `0`
  - `public.carts`: `0`
  - `public.cart_items`: `0`
  - `public.addresses`: `0`
  - `public.favorites`: `0`
  - `public.business_profiles`: `0`
  - `public.business_settings`: `0`
  - `public.kitchen_variations`: `0`
  - `public.coupons`: `0`
  - `public.promotions`: `0`

## 16. Security test results
All tests were executed against the live database using a transaction-isolated rollback block (`DO $test$ ... RAISE EXCEPTION 'ROLLBACK_TEST_RESULTS:...'`), ensuring **zero permanent mutations** occurred in production data.

| Test Case | Scenario Tested | Expected Behavior | Actual Live Behavior | Result |
| :--- | :--- | :--- | :--- | :--- |
| **TEST A** | Customer attempts self-escalation (`UPDATE profiles SET role = 'admin'`) | **DENIED** | Blocked by `prevent_profile_role_escalation_trigger` (`"Unauthorized: cannot change profile role"`) | **PASS** |
| **TEST B** | Customer attempts to alter another customer's profile | **DENIED** | Blocked by RLS (`0 rows updated`) | **PASS** |
| **TEST C** | Customer attempts to update/delete another customer's address | **DENIED** | Blocked by RLS (`0 rows modified/deleted`) | **PASS** |
| **TEST D** | Anonymous user attempts to update product or insert category | **DENIED** | Product update blocked (`0 rows updated`), Category insert blocked by RLS (`"new row violates row-level security policy for table 'categories'"`) | **PASS** |
| **TEST E1** | Customer attempts to update own order status to `DELIVERED` | **DENIED** | Blocked by `enforce_order_update_permissions_trigger` (`0 rows updated`) | **PASS** |
| **TEST E2** | Unassigned Delivery Partner attempts to update unassigned order status | **DENIED** | Blocked by `enforce_order_update_permissions_trigger` (`0 rows updated`) | **PASS** |
| **TEST F1** | Customer attempts to call `update_delivery_location` | **DENIED** | Blocked by `update_delivery_location` (`"Unauthorized: only the assigned delivery partner or an admin can update delivery location"`) | **PASS** |
| **TEST F2** | Unassigned Delivery Partner attempts to call `update_delivery_location` on unassigned order | **DENIED** | Blocked by `update_delivery_location` (`"Unauthorized: only the assigned delivery partner or an admin can update delivery location"`) | **PASS** |
| **BONUS 1** | Assigned Delivery Partner updates assigned order status to `OUT_FOR_DELIVERY` | **ALLOWED** | Updated successfully | **PASS** |
| **BONUS 2** | Assigned Delivery Partner attempts to tamper with order total/price | **DENIED** | Blocked by `enforce_order_update_permissions_trigger` (`"Unauthorized: delivery partners may only update delivery status on assigned orders"`) | **PASS** |
| **BONUS 3** | Assigned Delivery Partner updates location via `update_delivery_location` | **ALLOWED** | Upserted `delivery_locations` with calculated distance & ETA | **PASS** |
| **BONUS 4** | Order status lifecycle tracking via `on_order_status_change` trigger | **LOGGED** | Recorded `3` entries (`PLACED` -> `READY_FOR_DELIVERY` -> `OUT_FOR_DELIVERY`) | **PASS** |

## 17. Remaining issues
1. **Migration Tracking Table:** `supabase_migrations.schema_migrations` contains 0 rows because migrations were applied through Dashboard SQL Editor instead of Supabase CLI.
2. **Table Schema Mismatch on `product_bulk_tiers`:** Table `product_bulk_tiers` does not exist in live DB. Wholesale bulk pricing is currently stored in `products.bundle_items->'wholesale_pricing'`. If downstream phases require relational bulk pricing, a clean migration should create it.
3. **Duplicate Storage Bucket:** Both `products` and `Products` exist in Supabase Storage. RLS storage policies correctly target lowercase `'products'`.
4. **Redundant Constraint:** `public.carts` contains two duplicate unique constraints on `(profile_id, experience_type)`. Non-breaking.
5. **Downstream Role Alignment:** Live database uses lowercase roles (`'customer'`, `'admin'`, `'delivery'`). When implementing Android and Web frontend features in later phases, code must align with lowercase strings.

## 18. User action required
- **None for Phase 1.** No manual database migration or hotfix is required to complete Phase 1.
- All database security triggers, RLS policies, order creation RPCs, and delivery location tracking functions are operational and verified.

## 19. Final Phase 1 verdict
### Subsystem Classification:
- **[PASS]** migrations (All intended foundation & security objects exist live in Postgres)
- **[PASS]** profiles (Parity verified, 100% matched with auth.users)
- **[PASS]** auth trigger (New user trigger creates customer profile automatically)
- **[PASS]** roles (Canonical lowercase `'customer'`, `'admin'`, `'delivery'`, no mixed casing)
- **[PASS]** RLS (Enabled on all 18 tables, verified via security boundary tests)
- **[PASS]** addresses (Strict ownership and RLS enforced)
- **[PASS]** cart schema (Unified cart with experience_type verified)
- **[PASS]** order schema (Full preservation of items, snapshots, and experience_type)
- **[PASS]** order RPC (13-param `create_order_and_decrement_stock` verified)
- **[PASS]** order history (Trigger `on_order_status_change` verified with realtime)
- **[PASS]** delivery tracking (Function `update_delivery_location` & permissions verified)
- **[PASS]** storage (Buckets and RLS object policies verified)
- **[PASS]** secrets (No secrets in tracked code; gitignore verified)
- **[PASS]** live data consistency (Zero corruption, zero orphaned rows)

---

### **PHASE 1 = PASS**
The database foundation, schema integrity, and security policies of the GOVIND Supabase project (`crkuiuxajywlgmlnklvj`) are fully recovered, verified live, and ready for Phase 2.
