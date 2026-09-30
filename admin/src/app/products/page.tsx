"use client";

import { useEffect, useState, useRef } from "react";
import Image from "next/image";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Plus, Search, Upload, Trash2, Edit3, CheckCircle2, XCircle, AlertCircle } from "lucide-react";

export default function ProductsPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedExperience, setSelectedExperience] = useState<string>("ALL");

  // Form State
  const [isEditing, setIsEditing] = useState(false);
  const [currentProduct, setCurrentProduct] = useState<any>(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const initialFormState = {
    name: "",
    category_id: "",
    experience_type: "FRESH",
    product_type: "SINGLE",
    price: "",
    selling_price: "",
    unit: "1 kg",
    stock_quantity: "50",
    low_stock_threshold: "10",
    description: "",
    image_url: "",
    active: true,
    on_fresh_board: false,
    bulk_available: false,
    bundle_items: "",
  };

  const [formData, setFormData] = useState(initialFormState);

  useEffect(() => {
    fetchInitialData();
  }, []);

  async function fetchInitialData() {
    setLoading(true);
    const [{ data: cats }, { data: prods, error }] = await Promise.all([
      supabase.from("categories").select("id, name").order("name"),
      supabase
        .from("products")
        .select(`
          *,
          category:categories(name)
        `)
        .order("name", { ascending: true }),
    ]);

    if (cats) setCategories(cats);
    if (!error && prods) setProducts(prods);
    setLoading(false);
  }

  const handleAdd = () => {
    setCurrentProduct(null);
    setFormData(initialFormState);
    setFormError(null);
    setIsEditing(true);
  };

  const handleEdit = (product: any) => {
    setCurrentProduct(product);
    setFormData({
      name: product.name || "",
      category_id: product.category_id || "",
      experience_type: product.experience_type || "FRESH",
      product_type: product.product_type || "SINGLE",
      price: product.price?.toString() || "",
      selling_price: product.selling_price?.toString() || "",
      unit: product.unit || "1 item",
      stock_quantity: product.stock_quantity?.toString() || "0",
      low_stock_threshold: product.low_stock_threshold?.toString() || "10",
      description: product.description || "",
      image_url: product.image_url || "",
      active: product.active ?? true,
      on_fresh_board: product.on_fresh_board || false,
      bulk_available: product.bulk_available || false,
      bundle_items: product.bundle_items ? JSON.stringify(product.bundle_items, null, 2) : "",
    });
    setFormError(null);
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    setCurrentProduct(null);
    setFormError(null);
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadingImage(true);
    try {
      const fileExt = file.name.split(".").pop();
      const fileName = `${Date.now()}-${Math.random().toString(36).substring(2, 8)}.${fileExt}`;
      const filePath = `products/${fileName}`;

      const { error: uploadError } = await supabase.storage
        .from("products")
        .upload(filePath, file, { cacheControl: "3600", upsert: true });

      if (uploadError) {
        throw uploadError;
      }

      const { data: publicUrlData } = supabase.storage
        .from("products")
        .getPublicUrl(filePath);

      setFormData((prev) => ({ ...prev, image_url: publicUrlData.publicUrl }));
    } catch (err: any) {
      alert("Image upload failed: " + err.message);
    } finally {
      setUploadingImage(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    let parsedBundleItems = null;
    if (formData.bundle_items && formData.bundle_items.trim() !== "") {
      try {
        parsedBundleItems = JSON.parse(formData.bundle_items);
      } catch (err) {
        setFormError("Invalid JSON in Bundle / Wholesale settings field");
        return;
      }
    }

    const priceNum = parseFloat(formData.price);
    const sellingPriceNum = parseFloat(formData.selling_price) || priceNum;
    const stockNum = parseInt(formData.stock_quantity, 10) || 0;
    const lowStockNum = parseInt(formData.low_stock_threshold, 10) || 10;

    if (isNaN(priceNum) || priceNum <= 0) {
      setFormError("Valid MRP price is required.");
      return;
    }

    const payload = {
      name: formData.name.trim(),
      category_id: formData.category_id || null,
      experience_type: formData.experience_type,
      product_type: formData.product_type,
      price: priceNum,
      selling_price: sellingPriceNum,
      unit: formData.unit.trim(),
      stock_quantity: stockNum,
      low_stock_threshold: lowStockNum,
      description: formData.description.trim(),
      image_url: formData.image_url.trim() || null,
      active: formData.active,
      on_fresh_board: formData.on_fresh_board,
      bulk_available: formData.bulk_available,
      bundle_items: parsedBundleItems,
    };

    if (currentProduct) {
      const { error } = await supabase
        .from("products")
        .update(payload)
        .eq("id", currentProduct.id);

      if (error) {
        setFormError(error.message);
        return;
      }
    } else {
      const slug = formData.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "");
      const { error } = await supabase
        .from("products")
        .insert([{ ...payload, slug: `${slug}-${Date.now().toString().slice(-4)}` }]);

      if (error) {
        setFormError(error.message);
        return;
      }
    }

    setIsEditing(false);
    fetchInitialData();
  };

  const handleSafeDelete = async (product: any) => {
    // 1. Check if product has order items
    const { count } = await supabase
      .from("order_items")
      .select("*", { count: "exact", head: true })
      .eq("product_id", product.id);

    if (count && count > 0) {
      const confirmDeactivate = confirm(
        `"${product.name}" has ${count} customer order record(s). To preserve order audit history, it cannot be permanently deleted.\n\nWould you like to DEACTIVATE it instead?`
      );
      if (confirmDeactivate) {
        await supabase.from("products").update({ active: false }).eq("id", product.id);
        fetchInitialData();
      }
      return;
    }

    const confirmDelete = confirm(`Are you sure you want to permanently delete "${product.name}"?`);
    if (confirmDelete) {
      const { error } = await supabase.from("products").delete().eq("id", product.id);
      if (error) {
        alert("Error deleting product: " + error.message);
      } else {
        fetchInitialData();
      }
    }
  };

  const toggleProductActive = async (id: string, currentActive: boolean) => {
    await supabase.from("products").update({ active: !currentActive }).eq("id", id);
    setProducts((prev) =>
      prev.map((p) => (p.id === id ? { ...p, active: !currentActive } : p))
    );
  };

  // Filter products
  const filteredProducts = products.filter((p) => {
    const matchesSearch =
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.category?.name?.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesExperience =
      selectedExperience === "ALL" || p.experience_type === selectedExperience;
    return matchesSearch && matchesExperience;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900">Products Catalogue</h2>
          <p className="text-sm text-gray-500">Manage Fresh produce, Kitchen menu, and Wholesale goods</p>
        </div>
        {!isEditing && (
          <Button onClick={handleAdd} className="bg-green-700 hover:bg-green-800 text-white">
            <Plus className="h-4 w-4 mr-1.5" />
            Add New Product
          </Button>
        )}
      </div>

      {isEditing ? (
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle>{currentProduct ? "Edit Product" : "Add New Product"}</CardTitle>
            <CardDescription>
              Configure pricing, stock, experience tier, and catalogue visibility.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {formError && (
              <div className="mb-4 p-3 rounded bg-red-50 border border-red-200 text-sm text-red-700">
                {formError}
              </div>
            )}
            <form onSubmit={handleSubmit} className="space-y-4 max-w-2xl">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Product Name *</label>
                  <input
                    required
                    type="text"
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    placeholder="e.g. Fresh Shimla Apple"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Category</label>
                  <select
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    value={formData.category_id}
                    onChange={(e) => setFormData({ ...formData, category_id: e.target.value })}
                  >
                    <option value="">Select Category</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Experience Type *</label>
                  <select
                    className="w-full border px-3 py-2 rounded text-sm bg-white font-medium"
                    value={formData.experience_type}
                    onChange={(e) => setFormData({ ...formData, experience_type: e.target.value })}
                  >
                    <option value="FRESH">Fresh Vegetables & Fruits</option>
                    <option value="KITCHEN">Kitchen & Punjabi Meals</option>
                    <option value="WHOLESALE">Wholesale Bulk Produce</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Product Type</label>
                  <select
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    value={formData.product_type}
                    onChange={(e) => setFormData({ ...formData, product_type: e.target.value })}
                  >
                    <option value="SINGLE">Single Item</option>
                    <option value="PACK">Value Pack</option>
                    <option value="COMBO">Combo Deal</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">MRP Price (₹) *</label>
                  <input
                    required
                    type="number"
                    step="0.01"
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    placeholder="e.g. 100"
                    value={formData.price}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Selling Price (₹) *</label>
                  <input
                    type="number"
                    step="0.01"
                    className="w-full border px-3 py-2 rounded text-sm bg-white font-semibold text-green-700"
                    placeholder="e.g. 79 (Discounted)"
                    value={formData.selling_price}
                    onChange={(e) => setFormData({ ...formData, selling_price: e.target.value })}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Unit Specification *</label>
                  <input
                    required
                    type="text"
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    placeholder="e.g. 1 kg, 500 g, 1 plate"
                    value={formData.unit}
                    onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Stock Quantity *</label>
                  <input
                    required
                    type="number"
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    placeholder="e.g. 50"
                    value={formData.stock_quantity}
                    onChange={(e) => setFormData({ ...formData, stock_quantity: e.target.value })}
                  />
                </div>
              </div>

              {/* Image Upload / URL */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Product Image</label>
                <div className="flex gap-2 items-center">
                  <input
                    type="text"
                    className="flex-1 border px-3 py-2 rounded text-sm bg-white"
                    placeholder="Image URL or upload below..."
                    value={formData.image_url}
                    onChange={(e) => setFormData({ ...formData, image_url: e.target.value })}
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
                    {uploadingImage ? "Uploading..." : "Upload File"}
                  </Button>
                </div>
                {formData.image_url && (
                  <div className="mt-2 flex items-center gap-2">
                    <span className="text-xs text-gray-500">Preview:</span>
                    <img
                      src={formData.image_url}
                      alt="Preview"
                      className="h-12 w-12 object-cover rounded border"
                      onError={(e) => {
                        (e.target as HTMLElement).style.display = "none";
                      }}
                    />
                  </div>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Description</label>
                <textarea
                  className="w-full border px-3 py-2 rounded text-sm bg-white h-20"
                  placeholder="Fresh and organic produce sourced daily..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              {/* Bundle / Wholesale Settings */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Wholesale Tiers & Settings (JSON)
                </label>
                <textarea
                  className="w-full border px-3 py-2 rounded h-20 font-mono text-xs bg-white"
                  placeholder='{"wholesale_pricing": {"wholesale_eligible": true, "tiers": [...]}}'
                  value={formData.bundle_items}
                  onChange={(e) => setFormData({ ...formData, bundle_items: e.target.value })}
                />
              </div>

              {/* Toggles */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3 bg-gray-50 rounded-lg border">
                <label className="flex items-center gap-2 text-xs font-medium cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.active}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    className="rounded text-green-700"
                  />
                  <span>Active in Store</span>
                </label>

                <label className="flex items-center gap-2 text-xs font-medium cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.on_fresh_board}
                    onChange={(e) => setFormData({ ...formData, on_fresh_board: e.target.checked })}
                    className="rounded text-green-700"
                  />
                  <span>Highlight on Fresh Board</span>
                </label>

                <label className="flex items-center gap-2 text-xs font-medium cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.bulk_available}
                    onChange={(e) => setFormData({ ...formData, bulk_available: e.target.checked })}
                    className="rounded text-green-700"
                  />
                  <span>Enable Wholesale Bulk</span>
                </label>
              </div>

              <div className="flex gap-2 pt-2">
                <Button type="submit" className="bg-green-700 hover:bg-green-800 text-white">
                  Save Product
                </Button>
                <Button type="button" variant="outline" onClick={handleCancel}>
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
                  All Products ({filteredProducts.length})
                </CardTitle>
                <CardDescription className="text-xs">
                  Showing products filtered by experience and search query
                </CardDescription>
              </div>

              <div className="flex flex-wrap items-center gap-2">
                <div className="relative">
                  <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
                  <input
                    type="text"
                    placeholder="Search catalogue..."
                    className="pl-8 pr-3 py-1.5 text-xs border rounded-md w-48 bg-white"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>

                <select
                  className="text-xs border rounded-md px-2.5 py-1.5 bg-white font-medium"
                  value={selectedExperience}
                  onChange={(e) => setSelectedExperience(e.target.value)}
                >
                  <option value="ALL">All Experiences</option>
                  <option value="FRESH">Fresh</option>
                  <option value="KITCHEN">Kitchen</option>
                  <option value="WHOLESALE">Wholesale</option>
                </select>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="py-8 text-center text-sm text-gray-500">Loading products catalogue...</div>
            ) : filteredProducts.length === 0 ? (
              <div className="py-8 text-center text-sm text-gray-500">No matching products found.</div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Product</TableHead>
                      <TableHead>Experience</TableHead>
                      <TableHead>Category</TableHead>
                      <TableHead>MRP / Selling</TableHead>
                      <TableHead>Unit</TableHead>
                      <TableHead>Stock</TableHead>
                      <TableHead>Fresh Board</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredProducts.map((product) => (
                      <TableRow key={product.id}>
                        <TableCell>
                          <div className="flex items-center gap-2.5">
                            {product.image_url ? (
                              <img
                                src={product.image_url}
                                alt={product.name}
                                className="h-9 w-9 object-cover rounded border"
                              />
                            ) : (
                              <div className="h-9 w-9 rounded bg-gray-100 flex items-center justify-center text-xs text-gray-400 font-bold">
                                {product.name.slice(0, 1)}
                              </div>
                            )}
                            <div>
                              <div className="font-semibold text-xs text-gray-900">{product.name}</div>
                              <div className="text-[10px] text-gray-400 font-mono">
                                {product.product_type || "SINGLE"}
                              </div>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                              product.experience_type === "KITCHEN"
                                ? "bg-orange-100 text-orange-800"
                                : product.experience_type === "WHOLESALE"
                                ? "bg-blue-100 text-blue-800"
                                : "bg-emerald-100 text-emerald-800"
                            }`}
                          >
                            {product.experience_type}
                          </span>
                        </TableCell>
                        <TableCell className="text-xs text-gray-600">
                          {product.category?.name || "General"}
                        </TableCell>
                        <TableCell>
                          <div className="text-xs font-bold text-gray-900">
                            ₹{product.selling_price || product.price}
                          </div>
                          {product.selling_price && product.selling_price !== product.price && (
                            <div className="text-[10px] text-gray-400 line-through">
                              ₹{product.price}
                            </div>
                          )}
                        </TableCell>
                        <TableCell className="text-xs text-gray-500">{product.unit}</TableCell>
                        <TableCell>
                          <span
                            className={`text-xs font-semibold ${
                              product.stock_quantity <= (product.low_stock_threshold || 10)
                                ? "text-amber-600 font-bold"
                                : "text-gray-700"
                            }`}
                          >
                            {product.stock_quantity}
                          </span>
                        </TableCell>
                        <TableCell>
                          {product.on_fresh_board ? (
                            <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-green-700">
                              <CheckCircle2 className="h-3.5 w-3.5" /> Yes
                            </span>
                          ) : (
                            <span className="text-xs text-gray-400">No</span>
                          )}
                        </TableCell>
                        <TableCell>
                          <button
                            type="button"
                            onClick={() => toggleProductActive(product.id, product.active)}
                            className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold transition-all ${
                              product.active
                                ? "bg-green-100 text-green-800 hover:bg-green-200"
                                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                            }`}
                          >
                            {product.active ? "Active" : "Inactive"}
                          </button>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end gap-1">
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-7 w-7 p-0"
                              onClick={() => handleEdit(product)}
                              title="Edit Product"
                            >
                              <Edit3 className="h-3.5 w-3.5 text-gray-600" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-7 w-7 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                              onClick={() => handleSafeDelete(product)}
                              title="Delete or Archive Product"
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
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
