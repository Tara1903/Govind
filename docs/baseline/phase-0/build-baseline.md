# GOVIND — PHASE 0 BUILD BASELINE

**Recorded At:** 2026-09-29T15:54:00+05:30

---

## 1. ANDROID BUILD BASELINE (`C:\Web Apps\Govind\android`)

### 1.1 Build Configuration & Versions
- **Application ID / Package Name:** `com.example.govind`
- **Namespace:** `com.example.govind`
- **Version Code / Version Name:** `versionCode = 1`, `versionName = "1.0"`
- **SDK Versions:**
  - `minSdk = 24`
  - `targetSdk = 36`
  - `compileSdk = 36`
- **Gradle Version:** `9.1.0` (`gradle-9.1.0-bin.zip`)
- **Android Gradle Plugin (AGP):** `8.7.2`
- **Kotlin Version:** `2.0.21`
- **Java / JVM Toolchain:** OpenJDK `17.0.20` (`JavaVersion.VERSION_17`, `jvmToolchain(17)`)
- **Jetpack Compose BOM:** `2024.10.01`
- **Key Libraries:**
  - Hilt: `2.55` (`androidx-hilt-navigation-compose = 1.3.0`)
  - Navigation Compose: `2.8.5`
  - Room: `2.7.0-beta01`
  - Retrofit: `2.11.0` + OkHttp `4.12.0` + Kotlinx Serialization `1.7.3`
  - Coil: `3.1.0`
  - Firebase BOM: `33.5.1` (`firebase-messaging = 24.0.3`)
  - Play Services Location: `21.3.0`
  - Razorpay Checkout SDK: `1.6.39`

### 1.2 Build Execution Result
- **Command:** `.\gradlew.bat assembleDebug` (executed in `C:\Web Apps\Govind\android`)
- **Build Status:** **PASS** (`BUILD SUCCESSFUL in 39s`, Exit Code `0`)
- **Generated Debug APK:**
  - **Path:** `C:\Web Apps\Govind\android\app\build\outputs\apk\debug\app-debug.apk`
  - **Size:** `16,299,096` bytes (`15.54 MB`)
  - **Last Modified Timestamp:** `2026-09-28T17:50:22.8512584+05:30` (UP-TO-DATE verified at `2026-09-29T15:49:44+05:30`)
- **Additional Root APK Copies Present:**
  - `C:\Web Apps\Govind\govind-release.apk` (`11,297,852` bytes)
  - `C:\Web Apps\Govind\govind-release-v2.apk` (`16,298,864` bytes)
- **Non-Fatal Build Warnings:**
  1. `compileSdk = 36` with AGP `8.7.2` (tested up to `compileSdk = 35`).
  2. SDK XML version 4 warning from command-line tools vs Android Studio SDK packages.

---

## 2. ADMIN WEB BUILD BASELINE (`C:\Web Apps\Govind\admin`)

### 2.1 Build Configuration & Versions
- **Framework:** Next.js `16.3.6` (App Router, Turbopack)
- **React / React DOM:** `19.2.8`
- **Node.js Runtime:** `v26.4.0`
- **TypeScript:** `^5`
- **Tailwind CSS:** `^4` (`@tailwindcss/postcss`)
- **Supabase SDKs:** `@supabase/supabase-js` `^2.117.2`, `@supabase/ssr` `^0.12.7`
- **UI Libraries:** `@base-ui/react` `^1.8.0`, `lucide-react` `^1.48.0`, `recharts` `^3.10.1`

### 2.2 Build Execution Result
- **Command:** `npm run build` (executed in `C:\Web Apps\Govind\admin`)
- **Build Status:** **PASS** (`Compiled successfully in 7.1s`, `Finished TypeScript in 7.7s`, Exit Code `0`)
- **Compiled Routes (16 App Routes + 1 API Route + Proxy Middleware):**
  - Static (`○`): `/`, `/_not-found`, `/categories`, `/coupons`, `/customers`, `/daily-rates`, `/deliveries`, `/fresh-board`, `/inventory`, `/login`, `/orders`, `/products`, `/promotions`, `/punjabi-menu`, `/settings`, `/unauthorized`
  - Dynamic (`ƒ`): `/api/checkout`, `/orders/[id]`
  - Middleware (`ƒ`): `Proxy (Middleware)` (`src/proxy.ts`)
- **Non-Fatal Build Warnings:**
  - Multiple `package-lock.json` files detected (`C:\Web Apps\Govind\package-lock.json` and `C:\Web Apps\Govind\admin\package-lock.json`).

---

## 3. CUSTOMER WEBSITE BUILD BASELINE
- **Status:** **N/A (Does Not Exist)**
- **Details:** No standalone customer-facing web application exists in `C:\Web Apps\Govind`. Only `android/` and `admin/` exist.
