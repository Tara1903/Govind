"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Ticket, Plus, Edit3, Trash2, RefreshCw, Search } from "lucide-react";

export default function CouponsPage() {
  const supabase = createClient();
  const [coupons, setCoupons] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  // Form state
  const [isEditing, setIsEditing] = useState(false);
  const [currentCoupon, setCurrentCoupon] = useState<any | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  const initialFormState = {
    code: "",
    discount_type: "percentage",
    discount_value: "",
    min_order_amount: "0",
    max_discount_amount: "",
    expiration_date: "",
    active: true,
  };

  const [formData, setFormData] = useState(initialFormState);

  useEffect(() => {
    fetchCoupons();
  }, []);

  async function fetchCoupons() {
    setLoading(true);
    const { data, error } = await supabase
      .from("coupons")
      .select("*")
      .order("created_at", { ascending: false });

    if (!error && data) {
      setCoupons(data);
    }
    setLoading(false);
  }

  const handleAdd = () => {
    setCurrentCoupon(null);
    setFormData(initialFormState);
    setFormError(null);
    setIsEditing(true);
  };

  const handleEdit = (coupon: any) => {
    setCurrentCoupon(coupon);
    setFormData({
      code: coupon.code || "",
      discount_type: coupon.discount_type || "percentage",
      discount_value: (coupon.discount_value || "").toString(),
      min_order_amount: (coupon.min_order_amount ?? 0).toString(),
      max_discount_amount: (coupon.max_discount_amount ?? "").toString(),
      expiration_date: coupon.expiration_date ? coupon.expiration_date.split("T")[0] : "",
      active: coupon.active ?? true,
    });
    setFormError(null);
    setIsEditing(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    const val = parseFloat(formData.discount_value);
    if (!formData.code.trim() || isNaN(val) || val <= 0) {
      setFormError("Valid coupon code and discount value are required.");
      return;
    }

    const payload: any = {
      code: formData.code.trim().toUpperCase(),
      discount_type: formData.discount_type,
      discount_value: val,
      min_order_amount: parseFloat(formData.min_order_amount) || 0,
      max_discount_amount: formData.max_discount_amount ? parseFloat(formData.max_discount_amount) : null,
      expiration_date: formData.expiration_date ? new Date(formData.expiration_date).toISOString() : null,
      active: formData.active,
      updated_at: new Date().toISOString(),
    };

    if (currentCoupon) {
      const { error } = await supabase
        .from("coupons")
        .update(payload)
        .eq("id", currentCoupon.id);

      if (error) {
        setFormError(error.message);
        return;
      }
    } else {
      const { error } = await supabase.from("coupons").insert([payload]);
      if (error) {
        setFormError(error.message);
        return;
      }
    }

    setIsEditing(false);
    fetchCoupons();
  };

  const toggleCouponActive = async (id: string, currentActive: boolean) => {
    await supabase.from("coupons").update({ active: !currentActive }).eq("id", id);
    setCoupons((prev) =>
      prev.map((c) => (c.id === id ? { ...c, active: !currentActive } : c))
    );
  };

  const handleDelete = async (coupon: any) => {
    if (confirm(`Are you sure you want to delete coupon code "${coupon.code}"?`)) {
      const { error } = await supabase.from("coupons").delete().eq("id", coupon.id);
      if (error) {
        alert("Error deleting coupon: " + error.message);
      } else {
        fetchCoupons();
      }
    }
  };

  const filteredCoupons = coupons.filter((c) =>
    c.code.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <Ticket className="h-7 w-7 text-green-700" />
            Coupons & Discounts
          </h2>
          <p className="text-sm text-gray-500">
            Create and manage promotional discount vouchers for checkout
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchCoupons} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </Button>
          {!isEditing && (
            <Button size="sm" onClick={handleAdd} className="bg-green-700 hover:bg-green-800 text-white">
              <Plus className="h-4 w-4 mr-1.5" />
              Add Coupon
            </Button>
          )}
        </div>
      </div>

      {isEditing ? (
        <Card className="shadow-sm max-w-xl">
          <CardHeader>
            <CardTitle>{currentCoupon ? "Edit Coupon" : "Create New Coupon"}</CardTitle>
            <CardDescription>Define discount percentages, flat cuts, minimum spends, and expiry.</CardDescription>
          </CardHeader>
          <CardContent>
            {formError && (
              <div className="mb-4 p-3 rounded bg-red-50 border border-red-200 text-xs text-red-700">
                {formError}
              </div>
            )}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Coupon Code *</label>
                  <input
                    required
                    type="text"
                    placeholder="e.g. GOVIND50"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm uppercase font-mono font-bold bg-white focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.code}
                    onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Discount Type</label>
                  <select
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.discount_type}
                    onChange={(e) => setFormData({ ...formData, discount_type: e.target.value })}
                  >
                    <option value="percentage">Percentage (%)</option>
                    <option value="fixed">Fixed Flat (₹)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Discount Value {formData.discount_type === "percentage" ? "(%)" : "(₹)"} *
                  </label>
                  <input
                    required
                    type="number"
                    step="0.01"
                    placeholder="e.g. 20 or 50"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white font-semibold focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.discount_value}
                    onChange={(e) => setFormData({ ...formData, discount_value: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Min Order Amount (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="e.g. 299"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.min_order_amount}
                    onChange={(e) => setFormData({ ...formData, min_order_amount: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Max Discount Cap (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="e.g. 100"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.max_discount_amount}
                    onChange={(e) => setFormData({ ...formData, max_discount_amount: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Expiration Date</label>
                  <input
                    type="date"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-green-700 outline-none"
                    value={formData.expiration_date}
                    onChange={(e) => setFormData({ ...formData, expiration_date: e.target.value })}
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <input
                  type="checkbox"
                  id="coupon_active"
                  checked={formData.active}
                  onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                  className="h-4 w-4 rounded text-green-700 cursor-pointer"
                />
                <label htmlFor="coupon_active" className="text-xs font-semibold text-gray-700 cursor-pointer">
                  Active (valid for customer checkout)
                </label>
              </div>

              <div className="flex gap-2 pt-2">
                <Button type="submit" className="min-h-[42px] flex-1 sm:flex-initial bg-green-700 hover:bg-green-800 text-white font-semibold">
                  Save Coupon
                </Button>
                <Button type="button" variant="outline" onClick={() => setIsEditing(false)} className="min-h-[42px] flex-1 sm:flex-initial">
                  Cancel
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : (
        <Card className="shadow-sm">
          <CardHeader className="pb-3">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <CardTitle className="text-base font-semibold">
                  Configured Coupons ({filteredCoupons.length})
                </CardTitle>
                <CardDescription className="text-xs">
                  Active vouchers are automatically verified by the Pricing & Checkout engine
                </CardDescription>
              </div>
              <div className="relative w-full sm:w-auto">
                <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search code..."
                  className="pl-8 pr-3 py-2 sm:py-1.5 text-xs border rounded-md w-full sm:w-48 bg-white"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="py-8 text-center text-sm text-gray-500">Loading coupons...</div>
            ) : filteredCoupons.length === 0 ? (
              <div className="py-8 text-center text-sm text-gray-500">No coupons registered yet.</div>
            ) : (
              <>
                {/* Mobile Card Feed (block md:hidden) */}
                <div className="block md:hidden space-y-3">
                  {filteredCoupons.map((coupon) => (
                    <div
                      key={`mobile-coupon-${coupon.id}`}
                      className="bg-white rounded-xl border border-gray-200 p-3.5 shadow-xs space-y-3"
                    >
                      {/* Top Header: Coupon Code + Discount badge */}
                      <div className="flex items-center justify-between gap-2">
                        <div className="font-mono font-bold text-sm bg-gray-100 text-gray-900 px-3 py-1.5 rounded-lg border border-gray-200 tracking-wider">
                          {coupon.code}
                        </div>
                        <span className="text-xs font-bold text-green-700 bg-green-50 border border-green-200 px-2.5 py-1 rounded-full">
                          {coupon.discount_type === "percentage"
                            ? `${coupon.discount_value}% OFF`
                            : `₹${coupon.discount_value} FLAT`}
                        </span>
                      </div>

                      {/* Constraints Grid */}
                      <div className="grid grid-cols-2 gap-2 text-xs bg-gray-50/70 p-2.5 rounded-lg border border-gray-100">
                        <div>
                          <span className="text-gray-500 text-[11px] block">Min Spend</span>
                          <span className="font-semibold text-gray-800">₹{coupon.min_order_amount || 0}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 text-[11px] block">Max Discount Cap</span>
                          <span className="font-semibold text-gray-800">
                            {coupon.max_discount_amount ? `₹${coupon.max_discount_amount}` : "No Cap"}
                          </span>
                        </div>
                        <div className="col-span-2 pt-1 border-t border-gray-200/60 flex items-center justify-between">
                          <span className="text-gray-500 text-[11px]">Expires</span>
                          <span className="font-medium text-gray-700 text-[11px]">
                            {coupon.expiration_date
                              ? new Date(coupon.expiration_date).toLocaleDateString()
                              : "Never expires"}
                          </span>
                        </div>
                      </div>

                      {/* Actions: Toggle Active + Edit + Delete */}
                      <div className="flex items-center justify-between gap-2 pt-1 border-t border-gray-100">
                        <button
                          type="button"
                          onClick={() => toggleCouponActive(coupon.id, coupon.active)}
                          className={`min-h-[38px] px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-all ${
                            coupon.active
                              ? "bg-green-100 text-green-800 border border-green-200"
                              : "bg-red-100 text-red-800 border border-red-200"
                          }`}
                        >
                          <span className={`w-2 h-2 rounded-full ${coupon.active ? "bg-green-600" : "bg-red-600"}`} />
                          <span>{coupon.active ? "Active" : "Inactive"}</span>
                        </button>

                        <div className="flex items-center gap-1">
                          <Button
                            variant="outline"
                            size="sm"
                            className="min-h-[38px] px-3 text-xs flex items-center gap-1 text-gray-700"
                            onClick={() => handleEdit(coupon)}
                          >
                            <Edit3 className="h-3.5 w-3.5" />
                            <span>Edit</span>
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="min-h-[38px] px-3 text-xs flex items-center gap-1 text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200"
                            onClick={() => handleDelete(coupon)}
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                            <span>Delete</span>
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Desktop Table View (hidden md:block) */}
                <div className="hidden md:block overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Coupon Code</TableHead>
                        <TableHead>Discount</TableHead>
                        <TableHead>Min Spend</TableHead>
                        <TableHead>Max Cap</TableHead>
                        <TableHead>Expires On</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredCoupons.map((coupon) => (
                        <TableRow key={coupon.id}>
                          <TableCell className="font-mono font-bold text-xs text-gray-900">
                            {coupon.code}
                          </TableCell>
                          <TableCell className="text-xs font-semibold text-green-700">
                            {coupon.discount_type === "percentage"
                              ? `${coupon.discount_value}% OFF`
                              : `₹${coupon.discount_value} FLAT`}
                          </TableCell>
                          <TableCell className="text-xs text-gray-600">
                            ₹{coupon.min_order_amount || 0}
                          </TableCell>
                          <TableCell className="text-xs text-gray-500">
                            {coupon.max_discount_amount ? `₹${coupon.max_discount_amount}` : "None"}
                          </TableCell>
                          <TableCell className="text-xs text-gray-500">
                            {coupon.expiration_date
                              ? new Date(coupon.expiration_date).toLocaleDateString()
                              : "Never"}
                          </TableCell>
                          <TableCell>
                            <button
                              type="button"
                              onClick={() => toggleCouponActive(coupon.id, coupon.active)}
                              className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold transition-all ${
                                coupon.active
                                  ? "bg-green-100 text-green-800 hover:bg-green-200"
                                  : "bg-red-100 text-red-800 hover:bg-red-200"
                              }`}
                            >
                              {coupon.active ? "Active" : "Inactive"}
                            </button>
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1">
                              <Button
                                variant="ghost"
                                size="sm"
                                className="h-7 w-7 p-0"
                                onClick={() => handleEdit(coupon)}
                                title="Edit Coupon"
                              >
                                <Edit3 className="h-3.5 w-3.5 text-gray-600" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="h-7 w-7 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                                onClick={() => handleDelete(coupon)}
                                title="Delete Coupon"
                              >
                                <Trash2 className="h-3.5 w-3.5" />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
