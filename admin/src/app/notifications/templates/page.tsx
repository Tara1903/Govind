"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ArrowLeft, Bell } from "lucide-react";

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/notifications/templates')
      .then(r => r.json())
      .then(data => {
        setTemplates(Array.isArray(data) ? data : []);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2">
        <Link href="/notifications">
          <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <h2 className="text-2xl font-bold text-gray-900">Notification Templates</h2>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <CardTitle className="text-base font-semibold">
            Configured Templates ({templates.length})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="py-6 text-center text-sm text-gray-500">Loading templates...</p>
          ) : templates.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-500">No templates found.</p>
          ) : (
            <>
              {/* Mobile Card Feed (block md:hidden) */}
              <div className="block md:hidden space-y-3">
                {templates.map((t) => (
                  <div
                    key={`mob-template-${t.id}`}
                    className="bg-white rounded-xl border border-gray-200 p-3.5 shadow-xs space-y-2.5"
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <h4 className="font-semibold text-sm text-gray-900 leading-tight">
                          {t.title || t.name}
                        </h4>
                        <code className="text-[11px] text-gray-500 font-mono mt-0.5 block">
                          {t.name}
                        </code>
                      </div>
                      <span className={`px-2 py-0.5 rounded-full text-[11px] font-semibold shrink-0 ${
                        t.is_active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
                      }`}>
                        {t.is_active ? 'Active' : 'Inactive'}
                      </span>
                    </div>

                    <div className="pt-1 border-t border-gray-100">
                      <span className="text-[10px] uppercase font-semibold text-emerald-800 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full">
                        {t.type}
                      </span>
                    </div>
                  </div>
                ))}
              </div>

              {/* Desktop Table View (hidden md:block) */}
              <div className="hidden md:block overflow-x-auto">
                <table className="w-full text-sm text-left">
                  <thead className="text-xs text-gray-700 uppercase bg-gray-50 border-b">
                    <tr>
                      <th className="px-4 py-3">Name</th>
                      <th className="px-4 py-3">Type</th>
                      <th className="px-4 py-3">Title</th>
                      <th className="px-4 py-3">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {templates.map(t => (
                      <tr key={t.id} className="border-b">
                        <td className="px-4 py-3 font-medium font-mono text-xs text-gray-900">{t.name}</td>
                        <td className="px-4 py-3 text-gray-600 text-xs">{t.type}</td>
                        <td className="px-4 py-3 text-gray-800 font-medium">{t.title}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                            t.is_active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
                          }`}>
                            {t.is_active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
