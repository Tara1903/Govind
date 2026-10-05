import { initializeApp, getApps, cert, App } from 'firebase-admin/app'
import { getMessaging } from 'firebase-admin/messaging'
import fs from 'fs'
import path from 'path'

let initialized = false
let initError: string | null = null
let appInstance: App | null = null

function getServiceAccount(): any | null {
  // 1. Try FCM_SERVICE_ACCOUNT_JSON env var
  const envJson = process.env.FCM_SERVICE_ACCOUNT_JSON
  if (envJson && !envJson.includes('YOUR_FIREBASE')) {
    try {
      return JSON.parse(envJson)
    } catch (e: any) {
      console.error('Failed to parse FCM_SERVICE_ACCOUNT_JSON env var:', e)
    }
  }

  // 2. Try serviceAccountKey.json in the current working directory
  const defaultPath = path.join(process.cwd(), 'serviceAccountKey.json')
  if (fs.existsSync(/*turbopackIgnore: true*/ defaultPath)) {
    try {
      const content = fs.readFileSync(/*turbopackIgnore: true*/ defaultPath, 'utf-8')
      return JSON.parse(content)
    } catch (e: any) {
      console.error(`Failed to read/parse service account from ${defaultPath}:`, e)
    }
  }

  // 3. Try parent folder
  const parentPath = path.join(process.cwd(), '..', 'serviceAccountKey.json')
  if (fs.existsSync(/*turbopackIgnore: true*/ parentPath)) {
    try {
      const content = fs.readFileSync(/*turbopackIgnore: true*/ parentPath, 'utf-8')
      return JSON.parse(content)
    } catch (e: any) {
      console.error(`Failed to read/parse service account from ${parentPath}:`, e)
    }
  }

  return null
}

export function initFirebase(): { success: boolean; error?: string } {
  if (initialized && getApps().length > 0) {
    return { success: true }
  }

  const serviceAccount = getServiceAccount()
  if (!serviceAccount) {
    const msg = 'No service account key found. Please provide serviceAccountKey.json or set FCM_SERVICE_ACCOUNT_JSON.'
    initError = msg
    return { success: false, error: msg }
  }

  try {
    appInstance = initializeApp({
      credential: cert(serviceAccount),
      projectId: serviceAccount.project_id || 'govind-826d0',
    }, 'govind-admin-app')
    initialized = true
    initError = null
    return { success: true }
  } catch (e: any) {
    initError = e.message
    console.error('Firebase Admin init failed:', e)
    return { success: false, error: e.message }
  }
}

export function getFirebaseMessaging() {
  const result = initFirebase()
  if (!result.success || !appInstance) return null
  return getMessaging(appInstance)
}

export function isFirebaseConfigured(): boolean {
  return !!getServiceAccount()
}

export function getFirebaseStatus(): { configured: boolean; error?: string; projectId?: string; clientEmail?: string } {
  const account = getServiceAccount()
  if (!account) {
    return { configured: false, error: initError || 'Key file not found' }
  }
  return {
    configured: true,
    projectId: account.project_id,
    clientEmail: account.client_email,
  }
}

