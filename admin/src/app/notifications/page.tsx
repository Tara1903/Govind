"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
// using standard relative path or @/ since we are assuming standard alias
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Bell, Send, Users, BarChart3, Plus, RefreshCw, Settings } from "lucide-react";

export default function NotificationsPage() {
  const [stats, setStats] = useState({ sent_today: 0, total_devices: 0, total_unread: 0, active_campaigns: 0 });
  const [campaigns, setCampaigns] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      fetch('/api/notifications/stats').then(r => r.json()),
      fetch('/api/notifications/campaigns').then(r => r.json()),
    ]).then(([statsData, campaignsData]) => {
      setStats(statsData);
      setCampaigns(Array.isArray(campaignsData) ? campaignsData.slice(0, 10) : []);
      setLoading(false);
    });
  }, []);

  const statCards = [
    { label: 'Sent Today', value: stats.sent_today, icon: Send, color: 'text-green-600' },
    { label: 'Active Devices', value: stats.total_devices, icon: Bell, color: 'text-blue-600' },
    { label: 'Active Campaigns', value: stats.active_campaigns, icon: BarChart3, color: 'text-purple-600' },
    { label: 'Total Unread', value: stats.total_unread, icon: Users, color: 'text-orange-600' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
            <Bell className="h-7 w-7 text-green-700" />
            Notifications
          </h2>
          <p className="text-sm text-gray-500">Manage push notifications, campaigns and templates</p>
        </div>
        <div className="flex gap-2">
          <Link href="/notifications/compose" className="w-full sm:w-auto">
            <Button className="bg-green-700 hover:bg-green-800 text-white min-h-[38px] w-full sm:w-auto">
              <Plus className="h-4 w-4 mr-1.5" />
              Compose
            </Button>
          </Link>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((card) => (
          <Card key={card.label} className="shadow-sm">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs text-gray-500 font-medium">{card.label}</p>
                  <p className="text-2xl font-bold text-gray-900 mt-1">
                    {loading ? '...' : card.value.toLocaleString()}
                  </p>
                </div>
                <card.icon className={`h-8 w-8 ${card.color} opacity-70`} />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        {[
          { label: 'Compose', href: '/notifications/compose', icon: Send, desc: 'Send a notification' },
          { label: 'Campaigns', href: '/notifications/campaigns', icon: BarChart3, desc: 'Manage campaigns' },
          { label: 'Templates', href: '/notifications/templates', icon: Bell, desc: 'Message templates' },
          { label: 'Settings', href: '/notifications/settings', icon: Settings, desc: 'Notification settings' },
        ].map((action) => (
          <Link key={action.label} href={action.href}>
            <Card className="shadow-sm hover:shadow-md transition-shadow cursor-pointer h-full">
              <CardContent className="p-4">
                <action.icon className="h-6 w-6 text-green-700 mb-2" />
                <p className="font-semibold text-sm text-gray-900">{action.label}</p>
                <p className="text-xs text-gray-500 mt-0.5">{action.desc}</p>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>

      {/* Recent Campaigns */}
      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base font-semibold">Recent Campaigns</CardTitle>
            <Link href="/notifications/campaigns">
              <Button variant="outline" size="sm">View All</Button>
            </Link>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading...</div>
          ) : campaigns.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">
              No campaigns yet. <Link href="/notifications/compose" className="text-green-700 font-medium">Create your first campaign →</Link>
            </div>
          ) : (
            <div className="space-y-2">
              {campaigns.map((c) => (
                <div key={c.id} className="flex items-center justify-between py-2 border-b last:border-0">
                  <div>
                    <p className="text-sm font-medium text-gray-900">{c.name}</p>
                    <p className="text-xs text-gray-500">{c.audience_type} · {new Date(c.created_at).toLocaleDateString()}</p>
                  </div>
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                    c.status === 'SENT' ? 'bg-green-100 text-green-800' :
                    c.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-800' :
                    c.status === 'DRAFT' ? 'bg-gray-100 text-gray-700' :
                    'bg-red-100 text-red-800'
                  }`}>{c.status}</span>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
