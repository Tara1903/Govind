"use client";

import { useEffect, useState, useRef } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Plus, Edit3, Trash2, Upload, Search, CheckCircle2, FolderTree } from "lucide-react";

export default function CategoriesPage() {
  const supabase = createClient();
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  // Modal / Form state
  const [isEditing, setIsEditing] = useState(false);
  const [currentCategory, setCurrentCategory] = useState<any>(null);
  const [formData, setFormData] = useState({
    name: "",
    slug: "",
    display_order: "0",
    image_url: "",
    active: true,
  });
  const [uploadingImage, setUploadingImage] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  async function fetchCategories() {
    setLoading(true);
    // 1. Fetch categories
    const { data: cats, error } = await supabase
      .from("categories")
      .select("*")
      .order("display_order", { ascending: true })
      .order("name", { ascending: true });

    // 2. Fetch product counts per category
    const { data: prods } = await supabase
      .from("products")
      .select("id, category_id");

    const counts: Record<string, number> = {};
    if (prods) {
      prods.forEach((p) => {
        if (p.category_id) {
          counts[p.category_id] = (counts[p.category_id] || 0) + 1;
        }
      });
    }

    if (!error && cats) {
      const enriched = cats.map((c) => ({
        ...c,
        productCount: counts[c.id] || 0,
      }));
      setCategories(enriched);
    }
    setLoading(false);
  }

  const handleAdd = () => {
    setCurrentCategory(null);
    setFormData({
      name: "",
      slug: "",
      display_order: (categories.length + 1).toString(),
      image_url: "",
      active: true,
    });
    setFormError(null);
    setIsEditing(true);
  };

  const handleEdit = (category: any) => {
    setCurrentCategory(category);
    setFormData({
      name: category.name || "",
      slug: category.slug || "",
      display_order: (category.display_order ?? 0).toString(),
      image_url: category.image_url || "",
      active: category.active ?? true,
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
      const fileName = `${Date.now()}-${Math.random().toString(36).substring(2, 8)}.${fileExt}`;
      const filePath = `categories/${fileName}`;

      const { error: uploadError } = await supabase.storage
        .from("categories")
        .upload(filePath, file, { cacheControl: "3600", upsert: true });

      if (uploadError) throw uploadError;

      const { data: publicUrlData } = supabase.storage
        .from("categories")
        .getPublicUrl(filePath);

      setFormData((prev) => ({ ...prev, image_url: publicUrlData.publicUrl }));
    } catch (err: any) {
      alert("Category image upload failed: " + err.message);
    } finally {
      setUploadingImage(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    const nameTrim = formData.name.trim();
    if (!nameTrim) {
      setFormError("Category name is required.");
      return;
    }

    const slug = (
      formData.slug.trim() ||
      nameTrim.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "")
    );

    const payload = {
      name: nameTrim,
      slug,
      display_order: parseInt(formData.display_order, 10) || 0,
      image_url: formData.image_url.trim() || null,
      active: formData.active,
    };

    if (currentCategory) {
      const { error } = await supabase
        .from("categories")
        .update(payload)
        .eq("id", currentCategory.id);

      if (error) {
        setFormError(error.message);
        return;
      }
    } else {
      const { error } = await supabase.from("categories").insert([payload]);
      if (error) {
        setFormError(error.message);
        return;
      }
    }

    setIsEditing(false);
    fetchCategories();
  };

  const toggleCategoryActive = async (id: string, currentActive: boolean) => {
    await supabase.from("categories").update({ active: !currentActive }).eq("id", id);
    setCategories((prev) =>
      prev.map((c) => (c.id === id ? { ...c, active: !currentActive } : c))
    );
  };

  const handleDelete = async (category: any) => {
    if (category.productCount > 0) {
      alert(
        `Cannot delete "${category.name}" because it currently has ${category.productCount} associated product(s). Please reassign or delete those products first, or mark this category as Inactive.`
      );
      return;
    }

    if (confirm(`Are you sure you want to delete category "${category.name}"?`)) {
      const { error } = await supabase.from("categories").delete().eq("id", category.id);
      if (error) {
        alert("Error deleting category: " + error.message);
      } else {
        fetchCategories();
      }
    }
  };

  const filteredCategories = categories.filter((c) =>
    c.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    c.slug?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900">Categories</h2>
          <p className="text-sm text-gray-500">Organize products across Fresh, Kitchen, and Wholesale</p>
        </div>
        {!isEditing && (
          <Button onClick={handleAdd} className="bg-green-700 hover:bg-green-800 text-white">
            <Plus className="h-4 w-4 mr-1.5" />
            Add Category
          </Button>
        )}
      </div>

      {isEditing ? (
        <Card className="shadow-sm max-w-xl">
          <CardHeader>
            <CardTitle>{currentCategory ? "Edit Category" : "Add New Category"}</CardTitle>
            <CardDescription>
              Configure category name, slug, display priority, and visual banner.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {formError && (
              <div className="mb-4 p-3 rounded bg-red-50 border border-red-200 text-sm text-red-700">
                {formError}
              </div>
            )}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Category Name *
                </label>
                <input
                  required
                  type="text"
                  placeholder="e.g. Leafy Greens"
                  className="w-full border px-3 py-2 rounded text-sm bg-white"
                  value={formData.name}
                  onChange={(e) => {
                    const name = e.target.value;
                    const autoSlug = name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "");
                    setFormData({ ...formData, name, slug: currentCategory ? formData.slug : autoSlug });
                  }}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Slug</label>
                <input
                  type="text"
                  placeholder="leafy-greens"
                  className="w-full border px-3 py-2 rounded text-sm bg-white font-mono text-xs"
                  value={formData.slug}
                  onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Display Order
                </label>
                <input
                  type="number"
                  placeholder="0"
                  className="w-full border px-3 py-2 rounded text-sm bg-white"
                  value={formData.display_order}
                  onChange={(e) => setFormData({ ...formData, display_order: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Category Image
                </label>
                <div className="flex gap-2 items-center">
                  <input
                    type="text"
                    placeholder="Image URL..."
                    className="flex-1 border px-3 py-2 rounded text-sm bg-white"
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
                    {uploadingImage ? "Uploading..." : "Upload"}
                  </Button>
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <input
                  type="checkbox"
                  id="cat_active"
                  checked={formData.active}
                  onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                  className="rounded text-green-700"
                />
                <label htmlFor="cat_active" className="text-xs font-semibold text-gray-700 cursor-pointer">
                  Active (visible in app)
                </label>
              </div>

              <div className="flex gap-2 pt-4">
                <Button type="submit" className="bg-green-700 hover:bg-green-800 text-white">
                  Save Category
                </Button>
                <Button type="button" variant="outline" onClick={() => setIsEditing(false)}>
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
                  All Categories ({filteredCategories.length})
                </CardTitle>
                <CardDescription className="text-xs">
                  Live categories synced with the Supabase catalogue
                </CardDescription>
              </div>
              <div className="relative">
                <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search categories..."
                  className="pl-8 pr-3 py-1.5 text-xs border rounded-md w-56 bg-white"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="py-8 text-center text-sm text-gray-500">Loading categories...</div>
            ) : filteredCategories.length === 0 ? (
              <div className="py-8 text-center text-sm text-gray-500">No categories found.</div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Category Name</TableHead>
                      <TableHead>Slug</TableHead>
                      <TableHead>Order</TableHead>
                      <TableHead>Products Linked</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredCategories.map((cat) => (
                      <TableRow key={cat.id}>
                        <TableCell>
                          <div className="flex items-center gap-2.5">
                            {cat.image_url ? (
                              <img
                                src={cat.image_url}
                                alt={cat.name}
                                className="h-8 w-8 object-cover rounded border"
                              />
                            ) : (
                              <div className="h-8 w-8 rounded bg-gray-100 flex items-center justify-center text-gray-400">
                                <FolderTree className="h-4 w-4" />
                              </div>
                            )}
                            <span className="font-semibold text-xs text-gray-900">{cat.name}</span>
                          </div>
                        </TableCell>
                        <TableCell className="text-xs font-mono text-gray-500">{cat.slug}</TableCell>
                        <TableCell className="text-xs text-gray-600">{cat.display_order ?? 0}</TableCell>
                        <TableCell>
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-800">
                            {cat.productCount} products
                          </span>
                        </TableCell>
                        <TableCell>
                          <button
                            type="button"
                            onClick={() => toggleCategoryActive(cat.id, cat.active)}
                            className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold transition-all ${
                              cat.active
                                ? "bg-green-100 text-green-800 hover:bg-green-200"
                                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                            }`}
                          >
                            {cat.active ? "Active" : "Inactive"}
                          </button>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end gap-1">
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-7 w-7 p-0"
                              onClick={() => handleEdit(cat)}
                              title="Edit Category"
                            >
                              <Edit3 className="h-3.5 w-3.5 text-gray-600" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-7 w-7 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                              onClick={() => handleDelete(cat)}
                              title="Delete Category"
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
