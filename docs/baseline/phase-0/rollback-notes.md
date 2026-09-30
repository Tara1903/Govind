# GOVIND — PHASE 0 ROLLBACK & RECOVERY INSTRUCTIONS

**Recorded At:** 2026-09-29T15:54:00+05:30

---

## 1. SOURCE CODE & WORKING TREE ROLLBACK

Because the repository had 43 modified tracked files and 46 untracked files (including untracked root helper scripts containing plaintext credentials and an untracked root `node_modules/`), Phase 0 preserved the exact pre-repair working tree inside a clean local ZIP snapshot rather than forcing an unsafe Git commit.

### Snapshot Archive Location
- **Archive Path:** `C:\Web Apps\Govind\docs\baseline\phase-0\govind-phase-0-source-snapshot.zip`
- **Included Directories & Files:**
  - `android/app/src`, `android/app/build.gradle.kts`, `android/app/proguard-rules.pro`, `android/build.gradle.kts`, `android/settings.gradle.kts`, `android/gradle.properties`, `android/gradle/`
  - `admin/src`, `admin/public`, `admin/package.json`, `admin/next.config.ts`, `admin/tsconfig.json`, `admin/postcss.config.mjs`, `admin/eslint.config.mjs`
  - `supabase/migrations`, `supabase/functions`, `supabase/seed.sql`
  - `.gitignore`, `README.md`, `package.json`
- **Excluded from Archive (for safety & size):**
  - `node_modules/`, `admin/node_modules/`, `admin/.next/`
  - `android/.gradle/`, `android/build/`, `android/app/build/`
  - `.git/`, `.idea/`
  - Secret files (`admin/.env.local`, `android/local.properties`, `.env`, and root scripts containing hardcoded keys)

### How to Restore the Working Tree to Phase 0 State
1. **To revert any future edits back to the exact Phase 0 working-tree state:**
   ```powershell
   Expand-Archive -Path "C:\Web Apps\Govind\docs\baseline\phase-0\govind-phase-0-source-snapshot.zip" -DestinationPath "C:\Web Apps\Govind" -Force
   ```
2. **To revert tracked files to the last committed Git HEAD (`c1ad2351f278d9199498efb7364dc05e4b070692`):**
   ```powershell
   Set-Location "C:\Web Apps\Govind"
   git checkout -- .
   ```
   *(Warning: Running `git checkout -- .` discards the 43 uncommitted working-tree modifications that existed prior to Phase 0; always use `govind-phase-0-source-snapshot.zip` if you want to return to the exact Phase 0 state.)*

---

## 2. SUPABASE DATABASE BACKUP & ROLLBACK NOTES

### Live Database Snapshot Status
- **Direct `pg_dump` Status:** Not available without the PostgreSQL database password (`SUPABASE_DB_PASSWORD` is not present in `.env.local` or `local.properties`), so a raw `pg_dump` binary dump was not executed in Phase 0.
- **Read-Only JSON Data Snapshot:** Because only 4 tables in the live Supabase database contain data (`categories`: 16 rows, `products`: 85 rows, `product_images`: 79 rows, `delivery_settings`: 1 row; all other 12 tables have `0` rows), Phase 0 exported a complete read-only JSON snapshot of all non-empty public tables to:
  - `C:\Web Apps\Govind\docs\baseline\phase-0\supabase-live-data-snapshot.json`
- **Schema Baseline:** All 10 SQL migration files in `C:\Web Apps\Govind\supabase\migrations\` are preserved in `govind-phase-0-source-snapshot.zip`.
