# GOVIND — PHASE 0 ENVIRONMENT & SECRET INVENTORY

**Recorded At:** 2026-09-29T15:54:00+05:30  
**Security Rule:** Zero secret values are printed in this document. Only variable names, status flags, and file locations are recorded.

---

## 1. ENVIRONMENT FILES INVENTORY

### 1.1 `C:\Web Apps\Govind\admin\.env.local`
- **Tracked by Git:** NO (ignored via `admin/.gitignore`)
- **Variables Present:**
  - `NEXT_PUBLIC_SUPABASE_URL` — `[SET]` (Points to live Supabase project `crkuiuxajywlgmlnklvj`)
  - `NEXT_PUBLIC_SUPABASE_ANON_KEY` — `[SET]`
  - `SUPABASE_SERVICE_ROLE_KEY` — `[SET]`
  - `RAZORPAY_KEY_ID` — `[PLACEHOLDER]` (`rzp_test_placeholder`)
  - `RAZORPAY_KEY_SECRET` — `[PLACEHOLDER]` (`placeholder_secret`)

### 1.2 `C:\Web Apps\Govind\android\local.properties`
- **Tracked by Git:** NO (ignored via `.gitignore` / `android/.gitignore`)
- **Variables Present:**
  - `sdk.dir` — `[SET]` (`C:\\Users\\taras\\AppData\\Local\\Android\\Sdk`)
  - `SUPABASE_URL` — `[SET]` (Points to live Supabase project `crkuiuxajywlgmlnklvj`)
  - `SUPABASE_ANON_KEY` — `[SET]`
  - `RAZORPAY_KEY_ID` — `[PLACEHOLDER]` (`rzp_test_placeholder`)

### 1.3 `C:\Web Apps\Govind\.env`
- **Tracked by Git:** NO (ignored via `.gitignore`)
- **Variables Present:**
  - `SUPABASE_URL` — `[PLACEHOLDER]` (`https://your-project-id.supabase.co`)
  - `SUPABASE_ANON_KEY` — `[PLACEHOLDER]` (`your-anon-key`)
  - `SUPABASE_SERVICE_ROLE_KEY` — `[PLACEHOLDER]` (`your-service-role-key`)
  - `RAZORPAY_KEY_ID` — `[PLACEHOLDER]` (`rzp_test_...`)
  - `RAZORPAY_KEY_SECRET` — `[PLACEHOLDER]` (`your-razorpay-secret`)
  - `RAZORPAY_WEBHOOK_SECRET` — `[PLACEHOLDER]` (`your-webhook-secret`)

### 1.4 `C:\Web Apps\Govind\.env.example`
- **Tracked by Git:** YES
- **Variables Present:** Identical placeholder keys as `.env` (`[PLACEHOLDER]`).

---

## 2. SECRET AUDIT (HARDCODED CREDENTIALS IN SOURCE / SCRIPTS)

| File Path | Secret Type | Git Status | Audit Status | Remediation Note for Phase 1 |
| :--- | :--- | :--- | :--- | :--- |
| `C:\Web Apps\Govind\seed.js` (Line 4) | Supabase `service_role` JWT | Untracked (`??`) — **NOT in `.gitignore`** | `FOUND — VALUE REDACTED` | Read from `process.env.SUPABASE_SERVICE_ROLE_KEY` and add script to `.gitignore` |
| `C:\Web Apps\Govind\check.mjs` (Line 4) | Supabase `service_role` JWT | Untracked (`??`) — **NOT in `.gitignore`** | `FOUND — VALUE REDACTED` | Remove temporary script or read from env + gitignore |
| `C:\Web Apps\Govind\count.js` (Line 4) | Supabase `service_role` JWT | Untracked (`??`) — **NOT in `.gitignore`** | `FOUND — VALUE REDACTED` | Remove temporary script or read from env + gitignore |
| `C:\Web Apps\Govind\test-cat.mjs` (Line 4) | Supabase `service_role` JWT | Untracked (`??`) — **NOT in `.gitignore`** | `FOUND — VALUE REDACTED` | Remove temporary script or read from env + gitignore |
| `C:\Web Apps\Govind\test-insert.mjs` (Line 4) | Supabase `service_role` JWT | Untracked (`??`) — **NOT in `.gitignore`** | `FOUND — VALUE REDACTED` | Remove temporary script or read from env + gitignore |
| `C:\Web Apps\Govind\test-read.mjs` (Line 4) | Supabase `service_role` JWT | Untracked (`??`) — **NOT in `.gitignore`** | `FOUND — VALUE REDACTED` | Remove temporary script or read from env + gitignore |
| `C:\Web Apps\Govind\test-rpc.js` (Line 4) | Supabase `service_role` JWT | Untracked (`??`) — **NOT in `.gitignore`** | `FOUND — VALUE REDACTED` | Remove temporary script or read from env + gitignore |
| `C:\Web Apps\Govind\android\app\build.gradle.kts` (Lines 35-38) | Android Release Keystore Password (`release.keystore`) | Tracked (`M`) | `FOUND — VALUE REDACTED` | Move signing credentials to `local.properties` |
| `C:\Web Apps\Govind\android\app\src\main\java\com\example\govind\data\repository\SupabaseGovindRepositoryImpl.kt` (Line 218) | Hardcoded test API Key header (`X-API-Key`) | Tracked (`M`) | `FOUND — VALUE REDACTED` | Replace with authenticated user JWT in Phase 5 |
| `C:\Web Apps\Govind\android\app\src\main\java\com\example\govind\MainActivity.kt` (Line 47) | Razorpay Test Key Placeholder | Tracked (`M`) | `PLACEHOLDER` | Wire to `BuildConfig` or payment boundary |
