# GOVIND — PHASE 4: CHECKOUT, PAYMENT & ORDER CREATION REPORT

**Document ID:** `docs/baseline/phase-4-checkout-orders.md`  
**Execution Timestamp:** September 29, 2026  
**Scope:** Android Checkout & Order Creation (`android/`)  
**Backend Reference:** Supabase Production Database (`crkuiuxajywlgmlnklvj`)

---

## 1. Executive Summary

Phase 4 transforms the shopping layer established in Phase 3 into a **real, atomic, production-ready transaction system** connected directly to Supabase (`crkuiuxajywlgmlnklvj`). 

Prior to Phase 4, the Android checkout flow suffered from fatal force-unwraps (`uiState.cart!!.items`), absence of an unauthenticated guest gate, client-only pricing assumptions without backend stock/price validation, missing address snapshots, and lack of integration with the PostgreSQL atomic RPC `create_order_and_decrement_stock`.

In Phase 4:
1. **Zero Force-Unwraps & Safe State Machine:** Replaced all unsafe state with `CheckoutUiState` covering authentication, addresses, cart items, delivery calculation, order placement, and errors.
2. **Guest Authentication Gate:** Unauthenticated users tapping Checkout are presented with a clean login gate ("Sign In to Checkout") with their local Room basket preserved. Authenticating seamlessly returns the user back to the checkout flow.
3. **Mandatory Profile Phone Collection:** Checkout automatically detects if the customer profile lacks a contact number and prompts for a 10-digit mobile number, saving it to `profiles.phone` before order placement.
4. **Server Price & Stock Revalidation:** Before calling the order creation RPC, `placeGlobalOrder` fetches current product rows via `getProductsByIds` from Supabase to verify items are active and recalculates authoritative pricing via `PricingEngine`.
5. **Canonical Delivery Fee Rule:** Enforces the canonical rule: Orders $\ge ₹500$ qualify for **FREE Delivery**, otherwise **₹40** delivery charge is applied (matching `delivery_settings`).
6. **Payment System Wiring:**
   - **Cash on Delivery (COD):** Fully enabled and set as the default active payment method (`payment_status = 'PENDING'`).
   - **Pay Online (StarPay):** Explicitly marked `UNAVAILABLE` in the UI with a persistent warning notice on tap: `"ONLINE PAYMENT BLOCKED — STARPAY CREDENTIALS/API CONTRACT REQUIRED. Please select Cash on Delivery (COD)."`. Online order submission is strictly blocked in the repository.
7. **Atomic PostgreSQL RPC Execution:** Calls `public.create_order_and_decrement_stock` with all 13 parameters, creating an order with status `PLACED`, snapshotting items and delivery address in `JSONB`, and decrementing stock.
8. **Cart Clearing & Order Success View:** The local Room database cart is cleared only upon RPC confirmation, and a dedicated `OrderSuccessView` renders the order ID (`#6A978A37`), status, delivery address, bill details, and navigation actions.
9. **Live Emulator & Database Verification:** Verified end-to-end on `emulator-5554` and verified in Supabase live PostgreSQL table records.

---

## 2. Key Code Changes & Architecture

### 2.1 Remote API & Data Models
- **`com.example.govind.data.remote.SupabaseApi`:**
  - Added `getProductsByIds(@retrofit2.http.Query("id") idsQuery: String): List<Product>` (`rest/v1/products?id=in.(...)`) for server-side revalidation of checkout items.
- **`com.example.govind.data.model.OrderModels`:**
  - `OrderItem`: Added snapshot fields `productName`, `unit`, `basePrice`, `bulkDiscount`, `effectiveUnitPrice`, `lineTotal`, `discount`, `experienceType`.
  - `Order`: Added `savings: Double = 0.0`, `experienceType: String? = null`, and `addressSnapshot: kotlinx.serialization.json.JsonObject? = null`.

### 2.2 Authoritative Order Placement Repository
- **`com.example.govind.data.repository.SupabaseGovindRepositoryImpl.placeGlobalOrder`:**
  - Verifies user authentication (`sessionManager.userId` and `sessionManager.isLoggedIn()`).
  - Rejects `ONLINE` payment method with explanatory error until StarPay credentials are provided.
  - Server revalidation: Queries live products from Supabase to ensure all items are active and calculates verified subtotal and savings via `PricingEngine`.
  - Resolves delivery charge using canonical `subtotal >= 500.0 ? 0.0 : 40.0` rule.
  - Constructs `address_snapshot` with `name`, `phone`, `house`, `street`, `area`, `city`, `pincode`, and `landmark`.
  - Invokes `api.createOrderAndDecrementStock` passing:
    1. `p_customer_id`: User UUID
    2. `p_subtotal`: Revalidated subtotal
    3. `p_discount`: Total savings
    4. `p_coupon_id`: Null or applied coupon UUID
    5. `p_delivery_charge`: Canonical delivery fee
    6. `p_total`: Grand total
    7. `p_savings`: Total savings
    8. `p_address_snapshot`: Address JSON object
    9. `p_payment_method`: `"COD"`
    10. `p_payment_status`: `"PENDING"`
    11. `p_order_status`: `"PLACED"`
    12. `p_experience_type`: Cart primary experience (`FRESH`, `KITCHEN`, or `WHOLESALE`)
    13. `p_items`: Array of item JSON objects with price and line total snapshots
  - Decrements local and remote inventory atomically.
  - Clears Room cart via `cartDao.clearCart()` only after RPC succeeds.

### 2.3 ViewModel & UI Implementation
- **`com.example.govind.ui.features.checkout.CheckoutViewModel`:**
  - Exposes robust `CheckoutUiState` flow.
  - Loads cart, calculates authoritative pricing, fetches saved addresses, and checks profile phone completeness.
  - Handles `savePhoneNumber` saving directly to Supabase `profiles.phone`.
  - Selects default address automatically or prompts for address creation if none exist.
  - Intercepts online payment selection with user-facing warning notice.
- **`com.example.govind.ui.features.checkout.CheckoutScreen`:**
  - **Guest Gate:** Renders lock icon, benefits reminder, and "Sign In to Checkout" button if `!uiState.isAuthenticated`.
  - **Phone Number Required Modal:** Renders clean 10-digit mobile number input if profile lacks contact information.
  - **Address Selection:** Renders radio selection of saved addresses with default tag and "+ Add New" button navigating to `AddAddressScreen`.
  - **Payment Selector:** Renders Cash on Delivery as active/selected and Pay Online as disabled with `UNAVAILABLE` tag and dismissible StarPay alert banner.
  - **Order Summary:** Itemized list of products with unit prices, subtotal, total savings highlighted in orange, delivery fee with free delivery qualifier, and total to pay.
  - **Dedicated Order Success View:** Displays green confirmation checkmark, short Order ID `#${order.id.take(8).uppercase()}`, status `PLACED`, payment `CASH ON DELIVERY`, delivery address snapshot, full bill summary, and action buttons (`View Order Details`, `Continue Shopping`).
- **`com.example.govind.ui.navigation.MainAppScreen`:**
  - Configured `Screen.Auth` backstack to return directly to previous route on successful sign-in so cart and checkout intent are preserved.
  - Registered `onNavigateToOrderDetails` and `onNavigateToAuth` navigation lambdas.

---

## 3. Automated Verification & Test Results

### 3.1 Unit Tests
```shell
./gradlew testDebugUnitTest
```
- **Test Class:** `com.example.govind.domain.pricing.PricingEngineTest`
  - `calculateProductPrice_noTiers_standardPrice`: PASSED
  - `calculateProductPrice_withPercentageTier_appliesDiscount`: PASSED
  - `calculateProductPrice_withFixedTier_appliesFixedPrice`: PASSED
  - `calculateProductPrice_kitchenExperience_doesNotApplyWholesaleTier`: PASSED
- **Test Class:** `com.example.govind.domain.checkout.CheckoutCalculationTest`
  - `deliveryFee_under500_charges40`: PASSED
  - `deliveryFee_atOrAbove500_isFree`: PASSED
  - `deliveryFee_emptyCart_isZero`: PASSED
  - `experienceType_resolution_matchesPrimaryItem`: PASSED
  - `starPayNotice_containsRequiredExplanation`: PASSED
- **Result:** `BUILD SUCCESSFUL in 19s` (34 actionable tasks, 0 failures).

### 3.2 Compilation & APK Verification
```shell
./gradlew assembleDebug
```
- **Build Status:** `BUILD SUCCESSFUL`
- **Output Binary:** `android/app/build/outputs/apk/debug/app-debug.apk`

---

## 4. Live Verification Evidence

### 4.1 Test User & Initial State
- **Authenticated User:** `harisinghsikh1252@gmail.com` (`d2129669-fbb7-4b28-adb1-cda1fbbc2598`)
- **Product in Basket:** `Capsicum` (`6e6c35c3-121d-4d70-af1b-7be83859bf1b`)
  - Selling Price: ₹47.0 (MRP ₹55.0)
  - Quantity: 1 kg
- **Initial Capsicum Inventory:** `100` units in `public.products`

### 4.2 Interactive Walkthrough on Emulator
1. **Cart Review:** Basket shows Capsicum (1 kg, ₹47.0), Delivery ₹40.0, Grand Total ₹87.0. Free delivery banner indicates "Add ₹453 more for FREE Delivery".
2. **Checkout Navigation:** User taps `Checkout` button.
3. **Phone Collection Dialog:** Dialog appears prompting "Phone Number Required". Entered `9876543210` and tapped "Save & Continue". Profile updated in Supabase.
4. **Address Creation:** Tapped "+ Add Address", entered address for Hari Singh (`Flat 402, Main Ring Road, Lajpat Nagar, New Delhi - 110024`), saved successfully.
5. **StarPay Disabled Verification:** Tapped "Pay Online (UPI, Cards, NetBanking)". The option remained unselected and displayed the persistent warning banner:
   > `ONLINE PAYMENT BLOCKED — STARPAY CREDENTIALS/API CONTRACT REQUIRED. Please select Cash on Delivery (COD).`
6. **Place Order Trigger:** Cash on Delivery selected, address selected. User tapped `Place Order (COD)`.
7. **Order Success View:** Order placed instantly. UI transitioned to `OrderSuccessView` showing:
   - Order `#6A978A37` (Full Ref: `6a978a37-387a-4456-b1e4-89997c845067`)
   - STATUS: `PLACED`
   - PAYMENT: `CASH ON DELIVERY`
   - Delivery Address: Hari Singh, Flat 402, Main Ring Road, Lajpat Nagar, New Delhi - 110024
   - Items Subtotal: ₹47.0
   - Total Savings: -₹8.0
   - Delivery Charge: ₹40.0
   - Total to Pay (COD): ₹87.0
8. **Cart Cleared Verification:** Tapped "Continue Shopping", navigated to Cart tab. Basket is completely empty: `"Your basket is waiting for something fresh."`.
9. **Order History Verification:** Navigated to "My Orders" tab. Order `#6A978A37` appears at the top of the list with status `PLACED` and Total `₹87.0`.

### 4.3 Supabase Database Verification
Verified via live SQL queries against Supabase:
- **`public.orders` record:**
  ```json
  {
    "id": "6a978a37-387a-4456-b1e4-89997c845067",
    "customer_id": "d2129669-fbb7-4b28-adb1-cda1fbbc2598",
    "subtotal": 47.0,
    "discount": 8.0,
    "delivery_charge": 40.0,
    "total": 87.0,
    "savings": 8.0,
    "payment_method": "COD",
    "payment_status": "PENDING",
    "order_status": "PLACED",
    "experience_type": "FRESH",
    "address_snapshot": {
      "id": "113404eb-0681-4d34-843d-844ac7610607",
      "area": "Lajpat Nagar",
      "city": "New Delhi",
      "name": "Hari Singh",
      "house": "Flat 402",
      "phone": "9876543210",
      "street": "Main Ring Road",
      "pincode": "110024"
    }
  }
  ```
- **`public.order_items` record:**
  ```json
  {
    "id": "328bc4f7-84a4-4d38-aba4-769bdcd0ae21",
    "order_id": "6a978a37-387a-4456-b1e4-89997c845067",
    "product_id": "6e6c35c3-121d-4d70-af1b-7be83859bf1b",
    "product_name": "Capsicum",
    "unit": "kg",
    "price": 47.0,
    "quantity": 1,
    "experience_type": "FRESH",
    "base_price": 47.0,
    "bulk_discount": 0.0,
    "effective_unit_price": 47.0,
    "line_total": 47.0
  }
  ```
- **Stock Decrement in `public.products`:**
  - Before order: `stock_quantity = 100`
  - After order: `stock_quantity = 99`
  - Decrement confirmed: Exactly 1 unit decremented atomically by the database RPC.

---

## 5. StarPay Payment Gateway Status

| Property | Status | Detail |
|---|---|---|
| Gateway Name | StarPay | Client requested online payment gateway |
| Integration Status | **BLOCKED / UNAVAILABLE** | Merchant credentials, API keys, endpoints, and docs are missing |
| User Interface State | Explicitly Disabled | Marked `UNAVAILABLE` with banner explaining requirement |
| Repository Guard | Strict Exception | Attempting to call `placeGlobalOrder("ONLINE")` throws an explicit blocked error |
| Cash on Delivery | **ACTIVE & FUNCTIONING** | Full end-to-end checkout and order placement verified with COD |

---

## 6. Phase 4 Exit Checklist

| Item | Requirement | Status | Verification Note |
|---|---|---|---|
| 1 | Eliminate checkout crash / force unwraps | **COMPLETED** | Verified across all states; zero crashes. |
| 2 | Guest authentication gate with cart preserved | **COMPLETED** | Displays login prompt; cart preserved in Room. |
| 3 | Profile phone collection at checkout | **COMPLETED** | Prompts for 10-digit phone; saved to `profiles.phone`. |
| 4 | Address selection & creation | **COMPLETED** | Radio list of addresses + `AddAddressScreen` flow. |
| 5 | Server price & stock revalidation | **COMPLETED** | Revalidates via `getProductsByIds` & `PricingEngine`. |
| 6 | Canonical delivery fee rule | **COMPLETED** | Subtotal $\ge ₹500 \implies ₹0$, else $₹40$. |
| 7 | Payment options (COD active, StarPay blocked) | **COMPLETED** | COD default active; StarPay explicitly blocked. |
| 8 | Atomic RPC order placement | **COMPLETED** | Calls `create_order_and_decrement_stock` with 13 args. |
| 9 | Inventory stock decrement | **COMPLETED** | Capsicum stock verified decremented from 100 to 99. |
| 10 | Local cart clearing | **COMPLETED** | Room cart cleared upon RPC confirmation. |
| 11 | Dedicated Order Success view | **COMPLETED** | Displays Order ID `#6A978A37`, summary, and actions. |
| 12 | Automated unit tests & clean build | **COMPLETED** | `testDebugUnitTest` & `assembleDebug` pass 100%. |

---

## 7. Scope Boundaries & Next Phase Transition

- **Phase 4 is complete.**
- **No Phase 5 work was started.**
- **Next.js Admin panel, customer website, and Sardar ji were strictly untouched.**
- Ready for Phase 5 (Order Tracking, Delivery Partner UI/Foreground Location Service, and Admin Order Management).
