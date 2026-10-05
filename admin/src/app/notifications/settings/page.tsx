"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ArrowLeft, RefreshCw } from "lucide-react";

interface FirebaseStatus {
  configured: boolean;
  status: string;
  projectId?: string;
  clientEmail?: string;
  error?: string;
  message?: string;
  instructions?: string[];
}

export default function SettingsPage() {
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<FirebaseStatus | null>(null);

  const checkStatus = async () => {
    setLoading(true);
    try {
      const res = await fetch("/api/notifications/test-firebase");
      const data = await res.json();
      setStatus(data);
    } catch (e: any) {
      setStatus({
        configured: false,
        status: "ERROR",
        error: e.message,
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkStatus();
  }, []);

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Link href="/notifications">
            <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
              <ArrowLeft className="h-4 w-4" />
            </Button>
          </Link>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Notification Settings</h2>
            <p className="text-xs text-gray-500">Configure and test Firebase Cloud Messaging (FCM) credentials.</p>
          </div>
        </div>
        <Button variant="outline" size="sm" onClick={checkStatus} disabled={loading} className="min-h-[38px] w-full sm:w-auto">
          <RefreshCw className={`h-3.5 w-3.5 mr-1.5 ${loading ? "animate-spin" : ""}`} />
          {loading ? "Checking..." : "Refresh Status"}
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Firebase Admin SDK Configuration</CardTitle>
              <CardDescription>Server-side private key status for dispatching push notifications.</CardDescription>
            </div>
            {status && (
              <span
                className={`px-3 py-1 text-xs font-semibold rounded-full ${
                  status.status === "CONNECTED"
                    ? "bg-emerald-100 text-emerald-800"
                    : "bg-amber-100 text-amber-800"
                }`}
              >
                {status.status}
              </span>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-5">
          {status?.status === "CONNECTED" ? (
            <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg space-y-2">
              <div className="flex items-center gap-2 text-emerald-900 font-semibold text-sm">
                <span>✅ Firebase Admin SDK is Connected</span>
              </div>
              <p className="text-xs text-emerald-800">
                Project ID: <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">{status.projectId}</code>
              </p>
              <p className="text-xs text-emerald-800">
                Service Account: <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">{status.clientEmail}</code>
              </p>
              <p className="text-xs text-emerald-700">{status.message}</p>
            </div>
          ) : (
            <div className="p-4 bg-amber-50 border border-amber-200 rounded-lg space-y-3">
              <div className="flex items-center gap-2 text-amber-900 font-semibold text-sm">
                <span>⚠️ Firebase Private Key Needed</span>
              </div>
              <p className="text-xs text-amber-800">
                The Node.js snippet <code>var serviceAccount = require("path/to/serviceAccountKey.json")</code> indicates that Firebase requires the downloaded <strong>serviceAccountKey.json</strong> file.
              </p>
              <div className="text-xs text-gray-700 bg-white p-3 rounded border border-amber-200 space-y-2">
                <p className="font-semibold text-gray-900">How to obtain and place it:</p>
                <ol className="list-decimal list-inside space-y-1 text-gray-600">
                  <li>In Firebase Console, go to <strong>Project settings &gt; Service accounts</strong>.</li>
                  <li>Click the blue button <strong>&quot;Generate new private key&quot;</strong>.</li>
                  <li>Save or move the downloaded file to: <br /><code className="bg-gray-100 px-1 py-0.5 rounded font-mono text-gray-900">C:\Web Apps\Govind\admin\serviceAccountKey.json</code></li>
                  <li>Click <strong>&quot;Test Connection&quot;</strong> below.</li>
                </ol>
              </div>
            </div>
          )}

          <div className="flex items-center gap-3">
            <Button onClick={checkStatus} disabled={loading}>
              {loading ? "Testing..." : "Test Connection"}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
