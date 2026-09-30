"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Plus, RefreshCw, Utensils, CheckCircle2, Search } from "lucide-react";

export default function PunjabiMenuPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  // Add Dish state
  const [isAdding, setIsAdding] = useState(false);
  const [newDish, setNewDish] = useState({
    name: "",
    selling_price: "",
    unit: "1 plate",
    daily_special: false,
    combo: false,
  });

  useEffect(() => {
    fetchMenu();
  }, []);

  async function fetchMenu() {
    setLoading(true);
    // Strictly fetch products with experience_type = 'KITCHEN'
    const { data, error } = await supabase
      .from("products")
      .select(`
        *,
        category:categories(name)
      `)
      .eq("experience_type", "KITCHEN")
      .order("name", { ascending: true });

    if (!error && data) {
      setProducts(data);
    }
    setLoading(false);
  }

  async function updateField(id: string, field: string, value: any) {
    setUpdatingId(id);
    const { error } = await supabase
      .from("products")
      .update({ [field]: value })
      .eq("id", id);

    if (!error) {
      setProducts((prev) =>
        prev.map((p) => (p.id === id ? { ...p, [field]: value } : p))
      );
    } else {
      alert("Error updating " + field + ": " + error.message);
    }
    setUpdatingId(null);
  }

  async function handleAddDish(e: React.FormEvent) {
    e.preventDefault();
    const price = parseFloat(newDish.selling_price);
    if (!newDish.name.trim() || isNaN(price)) return;

    const slug = newDish.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "");

    const { error } = await supabase.from("products").insert([
      {
        name: newDish.name.trim(),
        slug: `${slug}-${Date.now().toString().slice(-4)}`,
        experience_type: "KITCHEN",
        product_type: newDish.combo ? "COMBO" : "SINGLE",
        price,
        selling_price: price,
        unit: newDish.unit,
        daily_special: newDish.daily_special,
        combo: newDish.combo,
        active: true,
        stock_quantity: 50,
      },
    ]);

    if (!error) {
      setIsAdding(false);
      setNewDish({ name: "", selling_price: "", unit: "1 plate", daily_special: false, combo: false });
      fetchMenu();
    } else {
      alert("Error adding dish: " + error.message);
    }
  }

  const filteredDishes = products.filter((p) =>
    p.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <Utensils className="h-7 w-7 text-orange-600" />
            Today&apos;s Punjabi Kitchen Menu
          </h2>
          <p className="text-sm text-gray-500">
            Control authentic Punjabi meals, daily specials, combos, and live kitchen pricing
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchMenu} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </Button>
          {!isAdding && (
            <Button size="sm" onClick={() => setIsAdding(true)} className="bg-orange-600 hover:bg-orange-700 text-white">
              <Plus className="h-4 w-4 mr-1.5" />
              Add Kitchen Dish
            </Button>
          )}
        </div>
      </div>

      {isAdding && (
        <Card className="shadow-sm border-orange-200 bg-orange-50/20 max-w-xl">
          <CardHeader>
            <CardTitle className="text-base font-semibold text-orange-950">Add Punjabi Kitchen Dish</CardTitle>
            <CardDescription className="text-xs">
              Instantly create a meal or combo for the GOVIND KITCHEN experience.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleAddDish} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div className="col-span-2">
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Dish Name *</label>
                  <input
                    required
                    type="text"
                    placeholder="e.g. Special Amritsari Kulcha Thali"
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    value={newDish.name}
                    onChange={(e) => setNewDish({ ...newDish, name: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Selling Price (₹) *</label>
                  <input
                    required
                    type="number"
                    step="0.01"
                    placeholder="e.g. 149"
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    value={newDish.selling_price}
                    onChange={(e) => setNewDish({ ...newDish, selling_price: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Portion / Unit</label>
                  <input
                    required
                    type="text"
                    placeholder="e.g. 1 plate / 1 thali"
                    className="w-full border px-3 py-2 rounded text-sm bg-white"
                    value={newDish.unit}
                    onChange={(e) => setNewDish({ ...newDish, unit: e.target.value })}
                  />
                </div>
              </div>

              <div className="flex gap-4 p-3 bg-white rounded border">
                <label className="flex items-center gap-2 text-xs font-medium cursor-pointer">
                  <input
                    type="checkbox"
                    checked={newDish.daily_special}
                    onChange={(e) => setNewDish({ ...newDish, daily_special: e.target.checked })}
                    className="rounded text-orange-600"
                  />
                  <span>Mark as Today&apos;s Special</span>
                </label>
                <label className="flex items-center gap-2 text-xs font-medium cursor-pointer">
                  <input
                    type="checkbox"
                    checked={newDish.combo}
                    onChange={(e) => setNewDish({ ...newDish, combo: e.target.checked })}
                    className="rounded text-orange-600"
                  />
                  <span>Combo Meal</span>
                </label>
              </div>

              <div className="flex gap-2">
                <Button type="submit" className="bg-orange-600 hover:bg-orange-700 text-white">
                  Create Dish
                </Button>
                <Button type="button" variant="outline" onClick={() => setIsAdding(false)}>
                  Cancel
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <CardTitle className="text-base font-semibold">
                Kitchen Items ({filteredDishes.length})
              </CardTitle>
              <CardDescription className="text-xs">
                Filter: <code>experience_type = &apos;KITCHEN&apos;</code>
              </CardDescription>
            </div>
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
              <input
                type="text"
                placeholder="Search dish..."
                className="pl-8 pr-3 py-1.5 text-xs border rounded-md w-48 bg-white"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading Punjabi kitchen menu...</div>
          ) : filteredDishes.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">No Kitchen dishes found.</div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Dish Name</TableHead>
                    <TableHead>Unit</TableHead>
                    <TableHead>Price (₹)</TableHead>
                    <TableHead>Daily Special?</TableHead>
                    <TableHead>Combo?</TableHead>
                    <TableHead>Active?</TableHead>
                    <TableHead className="text-right">Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredDishes.map((product) => (
                    <TableRow key={product.id}>
                      <TableCell className="font-semibold text-xs text-gray-900">
                        {product.name}
                      </TableCell>
                      <TableCell className="text-xs text-gray-500">{product.unit}</TableCell>
                      <TableCell>
                        <input
                          type="number"
                          step="0.01"
                          defaultValue={product.selling_price || product.price}
                          key={`${product.id}-${product.selling_price}`}
                          className="w-20 border px-2 py-1 rounded text-xs font-bold text-orange-700 bg-white"
                          onBlur={(e) => {
                            const val = parseFloat(e.target.value);
                            if (!isNaN(val) && val !== product.selling_price) {
                              updateField(product.id, "selling_price", val);
                            }
                          }}
                        />
                      </TableCell>
                      <TableCell>
                        <input
                          type="checkbox"
                          checked={product.daily_special || false}
                          onChange={(e) => updateField(product.id, "daily_special", e.target.checked)}
                          disabled={updatingId === product.id}
                          className="w-4 h-4 cursor-pointer text-orange-600 rounded"
                        />
                      </TableCell>
                      <TableCell>
                        <input
                          type="checkbox"
                          checked={product.combo || false}
                          onChange={(e) => updateField(product.id, "combo", e.target.checked)}
                          disabled={updatingId === product.id}
                          className="w-4 h-4 cursor-pointer text-orange-600 rounded"
                        />
                      </TableCell>
                      <TableCell>
                        <input
                          type="checkbox"
                          checked={product.active ?? true}
                          onChange={(e) => updateField(product.id, "active", e.target.checked)}
                          disabled={updatingId === product.id}
                          className="w-4 h-4 cursor-pointer text-green-600 rounded"
                        />
                      </TableCell>
                      <TableCell className="text-right text-xs text-gray-400">
                        {updatingId === product.id ? "Saving..." : "Auto-saved"}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
