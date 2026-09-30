# GOVIND — Design Implementation Map
## Stitch → Android Compose Screen Mapping

> **Stitch Project:** GOVIND Android Food Platform UX (`projects/13868558863199017635`)
> **Design System:** Govind Expressive Material — Plus Jakarta Sans, `#064520` primary
> **Last Stitch Update:** 2026-09-29T18:07:11Z
> **Last Map Update:** 2026-09-29

---

## Design System Summary (from Stitch)

| Token | Stitch Value | Current Code | Status |
|---|---|---|---|
| Primary | `#064520` | `#064520` ✅ | Match |
| Fresh Green | `#38802A` | `#38802A` ✅ | Match |
| Orange / Energy | `#F5450D` | `#F5450D` ✅ | Match |
| Warm Canvas | `#FEFCF5` | `#FEFCF5` ✅ | Match |
| Pure Surface | `#FFFFFF` | `#FFFFFF` ✅ | Match |
| Neutral Dark | `#1A1F1B` | `#163126` ⚠️ | Mismatch — update |
| Neutral Muted | `#616963` | `#617068` ⚠️ | Close — update |
| Border / Outline | `#E6E8E2` | `#DDE5DF` ⚠️ | Mismatch — update |
| Font | Plus Jakarta Sans | SansSerif ❌ | **Must update** — add PJS font files |
| Roundness | 8dp default, 16dp cards | 8/16dp ✅ | Close match |
| Headline-xl-mobile | 28sp / 800 | 32sp / Bold | Needs adjustment |
| Wholesale Navy | `#1A3038` | Not defined ❌ | **Must add** |
| Wholesale Amber | `#C47D00` | Not defined ❌ | **Must add** |
| Fresh Tint | `#E8F3EB` | `#EFF6F0` ⚠️ | Close — update |
| Kitchen Tint | `#FDEEE9` | `#FFF0EB` ⚠️ | Close — update |

---

## Stitch Screens → Android Mapping

### GLOBAL

| Screen | Stitch ID | Compose File | Route | ViewModel | Repository | Status | Notes |
|---|---|---|---|---|---|---|---|
| **Splash** | — | `SplashScreen.kt` | `splash` | `SplashViewModel` | — | ✅ Exists | Style update needed |
| **Onboarding** | `a4ce0576` | `OnboardingScreen.kt` | `onboarding` | `OnboardingViewModel` | — | ✅ Exists | Redesign to match Stitch |
| **Auth (OTP)** | — | `AuthScreen.kt` | `auth` | `AuthViewModel` | `GovindRepository` | ✅ Exists | Style update needed |
| **Experience Switcher** | In all homes | `ExperienceSwitcher.kt` | N/A | `AppState` | — | ✅ Exists | Needs Stitch pill track redesign |
| **Bottom Navigation** | In all homes | `MainAppScreen.kt` | N/A | — | — | ✅ Exists | Needs Stitch styling update |
| **Global Search** | — | `SearchScreen.kt` | `search` | `SearchViewModel` | `GovindRepository.searchProducts()` | ✅ Exists | Add cross-experience results |

### FRESH

| Screen | Stitch ID | Compose File | Route | ViewModel | Data Source | Status | Notes |
|---|---|---|---|---|---|---|---|
| **Fresh Home** | `a908e0da` | `HomeScreen.kt` | `home` | `HomeViewModel` | `getFreshBoardProducts()`, `getCategories()`, `getFeaturedProducts()`, `getPacks()`, `getCombos()` | ✅ Exists | **Major redesign** — add location bar, ETA pill, hero, category circles, product cards per Stitch |
| **Fresh Categories** | Part of Fresh Home | `HomeScreen.kt` | `home` | `HomeViewModel` | `getCategories()` | ✅ Exists | Convert to circle icons per Stitch |
| **Fresh Product Detail** | — | `ProductDetailsScreen.kt` | `product/{id}` | `ProductDetailsViewModel` | `getProductDetails()` | ✅ Exists | Style update per Stitch |

### KITCHEN

| Screen | Stitch ID | Compose File | Route | ViewModel | Data Source | Status | Notes |
|---|---|---|---|---|---|---|---|
| **Kitchen Home** | `82011a72` | `KitchenScreens.kt` | `kitchen_home` | `KitchenViewModel` | `getKitchenMenuItems()`, `getGlobalCart()` | ✅ Exists | Redesign header, hero, category grid per Stitch |
| **Kitchen Menu** | Part of Kitchen Home | `KitchenScreens.kt` | `kitchen_menu` | `KitchenViewModel` | `getKitchenMenuItems()` | ✅ Exists | Style update per Stitch |

### WHOLESALE

| Screen | Stitch ID | Compose File | Route | ViewModel | Data Source | Status | Notes |
|---|---|---|---|---|---|---|---|
| **Wholesale Home** | `a3d0db79` | `WholesaleScreens.kt` | `wholesale_home` | `WholesaleViewModel` | `getWholesaleItems()` | ✅ Exists | **Major redesign** — add Stitch wholesale identity, categories, bulk deals sections |
| **Wholesale Catalog** | `c41f95eb` | `WholesaleScreens.kt` | `wholesale_catalog` | `WholesaleViewModel` | `getWholesaleItems()` | ✅ Exists | Add Stitch tier pricing matrix cards |
| **Wholesale Product Detail** | `58a150b0` | `ProductDetailsScreen.kt` | `product/{id}` | `ProductDetailsViewModel` | `getProductDetails()`, `PricingEngine` | ✅ Shared | **Must specialize** wholesale PD with tier calculator per Stitch |
| **Wholesale Rate List** | `eb4995b6` | `RateListScreen.kt` | `ratelist` | `RateListViewModel` | `getWholesaleItems()` | ✅ Exists | Redesign to Stitch Mandi rate table |

### SHOPPING

| Screen | Stitch ID | Compose File | Route | ViewModel | Data Source | Status | Notes |
|---|---|---|---|---|---|---|---|
| **Global Cart** | `0e886679` | `CartScreen.kt` | `cart` | `CartViewModel` | `getGlobalCart()` | ✅ Exists | Redesign per Stitch — grouped sections, floating dock, delivery threshold |
| **Checkout** | `81b67470` | `CheckoutScreen.kt` | `checkout` | `CheckoutViewModel` | `getAddresses()`, `placeGlobalOrder()` | ✅ Exists | **Redesign** — Stitch StarPay checkout layout |
| **Add Address** | — | `AddAddressScreen.kt` | `add_address` | `AddAddressViewModel` | `addAddress()` | ✅ Exists | Style update |
| **Order Success** | In `CheckoutScreen.kt` | `CheckoutScreen.kt` | `checkout` (state) | `CheckoutViewModel` | — | ✅ Exists | Style update per Stitch |

### ORDERS

| Screen | Stitch ID | Compose File | Route | ViewModel | Data Source | Status | Notes |
|---|---|---|---|---|---|---|---|
| **My Orders** | — | `OrdersScreen.kt` | `orders` | `OrdersViewModel` | `getOrders()` | ✅ Exists | Redesign per Stitch order cards |
| **Order Details** | — | `OrderDetailsScreen.kt` | `order_details/{id}` | `OrdersViewModel` | `getOrderById()` | ✅ Exists | Redesign per Stitch |
| **Live Tracking** | `8e5f8cf9` | `OrderDetailsScreen.kt` (partial) | `order_details/{id}` | `OrdersViewModel` | `TrackingRepository` | ⚠️ Partial | **Must implement** dedicated tracking UI per Stitch — ETA, status, map, timeline |

### PROFILE

| Screen | Stitch ID | Compose File | Route | ViewModel | Data Source | Status | Notes |
|---|---|---|---|---|---|---|---|
| **Profile Hub** | `4a7d22d8` | `ProfileScreen.kt` | `profile` | `ProfileViewModel` | `getUserProfile()` | ✅ Exists | **Major redesign** per Stitch — add experience shortcuts, sections hub |

---

## Reusable Components Required

| Component | Stitch Reference | Current Status | Priority |
|---|---|---|---|
| `GovindTopBar` | All home screens — location + ETA + profile | ❌ Not centralized | HIGH |
| `GovindExperienceSwitcher` | Pill track switcher | ✅ Exists, needs Stitch restyling | HIGH |
| `GovindBottomNavigation` | 5-tab shared nav | ✅ Exists, needs Stitch restyling | HIGH |
| `GovindProductCard` | Fresh product tiles | ✅ `ProductCard` exists, needs Stitch layout | HIGH |
| `GovindKitchenCard` | Kitchen menu cards | ✅ `ProductMenuCard` exists | MEDIUM |
| `GovindWholesaleCard` | Wholesale tier rows | ✅ `WholesaleProductRow` exists | HIGH |
| `GovindQuantityControl` | ADD → Stepper pill | ✅ Exists | MEDIUM (needs ADD initial state) |
| `GovindSectionHeader` | Section titles | ✅ `SectionHeader` exists | LOW |
| `GovindPrice` | Price + MRP + discount | ❌ Scattered | HIGH |
| `GovindDiscountBadge` | "24% OFF" pill | ❌ Inline only | MEDIUM |
| `GovindWholesaleTierCard` | Tier pricing matrix | ❌ Missing | HIGH |
| `GovindCartDock` | Persistent floating cart dock | ❌ Missing | HIGH |
| `GovindDeliveryETA` | "10-15 MINS" orange pill | ❌ Missing | HIGH |
| `GovindCategoryCircle` | Circular category icons | ❌ Missing | MEDIUM |
| `GovindStatusChip` | Order status pills | ❌ Missing | MEDIUM |
| `GovindEmptyState` | Reusable empty pattern | ⚠️ Partial (cart only) | MEDIUM |
| `GovindErrorState` | Reusable error pattern | ⚠️ Partial (kitchen only) | MEDIUM |
| `GovindLoadingState` | Skeleton / shimmer | ❌ Missing | MEDIUM |
| `GovindAddressCard` | Address selection | ✅ Exists in checkout | LOW |
| `GovindOrderCard` | Order list items | ❌ Needs creation | MEDIUM |
| `GovindTimeline` | Tracking timeline steps | ❌ Missing | MEDIUM |
| `GovindVegIndicator` | FSSAI veg/non-veg marker | ❌ Missing | HIGH |

---

## Known Conflicts & Issues

| ID | Issue | Resolution |
|---|---|---|
| C-01 | Typography uses system `SansSerif` — Stitch requires `Plus Jakarta Sans` | Add PJS font files to `res/font/`, update `Type.kt` |
| C-02 | Neutral text color `#163126` doesn't match Stitch `#1A1F1B` | Update `GovindDesignSystem.kt` |
| C-03 | Border color `#DDE5DF` doesn't match Stitch `#E6E8E2` | Update `Color.kt` and `GovindDesignSystem.kt` |
| C-04 | No wholesale-specific colors (`#1A3038`, `#C47D00`) | Add to `GovindDesignSystem.kt` |
| C-05 | `ProductCard` lacks FSSAI veg indicator, ETA pill, proper ADD→Stepper transition | Rebuild per Stitch spec |
| C-06 | Experience Switcher uses colored backgrounds — Stitch uses white pill on gray track | Redesign per Stitch pill track |
| C-07 | No persistent floating cart dock — Stitch shows edge-to-edge floating pill | Build `GovindCartDock` |
| C-08 | Fresh Home top bar is `primaryContainer` — Stitch uses `#064520` or warm canvas | Update per Stitch |
| C-09 | Wholesale has no tier visualization on product cards | Build `GovindWholesaleTierCard` |
| C-10 | HomeScreen uses hardcoded fallback categories — should use real data only | Remove fallback data |
| C-11 | Some colors hardcoded inline (e.g., `Color(0xFF2E7D32)`, `Color(0xFFE65100)`) instead of design system | Replace with design system tokens |
| C-12 | Bottom nav changes per experience — Stitch wants unified 5-tab nav | Unify: Home, Explore, Orders, Cart, Profile |
| C-13 | No `Plus Jakarta Sans` font files in `res/font/` | Download and add TTF/OTF files |

---

## Implementation Groups

### GROUP 1: Design System Foundation
- [ ] Add Plus Jakarta Sans font files
- [ ] Update `Color.kt` — add Stitch-exact tokens
- [ ] Update `GovindDesignSystem.kt` — add wholesale colors, fix mismatches
- [ ] Update `Type.kt` — implement Stitch typography scale with PJS
- [ ] Update `Shape.kt` — verify shape scale
- [ ] Update `Dimens.kt` — add Stitch spacing tokens
- [ ] Update `Theme.kt` — update color scheme mappings
- [ ] Create `GovindTopBar` component
- [ ] Redesign `PillExperienceSwitcher` per Stitch
- [ ] Update `MainAppScreen.kt` — unified bottom nav
- [ ] Create `GovindCartDock` floating component
- [ ] Create `GovindDeliveryETA` component
- [ ] Create `GovindVegIndicator` component
- [ ] Rebuild `GovindProductCard` per Stitch
- [ ] Create `GovindPrice` component

### GROUP 2: Fresh Home + Categories + Product Detail
- [ ] Redesign `HomeScreen.kt` — Stitch Fresh Home layout
- [ ] Add category circles
- [ ] Add hero banner
- [ ] Daily Harvest Picks section with Stitch product cards
- [ ] Deals section
- [ ] Product Detail screen redesign

### GROUP 3: Kitchen Home + Menu
- [ ] Redesign `KitchenHomeScreen` per Stitch
- [ ] Kitchen header with orange accent
- [ ] Kitchen menu card redesign
- [ ] Kitchen Product Detail

### GROUP 4: Wholesale Home + Catalog + Product Detail + Rate List
- [ ] Redesign `WholesaleHomeScreen` per Stitch
- [ ] Create wholesale tier pricing matrix
- [ ] Wholesale catalog redesign
- [ ] Wholesale product detail with bulk calculator
- [ ] Rate List redesign per Stitch Mandi table

### GROUP 5: Search + Global Cart
- [ ] Search screen redesign
- [ ] Cart screen redesign per Stitch
- [ ] Cart grouping by experience
- [ ] Persistent cart dock integration

### GROUP 6: Address + Checkout + Payment + Order Success
- [ ] Address list/add/edit redesign
- [ ] Checkout screen redesign per Stitch
- [ ] StarPay payment UI states
- [ ] Order success redesign

### GROUP 7: Orders + Tracking
- [ ] My Orders list redesign
- [ ] Order Details redesign
- [ ] Live Tracking screen per Stitch

### GROUP 8: Profile + Favorites + Settings
- [ ] Profile hub redesign per Stitch
- [ ] Experience shortcuts
- [ ] Section navigation

### GROUP 9: States + Polish
- [ ] Loading skeletons
- [ ] Empty states
- [ ] Error states
- [ ] Offline states
- [ ] Final consistency audit
