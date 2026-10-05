# GOVIND — Phase 12: Official Brand Asset & Logo Integration Baseline

## 1. Executive Summary

This document establishes the official brand baseline for the **GOVIND** platform across the Android application and Admin Next.js panel. The two official, finalized logo assets provided by the user have been integrated across all designated touchpoints with strict adherence to brand guidelines: zero modification of artwork, no SVG/text redraws, preservation of official brand colors, and careful aspect ratio containment.

---

## 2. Official Brand Assets Specification

| Asset ID | File Name | Role & Approved Contexts | Dimensions | Format |
|---|---|---|---|---|
| **SQUIRCLE ICON** | `govind_logo_squircle.png` | **Primary Android Launcher Icon**, Recent Apps identity, Splash Screen centered brand mark, Authentication/Login primary badge | 975 × 1024 | Lossless PNG (Alpha channel) |
| **CIRCLE ICON** | `govind_logo_circle.png` | **Customer Profile / Account Avatar**, Browser Favicon (`favicon.ico`, `icon.png`), Admin Sidebar brand mark | 975 × 1024 | Lossless PNG |
| **NOTIFICATION SILHOUETTE** | `ic_notification.xml` | Android status bar & notification shade small-icon (monochrome white silhouette with transparent canvas) | 24 × 24 dp | Vector XML |

---

## 3. Brand Colors & Typography

The visual identity preserves the exact color palette of the GOVIND master brand:

- **Primary Dark Green**: `#064520` (Title text, primary buttons, status bars)
- **Fresh Leaf Green**: `#38802A` (Leaf motif, notification accents, delivery tags)
- **Accent Orange**: `#F5450D` (The iconic orange *"o"* accent, discount badges, urgent tags)
- **Warm White / Cream**: `#FEFCF5` (Splash screen background, auth background, card fill)
- **Pure White**: `#FFFFFF` (Surface cards, dialogs, button text)

---

## 4. Android Implementation Matrix

### 4.1. Launcher & Recent Apps Identity
- **Adaptive Icon XML (`mipmap-anydpi-v26/ic_launcher.xml` & `ic_launcher_round.xml`)**:
  - Background: `@android:color/white`
  - Foreground: `@drawable/govind_logo_squircle`
  - Monochrome (Themed Icons Android 13+): `@drawable/ic_notification`
- **Fallback Drawables (`mipmap-xxxhdpi/ic_launcher.png` & `ic_launcher_round.png`)**: High-resolution 192×192 rendering with safe padding.
- **Safe Zone Compliance**: The inner logo text ("Govind", leaf, tagline, underline) occupies coordinates X: [230, 813] (59% width) and Y: [302, 731] (41% height), placing it safely within the central 66.7% diameter circle required by circular, squircle, and rounded-rect launcher masks.
- **Recent Apps (`KEYCODE_APP_SWITCH`)**: Displays the official squircle logo in the app switcher header alongside the app name "Govind".

### 4.2. Splash Screen (`SplashScreen.kt`)
- **Background**: `GovindTheme.colors.warmWhite` (`#FEFCF5`).
- **Logo Presentation**: Centered `govind_logo_squircle` at `180.dp` with `ContentScale.Fit`. Generous safe padding, zero stretching.
- **Timing**: 1000ms delay before transitioning to destination (Home for logged-in users, Auth for logged-out users).

### 4.3. Authentication / Login (`AuthScreen.kt`)
- **Background**: `GovindTheme.colors.warmWhite`.
- **Brand Badge**: Official `govind_logo_squircle` displayed centrally at `136.dp` with `ContentScale.Fit`.
- **Typography**: Clean header "Welcome to Govind" and "Login or Sign up to continue" with Govind green CTA button.

### 4.4. Account Hub / Profile (`ProfileScreen.kt`)
- **Circular Avatar**: Circular container (`64.dp`, `CircleShape`) displaying the official circular badge `govind_logo_circle.png` with `ContentScale.Fit`.
- **Visual Impact**: Seamless integration with the VIP member gradient card (`#002D11` to `primaryContainer`).

### 4.5. Legacy Asset Cleanup
- Removed legacy unapproved assets from `drawable-nodpi/` (`logo.jpg`, `ic_launcher_squircle.jpg`, `ic_launcher_round_img.jpg`).
- Placed clean `logo.png` alias in `drawable/` pointing to the official squircle icon for full backward compatibility.

---

## 5. Admin Panel & Web Implementation

### 5.1. Sidebar Navigation (`admin/src/components/sidebar.tsx`)
- Replaced previous generic green square *"G"* with the official circular brand mark (`/brand/govind-logo-circle.png`, 38×38 px, `object-contain`).
- Added official brand tagline *"Fresh and Healthy Food"* in emerald green under the header title *"Govind Admin"*.

### 5.2. Admin Login (`admin/src/app/login/page.tsx`)
- Replaced previous green placeholder block with the official squircle logo (`/brand/govind-logo-squircle.png`, 80×80 px, `object-contain`, `priority`).
- Added tagline *"Fresh and Healthy Food • Restricted Administrative Access"*.

### 5.3. Web Metadata & Favicon (`admin/src/app/layout.tsx`)
- Deployed `/brand/govind-logo-circle.png` to:
  - `admin/src/app/icon.png` (Next.js App Router automated icon route)
  - `admin/public/favicon.ico` (Browser legacy favicon fallback)
  - `admin/public/brand/govind-logo-circle.png`
  - `admin/public/brand/govind-logo-squircle.png`

---

## 6. Verification & Visual QA Checklist

| Checkpoint | Target | Result | Evidence |
|---|---|---|---|
| Android Compilation | `./gradlew.bat assembleDebug` | **BUILD SUCCESSFUL** (2m 50s) | APK generated at `app/build/outputs/apk/debug/app-debug.apk` |
| Admin Panel Compilation | `npm run build` | **0 errors, 29 static pages** | Production build verified with Next.js Turbopack |
| Launcher / Recent Apps Icon | Pixel 10 Pro (API 36) | **VERIFIED** | `screen_logo_recents.png` shows squircle badge in Recent Apps |
| Splash Screen | Pixel 10 Pro | **VERIFIED** | Centered squircle logo on `#FEFCF5` warm white |
| Auth Screen | Pixel 10 Pro | **VERIFIED** | `screen_logo_auth_success.png` displays squircle brand badge |
| Profile Screen Avatar | Pixel 10 Pro | **VERIFIED** | `screen_logo_profile_routed.png` displays circular badge avatar |
| Admin Brand Assets HTTP | `localhost:3005` | **200 OK** | Verified `/brand/govind-logo-squircle.png` & `/brand/govind-logo-circle.png` |
| Safe Area & No Distortion | All Touchpoints | **100% Preserved** | All rendering uses `ContentScale.Fit` / `object-contain` |
