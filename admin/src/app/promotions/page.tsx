"use client";

import { useEffect, useState, useRef } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Megaphone, Plus, Edit3, Trash2, RefreshCw, Upload, Search } from "lucide-react";

export default function PromotionsPage() {
  const supabase = createClient();
  const [promotions, setPromotions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  // Form state
  const [isEditing, setIsEditing] = useState(false);
  const [currentPromo, setCurrentPromo] = useState<any | null>(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const initialFormState = {
    title: "",
    subtitle: "",
    image: "",
    cta: "Shop Now",
    deep_link: "/products",
    start_date: new Date().toISOString().split("T")[0],
    end_date: new Date(Date.now() + 30 * 86400000).toISOString().split("T")[0],
    priority: "1",
    active: true,
  };

  const [formData, setFormData] = useState(initialFormState);

  useEffect(() => {
    fetchPromotions();
  }, []);

  async function fetchPromotions() {
    setLoading(true);
    const { data, error } = await supabase
      .from("promotions")
      .select("*")
      .order("priority", { ascending: false })
      .order("created_at", { ascending: false });

    if (!error && data) {
      setPromotions(data);
    }
    setLoading(false);
  }

  const handleAdd = () => {
    setCurrentPromo(null);
    setFormData(initialFormState);
    setFormError(null);
    setIsEditing(true);
  };

  const handleEdit = (promo: any) => {
    setCurrentPromo(promo);
    setFormData({
      title: promo.title || "",
      subtitle: promo.subtitle || "",
      image: promo.image || "",
      cta: promo.cta || "Shop Now",
      deep_link: promo.deep_link || "/products",
      start_date: promo.start_date ? promo.start_date.split("T")[0] : "",
      end_date: promo.end_date ? promo.end_date.split("T")[0] : "",
      priority: (promo.priority ?? 1).toString(),
      active: promo.active ?? true,
    });
    setFormError(null);
    setIsEditing(true);
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadingImage(true);
    try {
      const fileExt = file.name.split(".").pop();
      const fileName = `promo-${Date.now()}.${fileExt}`;
      const filePath = `promotions/${fileName}`;

      const { error: uploadError } = await supabase.storage
        .from("promotions")
        .upload(filePath, file, { cacheControl: "3600", upsert: true });

      if (uploadError) throw uploadError;

      const { data: publicUrlData } = supabase.storage
        .from("promotions")
        .getPublicUrl(filePath);

      setFormData((prev) => ({ ...prev, image: publicUrlData.publicUrl }));
    } catch (err: any) {
      alert("Promotion banner upload failed: " + err.message);
    } finally {
      setUploadingImage(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.title.trim() || !formData.image.trim()) {
      setFormError("Banner title and image URL are required.");
      return;
    }

    const payload = {
      title: formData.title.trim(),
      subtitle: formData.subtitle.trim() || null,
      image: formData.image.trim(),
      cta: formData.cta.trim() || "Shop Now",
      deep_link: formData.deep_link.trim() || null,
      start_date: new Date(formData.start_date).toISOString(),
      end_date: new Date(formData.end_date).toISOString(),
      priority: parseInt(formData.priority, 10) || 1,
      active: formData.active,
      updated_at: new Date().toISOString(),
    };

    if (currentPromo) {
      const { error } = await supabase
        .from("promotions")
        .update(payload)
        .eq("id", currentPromo.id);

      if (error) {
        setFormError(error.message);
        return;
      }
    } else {
      const { error } = await supabase.from("promotions").insert([payload]);
      if (error) {
        setFormError(error.message);
        return;
      }
    }

    setIsEditing(false);
    fetchPromotions();
  };

  const togglePromoActive = async (id: string, currentActive: boolean) => {
    await supabase.from("promotions").update({ active: !currentActive }).eq("id", id);
    setPromotions((prev) =>
      prev.map((p) => (p.id === id ? { ...p, active: !currentActive } : p))
    );
  };

  const handleDelete = async (promo: any) => {
    if (confirm(`Are you sure you want to delete promotion banner "${promo.title}"?`)) {
      const { error } = await supabase.from("promotions").delete().eq("id", promo.id);
      if (error) {
        alert("Error deleting promotion: " + error.message);
      } else {
        fetchPromotions();
      }
    }
  };

  const filteredPromos = promotions.filter((p) =>
    p.title.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <Megaphone className="h-7 w-7 text-indigo-600" />
            Promotions & Hero Banners
          </h2>
          <p className="text-sm text-gray-500">
            Publish seasonal campaigns, discount banners, and home carousel slides
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchPromotions} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </Button>
          {!isEditing && (
            <Button size="sm" onClick={handleAdd} className="bg-indigo-600 hover:bg-indigo-700 text-white">
              <Plus className="h-4 w-4 mr-1.5" />
              Create Banner
            </Button>
          )}
        </div>
      </div>

      {isEditing ? (
        <Card className="shadow-sm max-w-xl">
          <CardHeader>
            <CardTitle>{currentPromo ? "Edit Banner Campaign" : "New Banner Campaign"}</CardTitle>
            <CardDescription>Configure promotional titles, visual banner, priority and expiry dates.</CardDescription>
          </CardHeader>
          <CardContent>
            {formError && (
              <div className="mb-4 p-3 rounded bg-red-50 border border-red-200 text-xs text-red-700">
                {formError}
              </div>
            )}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Banner Title *</label>
                <input
                  required
                  type="text"
                  placeholder="e.g. Monsoon Organic Harvest Sale"
                  className="w-full border px-3 py-2 rounded text-sm bg-white font-medium"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Subtitle / Tagline</label>
                <input
                  type="text"
                  placeholder="e.g. Up to 40% OFF Farm-Fresh Greens"
                  className="w-full border px-3 py-2 rounded text-sm bg-white"
                  value={formData.subtitle}
                  onChange={(e) => setFormData({ ...formData, subtitle: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Banner Image *</label>
                <div className="flex gap-2 items-center">
                  <input
                    required
                    type="text"
                    placeholder="Image URL..."
                    className="flex-1 border px-3 py-2 rounded text-sm bg-white"
                    value={formData.image}
                    onChange={(e) => setFormData({ ...formData, image: e.target.value })}
                  />
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleImageUpload}
                  />
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploadingImage}
                    className="flex items-center gap-1.5"
                  >
                    <Upload className="h-4 w-4" />
                    {uploadingImage ? "Uploading..." : "Upload"}
                  </Button>
                </div>
                {formData.image && (
                  <div className="mt-2">
                    <img
                      src={formData.image}
                      alt="Banner Preview"
                      className="w-full h-24 object-cover rounded border"
                      onError={(e) => { (e.target as HTMLElement).style.display = "none"; }}
                    />
                  </div>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Button CTA Text</label>
                  <input
                    type="text"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-600 outline-none"
                    value={formData.cta}
                    onChange={(e) => setFormData({ ...formData, cta: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Display Priority</label>
                  <input
                    type="number"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-600 outline-none"
                    value={formData.priority}
                    onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Start Date</label>
                  <input
                    required
                    type="date"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-600 outline-none"
                    value={formData.start_date}
                    onChange={(e) => setFormData({ ...formData, start_date: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">End Date</label>
                  <input
                    required
                    type="date"
                    className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-600 outline-none"
                    value={formData.end_date}
                    onChange={(e) => setFormData({ ...formData, end_date: e.target.value })}
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <input
                  type="checkbox"
                  id="promo_active"
                  checked={formData.active}
                  onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                  className="h-4 w-4 rounded text-indigo-600 cursor-pointer"
                />
                <label htmlFor="promo_active" className="text-xs font-semibold text-gray-700 cursor-pointer">
                  Active (show in customer hero carousel)
                </label>
              </div>

              <div className="flex gap-2 pt-2">
                <Button type="submit" className="min-h-[42px] flex-1 sm:flex-initial bg-indigo-600 hover:bg-indigo-700 text-white font-semibold">
                  Save Banner Campaign
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
                  Promotions Feed ({filteredPromos.length})
                </CardTitle>
                <CardDescription className="text-xs">
                  Active banners stream directly to the Android app and web carousel
                </CardDescription>
              </div>
              <div className="relative w-full sm:w-auto">
                <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search campaigns..."
                  className="pl-8 pr-3 py-2 sm:py-1.5 text-xs border rounded-md w-full sm:w-52 bg-white"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="py-8 text-center text-sm text-gray-500">Loading promotions...</div>
            ) : filteredPromos.length === 0 ? (
              <div className="py-8 text-center text-sm text-gray-500">No promotion campaigns registered.</div>
            ) : (
              <>
                {/* Mobile Card Feed (block md:hidden) */}
                <div className="block md:hidden space-y-3">
                  {filteredPromos.map((promo) => (
                    <div
                      key={`mobile-promo-${promo.id}`}
                      className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-xs space-y-3"
                    >
                      {/* Banner Image Preview */}
                      <div className="relative w-full h-36 bg-gray-100 border-b border-gray-100">
                        <img
                          src={promo.image}
                          alt={promo.title}
                          className="w-full h-full object-cover"
                          onError={(e) => { (e.target as HTMLElement).style.display = "none"; }}
                        />
                        <div className="absolute top-2 right-2">
                          <span className="text-[10px] font-bold bg-black/60 text-white backdrop-blur-xs px-2 py-0.5 rounded-full">
                            Priority #{promo.priority || 0}
                          </span>
                        </div>
                      </div>

                      {/* Content Body */}
                      <div className="p-3.5 pt-0 space-y-3">
                        <div>
                          <h3 className="font-semibold text-sm text-gray-900 leading-snug">
                            {promo.title}
                          </h3>
                          {promo.subtitle && (
                            <p className="text-xs text-gray-500 mt-0.5">{promo.subtitle}</p>
                          )}
                        </div>

                        {/* Dates Info */}
                        <div className="text-[11px] text-gray-500 bg-gray-50 p-2 rounded-lg border border-gray-100 flex items-center justify-between">
                          <span>Active Period:</span>
                          <span className="font-medium text-gray-700">
                            {new Date(promo.start_date).toLocaleDateString()} – {new Date(promo.end_date).toLocaleDateString()}
                          </span>
                        </div>

                        {/* Actions Footer */}
                        <div className="flex items-center justify-between gap-2 pt-1 border-t border-gray-100">
                          <button
                            type="button"
                            onClick={() => togglePromoActive(promo.id, promo.active)}
                            className={`min-h-[38px] px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-all ${
                              promo.active
                                ? "bg-green-100 text-green-800 border border-green-200"
                                : "bg-red-100 text-red-800 border border-red-200"
                            }`}
                          >
                            <span className={`w-2 h-2 rounded-full ${promo.active ? "bg-green-600" : "bg-red-600"}`} />
                            <span>{promo.active ? "Active" : "Inactive"}</span>
                          </button>

                          <div className="flex items-center gap-1">
                            <Button
                              variant="outline"
                              size="sm"
                              className="min-h-[38px] px-3 text-xs flex items-center gap-1 text-gray-700"
                              onClick={() => handleEdit(promo)}
                            >
                              <Edit3 className="h-3.5 w-3.5" />
                              <span>Edit</span>
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              className="min-h-[38px] px-3 text-xs flex items-center gap-1 text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200"
                              onClick={() => handleDelete(promo)}
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                              <span>Delete</span>
                            </Button>
                          </div>
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
                        <TableHead>Banner Preview</TableHead>
                        <TableHead>Campaign Title</TableHead>
                        <TableHead>Priority</TableHead>
                        <TableHead>Active Window</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredPromos.map((promo) => (
                        <TableRow key={promo.id}>
                          <TableCell>
                            <img
                              src={promo.image}
                              alt={promo.title}
                              className="h-10 w-24 object-cover rounded border"
                              onError={(e) => { (e.target as HTMLElement).style.display = "none"; }}
                            />
                          </TableCell>
                          <TableCell>
                            <div className="font-semibold text-xs text-gray-900">{promo.title}</div>
                            {promo.subtitle && <div className="text-[11px] text-gray-500">{promo.subtitle}</div>}
                          </TableCell>
                          <TableCell className="text-xs font-semibold text-gray-700">
                            #{promo.priority || 0}
                          </TableCell>
                          <TableCell className="text-xs text-gray-500 whitespace-nowrap">
                            {new Date(promo.start_date).toLocaleDateString()} – {new Date(promo.end_date).toLocaleDateString()}
                          </TableCell>
                          <TableCell>
                            <button
                              type="button"
                              onClick={() => togglePromoActive(promo.id, promo.active)}
                              className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold transition-all ${
                                promo.active
                                  ? "bg-green-100 text-green-800 hover:bg-green-200"
                                  : "bg-red-100 text-red-800 hover:bg-red-200"
                              }`}
                            >
                              {promo.active ? "Active" : "Inactive"}
                            </button>
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1">
                              <Button
                                variant="ghost"
                                size="sm"
                                className="h-7 w-7 p-0"
                                onClick={() => handleEdit(promo)}
                                title="Edit Banner"
                              >
                                <Edit3 className="h-3.5 w-3.5 text-gray-600" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="h-7 w-7 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                                onClick={() => handleDelete(promo)}
                                title="Delete Banner"
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
