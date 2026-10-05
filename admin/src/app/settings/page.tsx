"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Settings as SettingsIcon, Save, RefreshCw, CheckCircle2, Phone, Truck, Bell } from "lucide-react";

export default function SettingsPage() {
  const supabase = createClient();
  const [loading, setLoading] = useState(true);
  const [savingDelivery, setSavingDelivery] = useState(false);
  const [savingBusiness, setSavingBusiness] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Delivery Settings state
  const [deliveryId, setDeliveryId] = useState<string | null>(null);
  const [deliveryData, setDeliveryData] = useState({
    min_order_amount: "0",
    delivery_charge: "40",
    free_delivery_threshold: "500",
    serviceable_pincodes: "110001, 110002, 110003, 110005, 110006",
    active: true,
  });

  // Business Settings state
  const [businessId, setBusinessId] = useState<string | null>(null);
  const [businessData, setBusinessData] = useState({
    whatsapp_number: "+919630937033",
    support_phone: "+919630937033",
    wholesale_contact: "+919630937033",
    delivery_announcement: "FREE HOME DELIVERY ON ORDERS OVER ₹500",
  });

  useEffect(() => {
    fetchSettings();
  }, []);

  async function fetchSettings() {
    setLoading(true);
    // 1. Fetch delivery_settings
    const { data: ds } = await supabase
      .from("delivery_settings")
      .select("*")
      .limit(1);

    if (ds && ds.length > 0) {
      const d = ds[0];
      setDeliveryId(d.id);
      let pincodesStr = "";
      if (Array.isArray(d.serviceable_pincodes)) {
        pincodesStr = d.serviceable_pincodes.join(", ");
      } else if (typeof d.serviceable_pincodes === "string") {
        pincodesStr = d.serviceable_pincodes;
      }
      setDeliveryData({
        min_order_amount: (d.min_order_amount ?? 0).toString(),
        delivery_charge: (d.delivery_charge ?? 40).toString(),
        free_delivery_threshold: (d.free_delivery_threshold ?? 500).toString(),
        serviceable_pincodes: pincodesStr || "110001, 110002",
        active: d.active ?? true,
      });
    }

    // 2. Fetch business_settings
    const { data: bs } = await supabase
      .from("business_settings")
      .select("*")
      .limit(1);

    if (bs && bs.length > 0) {
      const b = bs[0];
      setBusinessId(b.id);
      setBusinessData({
        whatsapp_number: b.whatsapp_number || "+919630937033",
        support_phone: b.support_phone || "+919630937033",
        wholesale_contact: b.wholesale_contact || "+919630937033",
        delivery_announcement: b.delivery_announcement || "FREE HOME DELIVERY",
      });
    }
    setLoading(false);
  }

  const handleSaveDelivery = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingDelivery(true);
    setSuccessMessage(null);

    const pincodesArray = deliveryData.serviceable_pincodes
      .split(",")
      .map((p) => p.trim())
      .filter(Boolean);

    const payload = {
      min_order_amount: parseFloat(deliveryData.min_order_amount) || 0,
      delivery_charge: parseFloat(deliveryData.delivery_charge) || 0,
      free_delivery_threshold: parseFloat(deliveryData.free_delivery_threshold) || 500,
      serviceable_pincodes: pincodesArray,
      active: deliveryData.active,
      updated_at: new Date().toISOString(),
    };

    if (deliveryId) {
      await supabase.from("delivery_settings").update(payload).eq("id", deliveryId);
    } else {
      const { data } = await supabase.from("delivery_settings").insert([payload]).select();
      if (data && data.length > 0) setDeliveryId(data[0].id);
    }

    setSavingDelivery(false);
    setSuccessMessage("Delivery configuration saved successfully!");
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const handleSaveBusiness = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingBusiness(true);
    setSuccessMessage(null);

    const payload = {
      whatsapp_number: businessData.whatsapp_number.trim(),
      support_phone: businessData.support_phone.trim(),
      wholesale_contact: businessData.wholesale_contact.trim(),
      delivery_announcement: businessData.delivery_announcement.trim(),
      updated_at: new Date().toISOString(),
    };

    if (businessId) {
      await supabase.from("business_settings").update(payload).eq("id", businessId);
    } else {
      const { data } = await supabase.from("business_settings").insert([payload]).select();
      if (data && data.length > 0) setBusinessId(data[0].id);
    }

    setSavingBusiness(false);
    setSuccessMessage("Store & contact settings saved successfully!");
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <SettingsIcon className="h-7 w-7 text-gray-700" />
            System & Store Settings
          </h2>
          <p className="text-sm text-gray-500">
            Configure delivery rules, free shipping thresholds, serviceable pincodes, and support contacts
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchSettings} disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
          Reload
        </Button>
      </div>

      {successMessage && (
        <div className="p-3 bg-green-50 border border-green-200 text-green-800 text-xs rounded-lg flex items-center gap-2 font-medium">
          <CheckCircle2 className="h-4 w-4 text-green-600 shrink-0" />
          {successMessage}
        </div>
      )}

      {/* Delivery Rules Form */}
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <Truck className="h-4 w-4 text-green-700" />
            Delivery Rules & Charges
          </CardTitle>
          <CardDescription className="text-xs">
            Controls the pricing logic applied automatically during customer checkout
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSaveDelivery} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Minimum Order (₹)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                  value={deliveryData.min_order_amount}
                  onChange={(e) => setDeliveryData({ ...deliveryData, min_order_amount: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Standard Delivery Fee (₹)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white font-medium focus:ring-2 focus:ring-green-700 outline-none"
                  value={deliveryData.delivery_charge}
                  onChange={(e) => setDeliveryData({ ...deliveryData, delivery_charge: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Free Shipping Threshold (₹)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white font-medium text-green-700 focus:ring-2 focus:ring-green-700 outline-none"
                  value={deliveryData.free_delivery_threshold}
                  onChange={(e) => setDeliveryData({ ...deliveryData, free_delivery_threshold: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Serviceable Pincodes (Comma separated)
              </label>
              <textarea
                className="w-full border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white font-mono text-xs h-20 focus:ring-2 focus:ring-green-700 outline-none"
                placeholder="110001, 110002, 110003..."
                value={deliveryData.serviceable_pincodes}
                onChange={(e) => setDeliveryData({ ...deliveryData, serviceable_pincodes: e.target.value })}
              />
              <p className="text-[11px] text-gray-400 mt-1">
                Customers entering an address outside these pincodes will receive an unserviceable notification.
              </p>
            </div>

            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="del_active"
                checked={deliveryData.active}
                onChange={(e) => setDeliveryData({ ...deliveryData, active: e.target.checked })}
                className="h-4 w-4 rounded text-green-700 cursor-pointer"
              />
              <label htmlFor="del_active" className="text-xs font-semibold text-gray-700 cursor-pointer">
                Delivery System Active
              </label>
            </div>

            <Button type="submit" disabled={savingDelivery} className="min-h-[42px] w-full sm:w-auto bg-green-700 hover:bg-green-800 text-white text-xs font-semibold">
              <Save className="h-3.5 w-3.5 mr-1.5" />
              {savingDelivery ? "Saving Changes..." : "Save Delivery Rules"}
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* Business Contacts & Announcement */}
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <Phone className="h-4 w-4 text-blue-700" />
            Support Channels & Announcements
          </CardTitle>
          <CardDescription className="text-xs">
            Helpline numbers and announcement banner displayed to customers
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSaveBusiness} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  WhatsApp Support Number
                </label>
                <input
                  type="text"
                  required
                  className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white font-mono focus:ring-2 focus:ring-blue-700 outline-none"
                  value={businessData.whatsapp_number}
                  onChange={(e) => setBusinessData({ ...businessData, whatsapp_number: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Customer Helpline Phone
                </label>
                <input
                  type="text"
                  required
                  className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white font-mono focus:ring-2 focus:ring-blue-700 outline-none"
                  value={businessData.support_phone}
                  onChange={(e) => setBusinessData({ ...businessData, support_phone: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Wholesale Enquiry Contact
                </label>
                <input
                  type="text"
                  required
                  className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white font-mono focus:ring-2 focus:ring-blue-700 outline-none"
                  value={businessData.wholesale_contact}
                  onChange={(e) => setBusinessData({ ...businessData, wholesale_contact: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Store Announcement Marquee
              </label>
              <input
                type="text"
                className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-700 outline-none"
                value={businessData.delivery_announcement}
                onChange={(e) => setBusinessData({ ...businessData, delivery_announcement: e.target.value })}
              />
            </div>

            <Button type="submit" disabled={savingBusiness} className="min-h-[42px] w-full sm:w-auto bg-blue-700 hover:bg-blue-800 text-white text-xs font-semibold">
              <Save className="h-3.5 w-3.5 mr-1.5" />
              {savingBusiness ? "Saving Contacts..." : "Save Store Contacts"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
