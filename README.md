# Govind - Fresh and Healthy Food

## Architecture
This project is a full-stack solution containing:
- **Android App**: Kotlin, Jetpack Compose, Hilt, Supabase Kotlin SDK. Targeting Android 16.
- **Admin Web Dashboard**: Next.js 14, React, Tailwind CSS, TypeScript.
- **Backend**: Supabase (PostgreSQL, Auth, Storage, Edge Functions).

## Directory Structure
- `/android`: True Native Android Application.
- `/admin`: Next.js Web Admin Dashboard.
- `/supabase`: Supabase migrations and configuration.
- `/docs`: Additional documentation.

## Setup Instructions

### Environment Variables
Create `.env` files in both `/admin` and `/android/app` referencing `.env.example`.

### Supabase Setup
1. Ensure Supabase CLI is installed.
2. Run `supabase init` (if not already done).
3. Run `supabase start` or link to a remote project.
4. Apply migrations: `supabase db push`.

### Android Setup
1. Open `/android` in Android Studio.
2. Add your `google-services.json` if using FCM.
3. Build and Run.

### Admin Setup
1. Navigate to `/admin`.
2. Run `npm install`.
3. Run `npm run dev`.
