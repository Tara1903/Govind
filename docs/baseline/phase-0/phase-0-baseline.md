# GOVIND — PHASE 0 BASELINE REPORT

## 1. PHASE 0 SUMMARY
- **Date / Time Recorded:** `2026-09-29T15:56:00+05:30`
- **Project Root:** `C:\Web Apps\Govind`
- **Read-Only Reference Project:** `C:\Web Apps\Sardar ji` (Untouched — zero reads/writes/modifications performed)
- **Overall Baseline Status:** **COMPLETE & VERIFIED**
- **Phase 1 Readiness Verdict:** **READY FOR PHASE 1 (Database, Security & Auth Foundation)**

---

## 2. REPOSITORY & GIT BASELINE
- **Git Initialized:** YES (`C:\Web Apps\Govind\.git`)
- **Current Branch:** `main` (tracking `origin/main` -> `https://github.com/Tara1903/Govind.git`)
- **Current HEAD Commit:** `c1ad2351f278d9199498efb7364dc05e4b070692` (`feat(admin): sync products page with latest mobile app fields`)
- **Working Tree Status:** **DIRTY**
  - **Modified Tracked Files:** `43` files (`4` in `admin/src`, `39` in `android/`)
  - **Untracked Files / Folders:** `46` entries (including `admin/src/app/{api,deliveries,login,orders/[id]}`, `android/` tracking/address/ViewModel files, `supabase/migrations/20260927000000..0004`, `test_screens/`, root `node_modules/`, and root helper scripts `seed.js`, `check.mjs`, `count.js`, `test-*.mjs`)
- **Git Checkpoint Commit Status:**
  > **Git checkpoint not created — working tree contains uncommitted changes.**
  - **Rationale:** Root `.gitignore` does not ignore root `node_modules/` or the 7 untracked root helper scripts (`seed.js`, `check.mjs`, `count.js`, `test-cat.mjs`, `test-insert.mjs`, `test-read.mjs`, `test-rpc.js`) that contain the live Supabase `service_role` JWT in plaintext. Per Phase 0 rules (*"Never commit secrets"*, *"Do NOT modify .gitignore in Phase 0"*, and *"If committing is not safe because of unresolved working-tree conditions, DO NOT force it"*), a clean local source archive (`govind-phase-0-source-snapshot.zip`) was created instead.
- **Full Details:** See [repository-state.txt](file:///C:/Web%20Apps/Govind/docs/baseline/phase-0/repository-state.txt).

---

## 3. BACKUP / SNAPSHOT STATUS
- **Local Source Snapshot Archive:**
  - **Path:** `C:\Web Apps\Govind\docs\baseline\phase-0\govind-phase-0-source-snapshot.zip`
  - **Contents:** Complete `android/` source & Gradle config, `admin/` source & Next.js config, `supabase/` migrations/functions/seeds, and root manifests (excluding `node_modules`, `.gradle`, `build`, `.next`, `.git`, and secret files).
- **Supabase Live Data Snapshot:**
  - **Path:** `C:\Web Apps\Govind\docs\baseline\phase-0\supabase-live-data-snapshot.json`
  - **Contents:** Complete read-only JSON export of all 4 populated public tables (`categories`: 16 rows, `products`: 85 rows, `product_images`: 79 rows, `delivery_settings`: 1 row). All other 12 live public tables currently have `0` rows.
  - **Raw `pg_dump` Status:** Not available without direct PostgreSQL password (`SUPABASE_DB_PASSWORD` is not configured locally); fully mitigated by the 10 SQL migration files + `supabase-live-data-snapshot.json`.

---

## 4. BUILD BASELINE

| Target | Location | Command | Result | Artifact / Details |
| :--- | :--- | :--- | :--- | :--- |
| **Android App (Debug)** | `C:\Web Apps\Govind\android` | `.\gradlew.bat assembleDebug` | **PASS** (Exit `0`) | `android/app/build/outputs/apk/debug/app-debug.apk` (`16,299,096` bytes / `15.54 MB`), `minSdk 24`, `targetSdk 36`, `compileSdk 36`, Kotlin `2.0.21`, AGP `8.7.2`, Gradle `9.1.0`, JDK `17.0.20` |
| **Admin Web Panel** | `C:\Web Apps\Govind\admin` | `npm run build` | **PASS** (Exit `0`) | Next.js `16.3.6` (Turbopack), React `19.2.8`, Node `v26.4.0`. Compiles 16 static pages, 2 dynamic routes (`/api/checkout`, `/orders/[id]`), and Proxy middleware (`src/proxy.ts`). |
| **Customer Website** | *None* | N/A | **MISSING** | No separate customer website exists in `C:\Web Apps\Govind`. |

- **Full Details:** See [build-baseline.md](file:///C:/Web%20Apps/Govind/docs/baseline/phase-0/build-baseline.md).

---

## 5. SUPABASE / DATABASE BASELINE
- **Project Reference:** `crkuiuxajywlgmlnklvj` (`https://crkuiuxajywlgmlnklvj.supabase.co`)
- **Live Public Tables (16 Present):**
  - Populated (`4`): `categories` (`16`), `products` (`85`), `product_images` (`79`), `delivery_settings` (`1`)
  - Empty (`12`): `profiles` (`0`), `business_profiles` (`0`), `kitchen_variations` (`0`), `addresses` (`0`), `carts` (`0`), `cart_items` (`0`), `orders` (`0`), `order_items` (`0`), `favorites` (`0`), `coupons` (`0`), `promotions` (`0`), `business_settings` (`0`)
- **Missing Public Tables (Unapplied Migrations `0003` & `0004`):**
  - `public.delivery_locations` (`404 PGRST205`)
  - `public.order_status_history` (`404 PGRST205`)
- **Live RPC Functions (3 Present, 1 Missing):**
  - Present: `public.is_admin()`, `public.create_order_and_decrement_stock(...)`, `public.test_sync()`
  - Missing: `public.update_delivery_location(...)` (`404 PGRST202`)
- **Auth vs. Profiles State:**
  - `auth.users`: `2` users registered (emails redacted)
  - `public.profiles`: `0` rows (`on_auth_user_created` trigger was dropped in `20260925000001_consolidated_schema.sql` and never recreated)
- **Storage Buckets (6 Public Buckets):** `Products`, `Banners`, `products`, `categories`, `promotions`, `avatars`
- **Full Details:** See [supabase-baseline.md](file:///C:/Web%20Apps/Govind/docs/baseline/phase-0/supabase-baseline.md).

---

## 6. ENVIRONMENT & SECRET SAFETY BASELINE
- **Environment Files Present (Not Tracked by Git):**
  - `admin/.env.local`: `NEXT_PUBLIC_SUPABASE_URL` `[SET]`, `NEXT_PUBLIC_SUPABASE_ANON_KEY` `[SET]`, `SUPABASE_SERVICE_ROLE_KEY` `[SET]`, `RAZORPAY_KEY_ID` `[PLACEHOLDER]`, `RAZORPAY_KEY_SECRET` `[PLACEHOLDER]`
  - `android/local.properties`: `sdk.dir` `[SET]`, `SUPABASE_URL` `[SET]`, `SUPABASE_ANON_KEY` `[SET]`, `RAZORPAY_KEY_ID` `[PLACEHOLDER]`
  - `.env` & `.env.example`: All values `[PLACEHOLDER]`
- **Hardcoded Secrets Identified (Values Redacted):**
  - `seed.js`, `check.mjs`, `count.js`, `test-cat.mjs`, `test-insert.mjs`, `test-read.mjs`, `test-rpc.js` — Supabase `service_role` JWT (`FOUND — VALUE REDACTED`)
  - `android/app/build.gradle.kts` — Release keystore password (`FOUND — VALUE REDACTED`)
  - `android/.../SupabaseGovindRepositoryImpl.kt:218` — Hardcoded test API key header (`FOUND — VALUE REDACTED`)
- **Full Details:** See [environment-inventory.md](file:///C:/Web%20Apps/Govind/docs/baseline/phase-0/environment-inventory.md).

---

## 7. FORENSIC REPORT LINK
- **Authoritative 118-Test Forensic Audit Report:**  
  [forensic-test-report.md](file:///C:/Web%20Apps/Govind/docs/baseline/phase-0/forensic-test-report.md) (`14 PASS`, `26 PARTIAL`, `72 FAIL`, `6 BLOCKED`)

---

## 8. KNOWN P0 BLOCKERS (FROM FORENSIC REPORT)
1. **[P0-DB-01] Missing `auth.users` -> `public.profiles` Trigger Breaks Order Creation (`23503` FK Crash):** `auth.users` has `2` users while `public.profiles` has `0` rows, causing `create_order_and_decrement_stock` to fail on `orders_user_id_fkey`.
2. **[P0-DB-02] Migrations `0003` (Delivery Tracking) and `0004` (Order History) Are Unapplied on Live Supabase:** `public.delivery_locations`, `public.order_status_history`, and RPC `public.update_delivery_location` return `404`.
3. **[P0-DB-03] Conflicting Enum / Check Constraints Across Schema, Android, and Admin:**
   - `orders.order_status`: DB check constraint only allows `'PLACED','CONFIRMED','PREPARING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED'`, rejecting `'PENDING'` and `'READY_FOR_DELIVERY'`.
   - `orders.experience_type`: Initial DB check constraint rejects `'UNIFIED'`.
   - `profiles.role`: DB check constraint uses lowercase `'customer','admin','delivery'`, while migration `0003` and Admin use `'CUSTOMER','ADMIN','DELIVERY_PARTNER'`.
4. **[P0-NAV-01] Top Experience Switcher (`PillExperienceSwitcher`) Does Not Switch the Screen:** Tapping `Kitchen` or `Wholesale` on `HomeScreen` updates `AppState.currentExperience` (changing the 3rd bottom-nav icon) but leaves the user on `HomeScreen` (`Fresh`). `KitchenHomeScreen` is completely orphaned.
5. **[P0-CHK-01] Checkout Uses Hardcoded Fake Addresses & Fails to Persist Customer Phone Number:** `CheckoutViewModel` hardcodes two fake addresses (`123 Green Valley...`, `456 Tech Park...`) and never calls `getAddresses()`. `updateUserPhone` uses `PATCH` instead of `UPSERT`, updating `0` rows when `profiles` is missing.
6. **[P0-TRK-01] Android Live Order Status & Delivery Tracking Are 100% Fake Local Loops:** `OrdersViewModel.simulateLiveOrderStatusUpdates()` and `TrackingRepositoryImpl.getDeliveryLocationFlow()` use local `delay()` loops in memory and never query Supabase.
7. **[P0-DELIV-01] `DeliveryPartnerScreen` Is Orphaned and Missing Runtime Location Permission Request:** `DeliveryPartnerScreen` is not registered in `MainAppScreen.kt` `NavHost`, does not request runtime `ACCESS_FINE_LOCATION` permission before starting `DeliveryTrackingService`, and calls `update_delivery_location` with a fake header `X-API-Key: test_api_key_123`.
8. **[P0-SEC-01] Plaintext `service_role` Key in Root Scripts, Missing RLS on `coupons` & `delivery_settings`, Self-Role Escalation on `profiles`, Unprotected Admin `proxy.ts`, and Live `test_sync` RPC.**

---

## 9. KNOWN P1 HIGH-PRIORITY GAPS
1. **[P1-AUTH-01]** Guest mode (`isGuest`) is stored only in memory in `SessionManager.kt`, forcing guests back to `AuthScreen` on every app restart.
2. **[P1-KITCH-01]** `KitchenScreens.kt` merges 8 hardcoded fake dishes (`"k1".."k8"`, which crash checkout if added) and filters category chips against category UUIDs instead of names (`"Meals"`, `"Paratha"`, etc.).
3. **[P1-FRESH-01]** `HomeScreen.kt` never renders `uiState.freshBoardProducts` (`"TODAY AT GOVIND"`) and instead displays Fresh produce under a hardcoded `"TODAY'S PUNJABI MENU"` header. Category chips on `HomeScreen` do not filter products.
4. **[P1-CART-01]** `CartScreen` and `CheckoutViewModel` hardcode delivery fees (`₹0.0` / `FREE`) instead of reading `public.delivery_settings`, do not enforce `product.stock` limits, and have a non-functional `Apply Coupon` card.
5. **[P1-ADMIN-01]** 6 of 14 Admin pages (`/`, `/inventory`, `/customers`, `/coupons`, `/promotions`, `/settings`) are 100% static mock HTML with zero Supabase queries.
6. **[P1-ADMIN-02]** Admin `/orders` fails to join customer profiles, and `/products` does not write uploaded image URLs to `public.product_images` or support editing `bulk_tiers`.
7. **[P1-DATA-01]** All 79 rows in `public.product_images` are `ui-avatars.com` initials placeholders (plus 6 products have 0 images), and 5 categories in `public.categories` have 0 linked products due to duplicate category rows.

---

## 10. KNOWN P2 SECONDARY GAPS
1. **[P2-WHOLE-01]** Wholesale MOQ (`minOrderQty`) is not enforced when decrementing quantity in `CartScreen` / `WholesaleHomeScreen`, and the `Request Custom Quote on WhatsApp` button has an empty `onClick = {}`.
2. **[P2-PROD-01]** `ProductDetailsScreen` hardcodes `experienceType = "FRESH"` when adding items to cart, and its `Share` and `Favorite` buttons are no-ops.
3. **[P2-PROF-01]** `ProfileScreen` menu items (`Saved Addresses`, `Notifications`, `Help & Support`, `About Govind`) have empty `onClick = {}` lambdas.

---

## 11. PHASE 1 ENTRY CHECKLIST
- [x] Authoritative 118-test forensic audit preserved in `docs/baseline/phase-0/forensic-test-report.md`
- [x] Git status, HEAD commit (`c1ad235`), and modified/untracked file inventory recorded in `docs/baseline/phase-0/repository-state.txt`
- [x] Clean source snapshot archived at `docs/baseline/phase-0/govind-phase-0-source-snapshot.zip`
- [x] Live Supabase public data exported at `docs/baseline/phase-0/supabase-live-data-snapshot.json`
- [x] Android debug build (`assembleDebug`) and Admin build (`npm run build`) verified passing in `docs/baseline/phase-0/build-baseline.md`
- [x] Environment variables and secret locations audited (with zero secret values exposed) in `docs/baseline/phase-0/environment-inventory.md`
- [x] Ready to begin **Phase 1: Database, Security & Auth Foundation (Supabase)** upon user approval

---

## 12. ROLLBACK INSTRUCTIONS
- **Restore Source Tree to Phase 0 State:**
  ```powershell
  Expand-Archive -Path "C:\Web Apps\Govind\docs\baseline\phase-0\govind-phase-0-source-snapshot.zip" -DestinationPath "C:\Web Apps\Govind" -Force
  ```
- **Full Rollback & Database Restore Notes:** See [rollback-notes.md](file:///C:/Web%20Apps/Govind/docs/baseline/phase-0/rollback-notes.md).
