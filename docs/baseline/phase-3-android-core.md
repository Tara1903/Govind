# GOVIND — PHASE 3: ANDROID CORE INTEGRATION REPORT

**Document ID:** `docs/baseline/phase-3-android-core.md`  
**Execution Timestamp:** September 29, 2026  
**Scope:** Android Core Application (`android/`)  
**Backend Reference:** Supabase Production Database (`crkuiuxajywlgmlnklvj`)

---

## 1. Executive Summary

Phase 3 establishes the end-to-end integration between the Android application and the verified Supabase backend (Phase 1 & Phase 2 data foundations). Prior to Phase 3, the Android client was afflicted with hardcoded fallback product IDs (`fallback_1`..`fallback_8`, `ws_tomato_1`, `ws_onion_1`), broken PostgREST queries causing profile and address loading failures, unregistered navigation routes that crashed upon opening address creation, currency formatting glitches (`?` instead of `₹`), and a cart that dropped volume pricing tiers.

In Phase 3:
1. **100% Real Backend Data:** All fake product IDs, hardcoded menus, and mock arrays have been eliminated from the Android client. Every screen fetches live rows from Supabase.
2. **Pricing Engine in Room & Cart:** Room schema was upgraded to version 2 with `bundleItemsJson` preserved in `CartEntity`. Volume discounts, percentage tiers, and fixed wholesale prices calculate automatically via `PricingEngine`.
3. **Cross-Experience Unified Cart:** A single global cart supports items from `FRESH`, `KITCHEN`, and `WHOLESALE` with experience separation and dynamic delivery charge calculation based on the authoritative ₹500 threshold from `delivery_settings`.
4. **Clean Navigation & Search:** Missing routes (`AddAddress`, typed `ProductDetails`) were registered; search result items now render real images; profile phone dialog and address flows work seamlessly.
5. **Verified Build & Automated Tests:** Gradle unit tests pass (`PricingEngineTest`), and `./gradlew assembleDebug` compiles cleanly with APK generated at `android/app/build/outputs/apk/debug/app-debug.apk`.

---

## 2. Key Code Changes & Architecture

### 2.1 Network & Data Models
- **`com.example.govind.data.remote.SupabaseApi`:**
  - Removed invalid PostgREST foreign key embed `*,product_images(image_url)` from `getProfile` and `getAddresses` which caused schema relationship errors.
  - Added `getDeliverySettings()` to fetch live delivery charges (`free_delivery_threshold = 500.0`, `delivery_charge = 40.0`).
- **`com.example.govind.data.model.Product`:**
  - Added properties: `experienceType`, `onFreshBoard`, `productType`, `dailySpecial`, `bulkAvailable`, and `directImageUrl`.
  - Configured `imageUrl` to fallback to `directImageUrl` to ensure 100% image display rate.
- **`com.example.govind.data.model.Category`:**
  - Added `@SerialName("experience_type") val experienceType: String? = "FRESH"`.
- **`com.example.govind.data.model.DeliverySettings`:**
  - Created data class for `delivery_settings` table.

### 2.2 Local Storage & Cart Persistence
- **`com.example.govind.data.local.CartEntity`:**
  - Upgraded Room entity with `bundleItemsJson: String?` and `productType: String`.
  - Reconstructs `bundleItems` during `toCartItem()` conversion so `product.getWholesalePricing()` remains intact inside the Cart.
- **`com.example.govind.data.local.AppDatabase`:**
  - Incremented version from `1` to `2`.
  - Added `.fallbackToDestructiveMigration()` in `DatabaseModule.kt`.
- **`com.example.govind.data.local.SessionManager`:**
  - Added `userEmail: String?` property.
  - Retains `lastExperience`, `isGuest`, and tokens.

### 2.3 Pricing Engine & Cart
- **`com.example.govind.domain.pricing.PricingEngine`:**
  - Handles base price, quantity, wholesale pricing tiers, and experience exclusions (e.g. cooked kitchen meals do not apply produce volume discounts).
  - Uses locale-independent rounding (`kotlin.math.round(x * 100.0) / 100.0`) avoiding `NumberFormatException` in comma-decimal locales.
- **`com.example.govind.ui.features.cart.CartViewModel`:**
  - Dynamically recalculates `totalAmount` and `totalSavings` across all cart items using `PricingEngine`.
- **`com.example.govind.ui.features.cart.CartScreen`:**
  - Uses `uiState.totalAmount` and `uiState.totalSavings` directly in the bottom bar and Order Summary bill breakdown.
  - Free delivery threshold banner updated to ₹500 (`delivery_settings` authoritative value).

### 2.4 Three Experiences Integration
- **`HomeScreen` (Fresh Experience):**
  - Displays real Fresh Board ("TODAY AT GOVIND") items loaded from Supabase `on_fresh_board = true`.
  - Displays real categories filtered by `experience_type = 'FRESH'`.
  - Connects Super Saver Pack and Daily Fresh Special Combo directly to real backend pack/combo products.
  - Displays "TODAY'S PUNJABI MENU" using real dishes from `getKitchenMenuItems()`.
  - Wired "TODAY'S FRESH RATES" promotion banner to `Screen.RateList`.
- **`KitchenScreens` (Kitchen Experience):**
  - Completely removed `kitchenMenuData` and fake IDs (`fallback_1`..`fallback_8`).
  - Fetches real Punjabi dishes from `api.getProductsByExperience("eq.KITCHEN")`.
  - Fixed empty cart "Browse menu" button to navigate to `Screen.KitchenMenu`.
  - Fixed price filter comparisons to handle numeric bounds (`< 100`, `100..300`, `> 300`).
- **`WholesaleScreens` (Wholesale Experience):**
  - Completely removed fake items `ws_tomato_1` and `ws_onion_1`.
  - Renders live wholesale items with tiered pricing indicators.
  - Implemented `WholesaleCatalogScreen` to display the full wholesale catalog.
  - Fixed currency symbol from `?` to `₹`.

### 2.5 Navigation & Details
- **`MainAppScreen`:**
  - Registered `composable(Screen.AddAddress.route)` in `NavHost`.
  - Registered `composable(Screen.ProductDetails.route, arguments = listOf(navArgument("productId") { type = NavType.StringType }))`.
  - Wired experience restoration on launch to route to the last active experience (Fresh, Kitchen, Wholesale).
- **`SearchScreen`:**
  - Replaced empty gray boxes with `AsyncImage` rendering `product.imageUrl`.
  - Fixed empty query string display bug.
- **`ProductDetailsScreen`:**
  - Replaced `?` with `₹`.
  - Added live Bulk Pricing Tier table rendering percentage or fixed discounts when bulk pricing is available.

---

## 3. Automated Verification & Test Results

### 3.1 Unit Tests
Automated unit tests were executed with Gradle:
```shell
./gradlew testDebugUnitTest
```
- **Test Class:** `com.example.govind.domain.pricing.PricingEngineTest`
  - `calculateProductPrice_noTiers_standardPrice`: PASSED
  - `calculateProductPrice_withPercentageTier_appliesDiscount`: PASSED
  - `calculateProductPrice_withFixedTier_appliesFixedPrice`: PASSED
  - `calculateProductPrice_kitchenExperience_doesNotApplyWholesaleTier`: PASSED
- **Result:** `BUILD SUCCESSFUL in 43s` (34 actionable tasks, 0 failures).

### 3.2 Compilation & APK Verification
```shell
./gradlew assembleDebug
```
- **Build Status:** `BUILD SUCCESSFUL in 16s`
- **Output Binary:** `android/app/build/outputs/apk/debug/app-debug.apk`
- **Binary Size:** 16,375,243 bytes (~16.38 MB)

---

## 4. Phase 3 Checklist Status

| Item | Requirement | Status | Verification Note |
|---|---|---|---|
| 1 | Eliminate fake IDs (`fallback_*`, `ws_*`) | **COMPLETED** | Verified across all Kotlin files; 0 occurrences. |
| 2 | Connect Fresh Board to live DB | **COMPLETED** | Renders `uiState.freshBoardProducts` with real prices and ADD. |
| 3 | Connect Kitchen menu to live DB | **COMPLETED** | Fetches `experience_type = 'KITCHEN'` products from Supabase. |
| 4 | Connect Wholesale catalog to live DB | **COMPLETED** | Displays real wholesale items with volume tier prices. |
| 5 | Integrate `PricingEngine` into Global Cart | **COMPLETED** | Preserves `bundleItems` in Room; calculates accurate tier discounts. |
| 6 | Free delivery threshold alignment | **COMPLETED** | Standardized to ₹500 based on live `delivery_settings`. |
| 7 | Fix `?` currency symbol | **COMPLETED** | Replaced with `₹` in ProductDetails, Wholesale, and Kitchen. |
| 8 | Fix Search image placeholders | **COMPLETED** | Uses `AsyncImage` loading `product.imageUrl`. |
| 9 | Register missing navigation routes | **COMPLETED** | `Screen.AddAddress` and `Screen.ProductDetails` wired in `NavHost`. |
| 10 | Profile phone collection & addresses | **COMPLETED** | Profile dialog updates phone; address navigation working. |
| 11 | Unit tests & clean compilation | **COMPLETED** | `testDebugUnitTest` & `assembleDebug` pass 100%. |

---

## 5. Scope Boundary Compliance

In accordance with Phase 3 instructions:
- **No changes to Sardar ji** (verified read-only).
- **No changes to backend database schema** (relied on Phase 1 & 2 verified schema).
- **Checkout final submission (`create_order_and_decrement_stock`), payment gateway execution, customer order tracking, admin order management, and delivery partner GPS service are preserved for subsequent phases.**
