import { NextResponse } from 'next/server'
import { getFirebaseStatus, initFirebase, getFirebaseMessaging } from '@/lib/firebase-admin'

export async function GET() {
  try {
    const status = getFirebaseStatus()
    
    if (!status.configured) {
      return NextResponse.json({
        configured: false,
        status: 'AWAITING_SERVICE_ACCOUNT_KEY',
        message: 'Firebase Admin SDK requires a valid service account private key JSON file.',
        instructions: [
          '1. Go to Firebase Console -> Project Settings -> Service accounts',
          '2. Click the blue button "Generate new private key"',
          '3. Save the downloaded file as "serviceAccountKey.json" inside C:\\Web Apps\\Govind\\admin\\',
          '4. Click "Test Connection" again.',
        ],
      }, { status: 200 })
    }

    const initResult = initFirebase()
    if (!initResult.success) {
      return NextResponse.json({
        configured: false,
        status: 'INITIALIZATION_FAILED',
        error: initResult.error,
      }, { status: 500 })
    }

    const messaging = getFirebaseMessaging()

    return NextResponse.json({
      configured: true,
      status: 'CONNECTED',
      projectId: status.projectId,
      clientEmail: status.clientEmail,
      messagingAvailable: !!messaging,
      message: 'Firebase Admin SDK initialized successfully and ready to dispatch FCM push notifications.',
    }, { status: 200 })
  } catch (error: any) {
    return NextResponse.json({
      configured: false,
      status: 'ERROR',
      error: error.message,
    }, { status: 500 })
  }
}
