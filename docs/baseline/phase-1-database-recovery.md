# GOVIND — PHASE 1 DATABASE FOUNDATION & SECURITY RECOVERY REPORT

- **Timestamp:** `2026-09-29T16:47:00+05:30`
- **Project Root:** `C:\Web Apps\Govind`
- **Reference Project (Read-Only, Untouched):** `C:\Web Apps\Sardar ji`
- **Supabase Project Ref:** `crkuiuxajywlgmlnklvj` (`https://crkuiuxajywlgmlnklvj.supabase.co`)
- **Phase 1 Status:** **PASS**

---

## 1. Pre-Phase-1 Database Snapshot Confirmation
- Prior to Phase 1 schema/data operations, a full non-secret live database snapshot (`supabase-live-data-snapshot.json`) and source archive (`govind-phase-0-source-snapshot.zip`) were captured and verified in [`docs/baseline/phase-0/`](file:///C:/Web%20Apps/Govind/docs/baseline/phase-0/phase-0-baseline.md).
- Pre-repair row counts confirmed:
  - `categories`: `16`
  - `products`: `85`
  - `product_images`: `79`
  - `delivery_settings`: `1`
  - `auth.users`: `2`
  - `public.profiles`: `0` (prior to Phase 1 backfill)

---

## 2. Local Migration Inventory (`supabase/migrations/`)

| # | Migration File | Purpose | Pre-Phase-1 State | Post-Phase-1 State |
|---|---|---|---|---|
| 1 | [`20260918000000_initial_schema.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260918000000_initial_schema.sql) | Initial core schema (`profiles`, `addresses`, `categories`, `products`, `product_images`, `favorites`, `carts`, `cart_items`, `coupons`, `orders`, `order_items`, `promotions`, `delivery_settings`, `business_settings`) | Applied | Applied & Valid |
| 2 | [`20260918000001_seed_data.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260918000001_seed_data.sql) | Initial seed data for categories, products, coupons, delivery settings | Applied | Applied & Valid |
| 3 | [`20260925000000_final_scope.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260925000000_final_scope.sql) | Adds `featured`, `best_selling`, `min_order_quantity` to `products` | Applied | Applied & Valid |
| 4 | [`20260925000001_backend_logic_and_rls.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260925000001_backend_logic_and_rls.sql) | Storage buckets, initial `create_order_and_decrement_stock`, `is_admin()`, initial RLS policies | Applied | Applied & Valid |
| 5 | [`20260926000000_multi_experience_schema.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260926000000_multi_experience_schema.sql) | Multi-experience columns on `products`, `carts`, `orders`, and `product_bulk_tiers` table | Applied | Applied & Valid |
| 6 | [`20260926000001_design_bible_schema.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260926000001_design_bible_schema.sql) | Adds `on_fresh_board`, `product_type`, `bundle_items` to `products` | Applied | Applied & Valid |
| 7 | [`20260927000000_catalogue_updates.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000000_catalogue_updates.sql) | Adds `subcategory_id`, `image_url`, `gallery_images`, `exotic`, `premium`, `short_description` to `products` | Applied | Applied & Valid |
| 8 | [`20260927000001_test_sync.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000001_test_sync.sql) | Temporary debug function `test_sync()` | Applied manually; had invalid `$$$` syntax locally | Repaired `$$` syntax; dropped in `20260929000000` |
| 9 | [`20260927000002_unified_cart.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000002_unified_cart.sql) | Unified cart schema & `create_order_and_decrement_stock` update | Applied manually; had invalid `$$$` syntax & missed dropping `cart_items_cart_id_product_id_key` | Repaired `$$` syntax, drops legacy constraint, clears cart |
| 10 | [`20260927000003_delivery_tracking.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000003_delivery_tracking.sql) | `delivery_locations` table, delivery columns on `orders`/`addresses`, `update_delivery_location` RPC | Unapplied; had uppercase role conflicts & missing auth check | Repaired canonical roles & auth checks; applied via Phase 1 |
| 11 | [`20260927000004_order_history.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000004_order_history.sql) | `order_status_history` table & `on_order_status_change` trigger | Unapplied; had uppercase role conflicts & missing `'PLACED'` status | Repaired canonical columns/roles/statuses; applied via Phase 1 |
| 12 | [`20260929000000_phase1_foundation_and_security.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260929000000_phase1_foundation_and_security.sql) | Authoritative Phase 1 foundation, schema sync, trigger, backfill, RLS hardening, RPCs, and cleanup | Newly created in Phase 1 | Applied to live DB & verified |

---

## 3. Live Schema Drift Found Before Repair
1. **Unapplied Migrations (`20260927000003` & `20260927000004`):**
   - `public.delivery_locations` and `/rpc/update_delivery_location` did not exist in the live database.
   - `public.order_status_history` and trigger `on_order_status_change` did not exist in the live database.
   - `public.orders` was missing `delivery_partner_id`, `dest_latitude`, `dest_longitude`, and `address_id`.
   - `public.addresses` was missing `latitude` and `longitude`.
2. **Broken `auth.users` -> `public.profiles` Synchronization:**
   - No `AFTER INSERT` trigger existed on `auth.users`.
   - `auth.users` had `2` users while `public.profiles` had `0` rows.
3. **Critical RLS & Privilege Escalation Vulnerabilities:**
   - `public.profiles` allowed any authenticated customer to run `UPDATE public.profiles SET role = 'admin' WHERE id = auth.uid()`.
   - `public.coupons` and `public.delivery_settings` had RLS disabled (`ALTER TABLE ... ENABLE ROW LEVEL SECURITY` was never executed), allowing `anon` users to insert coupons and modify delivery charges.
4. **Conflicting `cart_items` Uniqueness Constraint:**
   - The legacy `cart_items_cart_id_product_id_key` (`UNIQUE (cart_id, product_id)`) from `20260918000000_initial_schema.sql` remained active on the live database alongside `cart_items_unique_product_exp`, blocking customers from adding the same product under different `experience_type`s (`FRESH` vs `WHOLESALE`).
5. **Debug Function Exposure:**
   - `public.test_sync()` was publicly callable by `anon`.

---

## 4. SQL Syntax Repairs Performed
- **[`20260927000001_test_sync.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000001_test_sync.sql):** Replaced invalid `AS $$$ ... $$$` dollar-quoting with valid `AS $$ ... $$`.
- **[`20260927000002_unified_cart.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000002_unified_cart.sql):** Replaced invalid `AS $$$ ... $$$;` dollar-quoting with valid `AS $$ ... $$;`, added `ALTER TABLE public.cart_items DROP CONSTRAINT IF EXISTS cart_items_cart_id_product_id_key;`, and restored cart clearing on order creation.
- **[`20260927000003_delivery_tracking.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000003_delivery_tracking.sql) & [`20260927000004_order_history.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260927000004_order_history.sql):** Repaired role constraints/policies to use canonical lowercase roles (`'customer'`, `'admin'`, `'delivery'`), added `IF NOT EXISTS` / `DROP POLICY IF EXISTS` idempotency guards, and aligned status constraints.

---

## 5. New / Updated Migration Files Created
- Created [`supabase/migrations/20260929000000_phase1_foundation_and_security.sql`](file:///C:/Web%20Apps/Govind/supabase/migrations/20260929000000_phase1_foundation_and_security.sql) (idempotent forward migration consolidating all Phase 1 schema, trigger, backfill, RLS, and RPC requirements) and executed it on the live Supabase database.

---

## 6. Canonical Role Standard Enforced
- **Canonical Values:** `'customer'`, `'admin'`, `'delivery'`
- **Default Role:** `'customer'` (`NOT NULL`)
- **Constraint:** `profiles_role_check CHECK (role IN ('customer', 'admin', 'delivery'))`
- **Helper Functions (`SECURITY DEFINER`, `SET search_path = public`):**
  - `public.get_my_role()`
  - `public.is_admin()`
  - `public.is_delivery_or_admin()`

---

## 7. Canonical Order Status Standard Enforced
- **Canonical Order Lifecycle:**
  1. `'PLACED'` (Default initial status on order creation)
  2. `'CONFIRMED'`
  3. `'PREPARING'`
  4. `'READY_FOR_DELIVERY'`
  5. `'OUT_FOR_DELIVERY'`
  6. `'DELIVERED'`
  7. `'CANCELLED'`
- **Transitional Alias Supported in Check Constraint:** `'PENDING'` is permitted in the `CHECK` constraint of both `public.orders.order_status` and `public.order_status_history.status` for backward compatibility with existing client code prior to Phase 4/5 UI alignment, ensuring zero constraint mismatches between `orders` and `order_status_history`.

---

## 8. Canonical Experience Type Standard Enforced
- **Item/Cart/Product Experience Types (`public.products`, `public.carts`, `public.cart_items`, `public.order_items`):**
  - `'FRESH'`, `'KITCHEN'`, `'WHOLESALE'`
- **Order-Level Experience Types (`public.orders.experience_type`):**
  - `'FRESH'`, `'KITCHEN'`, `'WHOLESALE'`, `'MIXED'` (Default: `'MIXED'`)

---

## 9. `auth.users` -> `public.profiles` Trigger & Backfill Verification
- **Trigger Function:** `public.handle_new_user()` (`SECURITY DEFINER SET search_path = public`)
- **Trigger:** `on_auth_user_created AFTER INSERT ON auth.users FOR EACH ROW EXECUTE FUNCTION public.handle_new_user()`
- **Backfill Result:**
  - Pre-Phase-1: `auth.users = 2`, `public.profiles = 0`
  - Post-Phase-1: `auth.users = 2`, `public.profiles = 2` (`d2129669-fbb7-4b28-adb1-cda1fbbc2598` and `fc9c7bfa-1501-4a17-8d10-575ee68236e8`, both with `role = 'customer'` and non-null `name` / `full_name`)
- **Live Trigger Verification:** Created a temporary user via `auth.admin.createUser`, verified automatic `public.profiles` row creation with `role = 'customer'`, and deleted the temporary user.

---

## 10. `public.profiles` Role-Escalation Fix Verification
- **Defense-in-Depth:**
  1. **RLS Policy `"Profiles insert"`:** `WITH CHECK (auth.uid() = id AND role = 'customer')`
  2. **RLS Policy `"Profiles update own"`:** `USING (auth.uid() = id) WITH CHECK (auth.uid() = id AND role = public.get_my_role())`
  3. **Trigger `prevent_profile_role_escalation_trigger` (`BEFORE UPDATE ON public.profiles`):** Raises `Unauthorized: cannot change profile role` if `NEW.role IS DISTINCT FROM OLD.role` and the caller is not an `'admin'` or `service_role`.
- **Live Test Result:** Authenticated customer attempting `UPDATE public.profiles SET role = 'admin'` failed with `Unauthorized: cannot change profile role`, and `role` remained `'customer'`.

---

## 11. `coupons` and `delivery_settings` RLS Verification
- Executed `ALTER TABLE public.coupons ENABLE ROW LEVEL SECURITY;` and `ALTER TABLE public.delivery_settings ENABLE ROW LEVEL SECURITY;`.
- Configured read policies for active rows (`active = TRUE OR public.is_admin()`) and admin-only write policies (`public.is_admin()`).
- **Live Test Result:** `anon` and `customer` inserts/updates on `public.coupons` and `public.delivery_settings` were rejected by RLS (`new row violates row-level security policy`), while public `SELECT` succeeded.

---

## 12. `cart_items` Uniqueness Verification
- Dropped legacy `cart_items_cart_id_product_id_key` (`UNIQUE (cart_id, product_id)`).
- Enforced `cart_items_unique_product_exp UNIQUE (cart_id, product_id, experience_type)`.
- **Live Test Result:** Inserting the same `product_id` into the same `cart_id` once with `experience_type = 'FRESH'` and once with `experience_type = 'WHOLESALE'` succeeded; inserting a duplicate `(cart_id, product_id, 'FRESH')` was rejected with SQLSTATE `23505`.

---

## 13. `create_order_and_decrement_stock` Verification
- Upgraded `public.create_order_and_decrement_stock` to:
  1. Verify caller identity (`auth.uid() = p_customer_id` or `public.is_admin()` or `service_role`; rejects `anon` and cross-customer spoofing).
  2. Accept both Next.js `/api/checkout` parameters (`p_savings`, `p_address_snapshot`, `p_experience_type`) and Android parameters (`p_tax`, `p_address_id`) via safe defaults.
  3. Lock product rows `FOR UPDATE`, verify `stock_quantity >= quantity`, and decrement `stock_quantity` atomically.
  4. Insert `public.orders` with canonical `order_status = 'PLACED'` and `public.order_items` with full price snapshot columns (`base_price`, `bulk_discount`, `promotion_discount`, `coupon_discount`, `effective_unit_price`, `line_total`, `experience_type`).
  5. Clear the customer's server-side cart items in `public.cart_items` atomically.
- **Live Test Result:** Verified cross-user spoofing rejection, insufficient-stock rejection, valid order creation, stock decrement (`100 -> 98`), and cart item clearing (`0` remaining).

---

## 14. `delivery_locations` and `update_delivery_location` Verification
- Created `public.delivery_locations` with RLS enabled and added to `supabase_realtime` publication.
- Created `public.update_delivery_location` RPC (`SECURITY DEFINER`) with authorization check (only assigned delivery partner with `role = 'delivery'`, `'admin'`, or `service_role`) and clamped Haversine distance + ETA calculation.
- **Live Test Result:** Unauthorized customer call was rejected; assigned delivery partner call succeeded and populated `estimated_distance_m = 1033` and `estimated_time_sec = 129`; customer owning the order could read the tracking row while another customer received `0` rows.

---

## 15. `order_status_history` and Trigger Verification
- Created `public.order_status_history` (`id`, `order_id`, `status`, `updated_by`, `notes`, `created_at`, plus compatibility columns `changed_by`, `changed_at`, `note`) with RLS enabled and added to `supabase_realtime` publication.
- Created trigger `on_order_status_change AFTER INSERT OR UPDATE ON public.orders` calling `public.log_order_status_change()`.
- **Live Test Result:** Order creation and status updates (`PLACED -> CONFIRMED -> OUT_FOR_DELIVERY -> DELIVERED`) were automatically logged in `public.order_status_history` and readable by the order's customer.

---

## 16. RLS Policy Matrix Across Core Tables

| Table | RLS Enabled | Anon Access | Customer Access | Delivery Partner Access | Admin Access |
|---|---|---|---|---|---|
| `public.profiles` | Yes | None | `SELECT`/`INSERT`/`UPDATE` own row (`role` locked to `'customer'`) | `SELECT`/`UPDATE` own row (`role` locked to `'delivery'`) | Full CRUD (`is_admin()`) |
| `public.addresses` | Yes | None | Full CRUD on own rows (`profile_id = auth.uid()`) | Own rows only | Full CRUD (`is_admin()`) |
| `public.categories` | Yes | `SELECT` active rows | `SELECT` active rows | `SELECT` active rows | Full CRUD (`is_admin()`) |
| `public.products` | Yes | `SELECT` active rows | `SELECT` active rows | `SELECT` active rows | Full CRUD (`is_admin()`) |
| `public.product_images` | Yes | `SELECT` all | `SELECT` all | `SELECT` all | Full CRUD (`is_admin()`) |
| `public.product_bulk_tiers` | Yes | `SELECT` active rows | `SELECT` active rows | `SELECT` active rows | Full CRUD (`is_admin()`) |
| `public.carts` | Yes | None | Full CRUD on own rows (`profile_id = auth.uid()`) | Own rows only | `SELECT`/`DELETE` all (`is_admin()`) |
| `public.cart_items` | Yes | None | Full CRUD on items in own cart | Own cart items only | `SELECT`/`DELETE` all (`is_admin()`) |
| `public.orders` | Yes | None | `SELECT`/`INSERT` own orders (`customer_id = auth.uid()`) | `SELECT`/`UPDATE` assigned orders (`delivery_partner_id = auth.uid()`, status-only guard) | Full CRUD (`is_admin()`) |
| `public.order_items` | Yes | None | `SELECT`/`INSERT` items for own orders | `SELECT` items for assigned orders | Full CRUD (`is_admin()`) |
| `public.delivery_locations` | Yes | None | `SELECT` tracking for own orders | Full CRUD on own assigned location (`delivery_partner_id = auth.uid()`) | Full CRUD (`is_admin()`) |
| `public.order_status_history` | Yes | None | `SELECT` history for own orders | `SELECT`/`INSERT` history for assigned orders | Full CRUD (`is_admin()`) |
| `public.coupons` | Yes | `SELECT` active rows | `SELECT` active rows | `SELECT` active rows | Full CRUD (`is_admin()`) |
| `public.promotions` | Yes | `SELECT` active current rows | `SELECT` active current rows | `SELECT` active current rows | Full CRUD (`is_admin()`) |
| `public.delivery_settings` | Yes | `SELECT` active rows | `SELECT` active rows | `SELECT` active rows | Full CRUD (`is_admin()`) |
| `public.business_settings` | Yes | `SELECT` all | `SELECT` all | `SELECT` all | Full CRUD (`is_admin()`) |

---

## 17. Realtime Publication Status
- `supabase_realtime` publication includes:
  - `public.orders`
  - `public.delivery_locations`
  - `public.order_status_history`

---

## 18. Secret Hygiene Cleanup Summary
- Removed hardcoded `SUPABASE_SERVICE_ROLE_KEY` JWT strings from all 7 root utility scripts and updated them to read from environment variables via `dotenv`:
  - [`seed.js`](file:///C:/Web%20Apps/Govind/seed.js)
  - [`check.mjs`](file:///C:/Web%20Apps/Govind/check.mjs)
  - [`count.js`](file:///C:/Web%20Apps/Govind/count.js)
  - [`test-cat.mjs`](file:///C:/Web%20Apps/Govind/test-cat.mjs)
  - [`test-insert.mjs`](file:///C:/Web%20Apps/Govind/test-insert.mjs)
  - [`test-read.mjs`](file:///C:/Web%20Apps/Govind/test-read.mjs)
  - [`test-rpc.js`](file:///C:/Web%20Apps/Govind/test-rpc.js)
- Updated [`.gitignore`](file:///C:/Web%20Apps/Govind/.gitignore) to ignore `.env`, `.env.local`, `.env.*.local`, and `android/local.properties` while keeping `!.env.example`.
- Updated [`.env.example`](file:///C:/Web%20Apps/Govind/.env.example) with placeholder keys (`SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`).
- Removed public debug RPC `public.test_sync()` from the live database.

---

## 19. Live Verification Test Results (`PASS` / `FAIL` / `BLOCKED`)

| Test # | Verification Item | Status | Evidence |
|---|---|---|---|
| **Test 1** | Profile Auto-Creation (`on_auth_user_created`) & Backfill (`2/2` users) | **PASS** | `auth.users` (`2`) == `public.profiles` (`2`); temp auth user auto-created profile with `role = 'customer'` |
| **Test 2** | Role Escalation Prevention (`public.profiles`) | **PASS** | Customer `UPDATE role = 'admin'` rejected with `Unauthorized: cannot change profile role`; `role` stayed `'customer'` |
| **Test 3** | `coupons` and `delivery_settings` RLS Enforcement | **PASS** | `anon` & `customer` writes rejected with `new row violates row-level security policy`; public `SELECT` succeeded |
| **Test 4** | Multi-Experience Cart Uniqueness `(cart_id, product_id, experience_type)` | **PASS** | Same product inserted in `FRESH` + `WHOLESALE` succeeded; duplicate `FRESH` rejected (`23505`) |
| **Test 5** | Order Creation, Snapshot, Stock Decrement & Cart Clear (`create_order_and_decrement_stock`) | **PASS** | Cross-user spoofing & OOS rejected; valid order created (`PLACED`), stock decremented (`100 -> 98`), cart cleared (`0` items) |
| **Test 6** | Delivery Tracking (`delivery_locations`, `update_delivery_location`) & `order_status_history` | **PASS** | Unassigned user blocked; assigned `'delivery'` partner updated location (`1033m`, `129s`); status history logged `PLACED -> CONFIRMED -> OUT_FOR_DELIVERY -> DELIVERED` |
| **Test 7** | `test_sync()` Debug Function Removed | **PASS** | `anon` RPC call to `test_sync` returned `PGRST202` (function removed) |

---

## 20. Remaining Blockers Before Phase 2
- **None.** The database foundation, schema synchronization, RLS policies, triggers, and RPCs are 100% live and verified. Ready to proceed to **PHASE 2 (Authentication, Session Persistence, Profile & Address Recovery)**.
