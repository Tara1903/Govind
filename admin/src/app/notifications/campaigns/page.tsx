"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Plus, ArrowLeft, Send } from "lucide-react";

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/notifications/campaigns')
      .then(r => r.json())
      .then(data => {
        setCampaigns(Array.isArray(data) ? data : []);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Link href="/notifications">
            <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
              <ArrowLeft className="h-4 w-4" />
            </Button>
          </Link>
          <h2 className="text-2xl font-bold text-gray-900">Push Campaigns</h2>
        </div>
        <Link href="/notifications/compose">
          <Button size="sm" className="bg-green-700 hover:bg-green-800 text-white min-h-[38px] w-full sm:w-auto">
            <Plus className="h-4 w-4 mr-1.5" />
            New Campaign
          </Button>
        </Link>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <CardTitle className="text-base font-semibold">
            All Campaigns ({campaigns.length})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="py-6 text-center text-sm text-gray-500">Loading campaigns...</p>
          ) : campaigns.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">
              No campaigns found. <Link href="/notifications/compose" className="text-green-700 font-semibold underline">Create one now</Link>
            </div>
          ) : (
            <>
              {/* Mobile Card Feed (block md:hidden) */}
              <div className="block md:hidden space-y-3">
                {campaigns.map((c) => (
                  <div
                    key={`mob-camp-${c.id}`}
                    className="bg-white rounded-xl border border-gray-200 p-3.5 shadow-xs space-y-2.5"
                  >
                    <div className="flex items-start justify-between gap-2">
                      <h4 className="font-semibold text-sm text-gray-900 leading-tight">
                        {c.name}
                      </h4>
                      <span className={`px-2 py-0.5 rounded-full text-[11px] font-semibold shrink-0 ${
                        c.status === 'SENT' ? 'bg-green-100 text-green-800' :
                        c.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-800' :
                        c.status === 'DRAFT' ? 'bg-gray-100 text-gray-700' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {c.status}
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-xs text-gray-500 pt-1 border-t border-gray-100">
                      <span className="font-medium bg-gray-50 px-2 py-0.5 rounded border border-gray-100">
                        {c.audience_type}
                      </span>
                      <span>{new Date(c.created_at).toLocaleDateString()}</span>
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
                      <th className="px-4 py-3">Audience</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3">Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {campaigns.map(c => (
                      <tr key={c.id} className="border-b">
                        <td className="px-4 py-3 font-medium text-gray-900">{c.name}</td>
                        <td className="px-4 py-3 text-gray-600">{c.audience_type}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                            c.status === 'SENT' ? 'bg-green-100 text-green-800' :
                            c.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-800' :
                            c.status === 'DRAFT' ? 'bg-gray-100 text-gray-700' :
                            'bg-red-100 text-red-800'
                          }`}>
                            {c.status}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-gray-500">{new Date(c.created_at).toLocaleDateString()}</td>
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
