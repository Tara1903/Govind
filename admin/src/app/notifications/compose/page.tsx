"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";

export default function ComposeNotificationPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [templates, setTemplates] = useState<any[]>([]);
  const [formData, setFormData] = useState({
    user_id: "",
    template_name: "",
    custom_title: "",
    custom_body: "",
    custom_deep_link: "",
    audience_type: "ALL_CUSTOMERS",
    notification_type: "MARKETING"
  });

  useEffect(() => {
    fetch('/api/notifications/templates')
      .then(r => r.json())
      .then(data => Array.isArray(data) ? setTemplates(data) : setTemplates([]))
      .catch(console.error);
  }, []);

  const handleSubmit = async (e: React.FormEvent, isTest: boolean = false) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const res = await fetch('/api/notifications/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...formData,
          user_id: isTest ? formData.user_id : (formData.audience_type === 'DIRECT' ? formData.user_id : null),
        }),
      });
      
      if (!res.ok) throw new Error(await res.text());
      
      alert(isTest ? "Test sent successfully!" : "Notification sent/queued successfully!");
      if (!isTest) router.push('/notifications');
    } catch (err: any) {
      alert(`Error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-2">
        <Link href="/notifications">
          <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <h2 className="text-2xl font-bold text-gray-900">Compose Notification</h2>
      </div>

      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="text-base font-semibold">Message Details</CardTitle>
          <CardDescription className="text-xs">Configure your push notification content and target audience.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Audience Type</label>
              <select 
                className="w-full min-h-[42px] border border-gray-300 rounded-lg p-2.5 text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                value={formData.audience_type}
                onChange={e => setFormData({ ...formData, audience_type: e.target.value })}
              >
                <option value="ALL_CUSTOMERS">All Customers</option>
                <option value="FRESH_CUSTOMERS">Fresh Customers</option>
                <option value="DIRECT">Direct to User</option>
              </select>
            </div>

            {formData.audience_type === 'DIRECT' && (
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">User ID</label>
                <input 
                  type="text" 
                  className="w-full min-h-[42px] border border-gray-300 rounded-lg p-2.5 text-sm bg-white font-mono focus:ring-2 focus:ring-green-700 outline-none"
                  value={formData.user_id}
                  onChange={e => setFormData({ ...formData, user_id: e.target.value })}
                  placeholder="Enter specific User ID"
                />
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Template (Optional)</label>
              <select 
                className="w-full min-h-[42px] border border-gray-300 rounded-lg p-2.5 text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                value={formData.template_name}
                onChange={e => setFormData({ ...formData, template_name: e.target.value })}
              >
                <option value="">-- Custom Message --</option>
                {templates.map(t => (
                  <option key={t.id} value={t.name}>{t.name}</option>
                ))}
              </select>
            </div>

            {!formData.template_name && (
              <>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Custom Title</label>
                  <input 
                    type="text" 
                    className="w-full min-h-[42px] border border-gray-300 rounded-lg p-2.5 text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.custom_title}
                    onChange={e => setFormData({ ...formData, custom_title: e.target.value })}
                    placeholder="e.g. Fresh Mangoes Just Arrived!"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Custom Body</label>
                  <textarea 
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none h-24"
                    value={formData.custom_body}
                    onChange={e => setFormData({ ...formData, custom_body: e.target.value })}
                    placeholder="Enter notification message text..."
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Deep Link (Optional)</label>
                  <input 
                    type="text" 
                    className="w-full min-h-[42px] border border-gray-300 rounded-lg p-2.5 text-sm bg-white font-mono focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.custom_deep_link}
                    onChange={e => setFormData({ ...formData, custom_deep_link: e.target.value })}
                    placeholder="govind://products"
                  />
                </div>
              </>
            )}

            <div className="flex flex-col sm:flex-row gap-3 pt-2">
              <Button 
                type="button" 
                variant="outline"
                className="min-h-[42px] flex-1 text-xs"
                disabled={loading || !formData.user_id}
                onClick={(e) => handleSubmit(e, true)}
              >
                Send Test
              </Button>
              <Button 
                type="button" 
                className="min-h-[42px] flex-1 bg-green-700 hover:bg-green-800 text-white font-semibold text-xs"
                disabled={loading}
                onClick={(e) => handleSubmit(e, false)}
              >
                {loading ? "Sending..." : "Send Now"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
