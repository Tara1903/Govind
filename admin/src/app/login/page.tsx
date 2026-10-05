'use client'

import { useState } from 'react'
import { login, verifyOtp, loginWithPassword } from './actions'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { KeyRound, Mail, ShieldCheck, Lock } from 'lucide-react'
import Image from 'next/image'

export default function LoginPage() {
  const [authMode, setAuthMode] = useState<'otp' | 'password'>('password')
  const [email, setEmail] = useState('')
  const [isOtpSent, setIsOtpSent] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleSendOtp = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    const formData = new FormData(e.currentTarget)
    const result = await login(formData)

    if (result?.error) {
      setError(result.error)
    } else if (result?.success) {
      setEmail(result.email)
      setIsOtpSent(true)
    }
    setLoading(false)
  }

  const handleVerifyOtp = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    const formData = new FormData(e.currentTarget)
    formData.append('email', email)
    const result = await verifyOtp(formData)

    if (result?.error) {
      setError(result.error)
    }
    setLoading(false)
  }

  const handlePasswordLogin = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    const formData = new FormData(e.currentTarget)
    const result = await loginWithPassword(formData)

    if (result?.error) {
      setError(result.error)
    }
    setLoading(false)
  }

  return (
    <div className="flex min-h-screen w-full items-center justify-center bg-gradient-to-br from-green-50 via-gray-50 to-emerald-50 p-4">
      <Card className="w-full max-w-md shadow-xl border-gray-200">
        <CardHeader className="space-y-2 text-center pb-4">
          <div className="mx-auto flex justify-center mb-1">
            <Image
              src="/brand/govind-logo-squircle.png"
              alt="Govind Logo"
              width={80}
              height={80}
              className="rounded-2xl shadow-sm object-contain"
              priority
            />
          </div>
          <CardTitle className="text-2xl font-bold text-gray-900">Govind Admin Portal</CardTitle>
          <CardDescription className="text-gray-500 text-xs">
            Fresh and Healthy Food • Restricted Administrative Access
          </CardDescription>

          <div className="flex rounded-lg bg-gray-100 p-1 mt-4 text-xs font-medium">
            <button
              type="button"
              onClick={() => { setAuthMode('password'); setError(null); }}
              className={`flex-1 py-1.5 rounded-md transition-all flex items-center justify-center gap-1.5 ${
                authMode === 'password'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-500 hover:text-gray-900'
              }`}
            >
              <KeyRound className="h-3.5 w-3.5" />
              Password
            </button>
            <button
              type="button"
              onClick={() => { setAuthMode('otp'); setError(null); }}
              className={`flex-1 py-1.5 rounded-md transition-all flex items-center justify-center gap-1.5 ${
                authMode === 'otp'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-500 hover:text-gray-900'
              }`}
            >
              <Mail className="h-3.5 w-3.5" />
              Email OTP
            </button>
          </div>
        </CardHeader>

        <CardContent>
          {error && (
            <div className="mb-4 p-3 rounded-md bg-red-50 border border-red-200 text-xs font-medium text-red-700">
              {error}
            </div>
          )}

          {authMode === 'password' ? (
            <form onSubmit={handlePasswordLogin} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700">Admin Email</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                  <input
                    name="email"
                    type="email"
                    placeholder="admin@govind.com"
                    required
                    className="w-full pl-9 pr-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-green-600 bg-white"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700">Password</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                  <input
                    name="password"
                    type="password"
                    placeholder="••••••••"
                    required
                    className="w-full pl-9 pr-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-green-600 bg-white"
                  />
                </div>
              </div>

              <Button
                type="submit"
                className="w-full bg-green-700 hover:bg-green-800 text-white font-medium"
                disabled={loading}
              >
                {loading ? 'Authenticating...' : 'Sign In as Admin'}
              </Button>
            </form>
          ) : !isOtpSent ? (
            <form onSubmit={handleSendOtp} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700">Admin Email</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                  <input
                    name="email"
                    type="email"
                    placeholder="admin@govind.com"
                    required
                    className="w-full pl-9 pr-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-green-600 bg-white"
                  />
                </div>
              </div>
              <Button
                type="submit"
                className="w-full bg-green-700 hover:bg-green-800 text-white font-medium"
                disabled={loading}
              >
                {loading ? 'Sending Code...' : 'Send One-Time Password'}
              </Button>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700">6-Digit Code for {email}</label>
                <input
                  name="token"
                  type="text"
                  placeholder="123456"
                  maxLength={6}
                  required
                  className="w-full px-3 py-2 text-center text-lg tracking-widest font-mono border rounded-md focus:outline-none focus:ring-2 focus:ring-green-600 bg-white"
                />
              </div>
              <Button
                type="submit"
                className="w-full bg-green-700 hover:bg-green-800 text-white font-medium"
                disabled={loading}
              >
                {loading ? 'Verifying...' : 'Verify & Enter Dashboard'}
              </Button>
              <Button
                type="button"
                variant="ghost"
                className="w-full text-xs text-gray-500"
                onClick={() => setIsOtpSent(false)}
                disabled={loading}
              >
                ← Back to email input
              </Button>
            </form>
          )}

          <div className="mt-6 border-t pt-4 text-center">
            <span className="inline-flex items-center gap-1 text-[11px] text-gray-500">
              <ShieldCheck className="h-3 w-3 text-green-600" />
              Role-Based Access Enforcement Active
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
