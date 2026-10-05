"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Search, RefreshCw, Tags, CheckCircle2 } from "lucide-react";

export default function DailyRatesPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    fetchDailyRates();
  }, []);

  async function fetchDailyRates() {
    setLoading(true);
    // Fetch active fresh produce
    const { data, error } = await supabase
      .from("products")
      .select(`
        *,
        category:categories(name)
      `)
      .eq("active", true)
      .eq("experience_type", "FRESH")
      .order("name", { ascending: true });

    if (!error && data) {
      setProducts(data);
    }
    setLoading(false);
  }

  async function updatePrice(id: string, field: "price" | "selling_price", newPrice: number) {
    if (isNaN(newPrice) || newPrice < 0) return;
    setUpdatingId(id);

    // Update single field without overwriting the other
    const { error } = await supabase
      .from("products")
      .update({ [field]: newPrice })
      .eq("id", id);

    if (!error) {
      setProducts((prev) =>
        prev.map((p) => (p.id === id ? { ...p, [field]: newPrice } : p))
      );
    } else {
      alert("Error updating " + field + ": " + error.message);
    }
    setUpdatingId(null);
  }

  async function toggleFreshToday(id: string, currentValue: boolean) {
    setUpdatingId(id);
    const { error } = await supabase
      .from("products")
      .update({ fresh_today: !currentValue })
      .eq("id", id);

    if (!error) {
      setProducts((prev) =>
        prev.map((p) => (p.id === id ? { ...p, fresh_today: !currentValue } : p))
      );
    }
    setUpdatingId(null);
  }

  const filteredProduce = products.filter((p) =>
    p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    p.category?.name?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <Tags className="h-7 w-7 text-emerald-600" />
            Daily Mandi Rate List
          </h2>
          <p className="text-sm text-gray-500">
            Rapid price controller for fresh fruits and vegetables. Changes reflect instantly on customer devices.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchDailyRates} disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <CardTitle className="text-base font-semibold">
                Fresh Produce Rates ({filteredProduce.length})
              </CardTitle>
              <CardDescription className="text-xs">
                Edit Mandi MRP vs Customer Selling Price independently
              </CardDescription>
            </div>
            <div className="relative w-full sm:w-auto">
              <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
              <input
                type="text"
                placeholder="Search produce..."
                className="pl-8 pr-3 py-2 sm:py-1.5 text-xs border rounded-md w-full sm:w-52 bg-white"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading daily rate list...</div>
          ) : filteredProduce.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">No matching produce items found.</div>
          ) : (
            <>
              {/* Mobile Card Feed (block md:hidden) */}
              <div className="block md:hidden space-y-3">
                {filteredProduce.map((product) => {
                  const mrp = Number(product.price) || 0;
                  const sp = Number(product.selling_price) || mrp;
                  const discount = mrp > sp ? Math.round(((mrp - sp) / mrp) * 100) : 0;
                  const isUpdating = updatingId === product.id;

                  return (
                    <div
                      key={`mobile-rate-${product.id}`}
                      className="bg-white rounded-xl border border-gray-200 p-3.5 shadow-xs space-y-3"
                    >
                      {/* Top Header: Name, Category, Unit */}
                      <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0">
                          <h3 className="font-semibold text-sm text-gray-900 leading-tight">
                            {product.name}
                          </h3>
                          <div className="flex items-center gap-2 mt-1">
                            <span className="text-[10px] bg-emerald-50 text-emerald-800 border border-emerald-200 px-2 py-0.5 rounded-full font-medium">
                              {product.category?.name || "General"}
                            </span>
                            <span className="text-xs text-gray-500 font-medium">
                              {product.unit}
                            </span>
                          </div>
                        </div>

                        {/* Fresh Today Toggle Pill */}
                        <button
                          type="button"
                          onClick={() => toggleFreshToday(product.id, product.fresh_today)}
                          disabled={isUpdating}
                          className={`min-h-[38px] px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-all shrink-0 ${
                            product.fresh_today
                              ? "bg-emerald-600 text-white shadow-xs"
                              : "bg-gray-100 text-gray-600 border border-gray-200"
                          }`}
                        >
                          <CheckCircle2 className={`h-3.5 w-3.5 ${product.fresh_today ? "text-white" : "text-gray-400"}`} />
                          <span>{product.fresh_today ? "Fresh Today" : "Not Featured"}</span>
                        </button>
                      </div>

                      {/* Pricing Inputs: Mandi MRP & Selling Price */}
                      <div className="grid grid-cols-2 gap-3 pt-1">
                        <div className="space-y-1">
                          <label className="text-[11px] font-semibold text-gray-600 block">
                            Market MRP (₹)
                          </label>
                          <input
                            type="number"
                            step="0.01"
                            defaultValue={product.price}
                            key={`m-mrp-${product.id}-${product.price}`}
                            className="w-full min-h-[42px] border border-gray-300 px-3 py-2 rounded-lg text-sm font-medium text-gray-800 bg-gray-50/50 focus:bg-white focus:ring-2 focus:ring-emerald-500 outline-none"
                            onBlur={(e) => {
                              const val = parseFloat(e.target.value);
                              if (!isNaN(val) && val !== product.price) {
                                updatePrice(product.id, "price", val);
                              }
                            }}
                          />
                        </div>

                        <div className="space-y-1">
                          <label className="text-[11px] font-semibold text-emerald-800 block">
                            Selling Price (₹)
                          </label>
                          <input
                            type="number"
                            step="0.01"
                            defaultValue={product.selling_price || product.price}
                            key={`m-sp-${product.id}-${product.selling_price}`}
                            className="w-full min-h-[42px] border border-emerald-300 px-3 py-2 rounded-lg text-sm font-bold text-emerald-700 bg-emerald-50/30 focus:bg-white focus:ring-2 focus:ring-emerald-500 outline-none"
                            onBlur={(e) => {
                              const val = parseFloat(e.target.value);
                              if (!isNaN(val) && val !== product.selling_price) {
                                updatePrice(product.id, "selling_price", val);
                              }
                            }}
                          />
                        </div>
                      </div>

                      {/* Footer: Discount & Sync Status */}
                      <div className="flex items-center justify-between text-xs pt-1 border-t border-gray-100">
                        <div className="flex items-center gap-1.5">
                          <span className="text-gray-500 text-[11px]">Calculated Margin:</span>
                          {discount > 0 ? (
                            <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded">
                              {discount}% OFF
                            </span>
                          ) : (
                            <span className="text-[11px] text-gray-400 font-medium">0%</span>
                          )}
                        </div>

                        <span className="text-[11px] text-gray-400 italic">
                          {isUpdating ? "Saving..." : "✓ Synced"}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Desktop Table View (hidden md:block) */}
              <div className="hidden md:block overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Produce Item</TableHead>
                      <TableHead>Category</TableHead>
                      <TableHead>Unit</TableHead>
                      <TableHead>Market MRP (₹)</TableHead>
                      <TableHead>Selling Price (₹)</TableHead>
                      <TableHead>Discount</TableHead>
                      <TableHead>Fresh Today?</TableHead>
                      <TableHead className="text-right">Sync Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredProduce.map((product) => {
                      const mrp = Number(product.price) || 0;
                      const sp = Number(product.selling_price) || mrp;
                      const discount = mrp > sp ? Math.round(((mrp - sp) / mrp) * 100) : 0;

                      return (
                        <TableRow key={product.id}>
                          <TableCell className="font-semibold text-xs text-gray-900">
                            {product.name}
                          </TableCell>
                          <TableCell className="text-xs text-gray-500">
                            {product.category?.name || "General"}
                          </TableCell>
                          <TableCell className="text-xs text-gray-500">{product.unit}</TableCell>
                          <TableCell>
                            <input
                              type="number"
                              step="0.01"
                              defaultValue={product.price}
                              key={`mrp-${product.id}-${product.price}`}
                              className="w-20 border px-2 py-1 rounded text-xs text-gray-700 bg-white"
                              onBlur={(e) => {
                                const val = parseFloat(e.target.value);
                                if (!isNaN(val) && val !== product.price) {
                                  updatePrice(product.id, "price", val);
                                }
                              }}
                            />
                          </TableCell>
                          <TableCell>
                            <input
                              type="number"
                              step="0.01"
                              defaultValue={product.selling_price || product.price}
                              key={`sp-${product.id}-${product.selling_price}`}
                              className="w-20 border px-2 py-1 rounded text-xs font-bold text-green-700 bg-white"
                              onBlur={(e) => {
                                const val = parseFloat(e.target.value);
                                if (!isNaN(val) && val !== product.selling_price) {
                                  updatePrice(product.id, "selling_price", val);
                                }
                              }}
                            />
                          </TableCell>
                          <TableCell>
                            {discount > 0 ? (
                              <span className="text-[11px] font-bold text-green-700 bg-green-50 border border-green-200 px-1.5 py-0.5 rounded">
                                {discount}% OFF
                              </span>
                            ) : (
                              <span className="text-xs text-gray-400">0%</span>
                            )}
                          </TableCell>
                          <TableCell>
                            <input
                              type="checkbox"
                              checked={product.fresh_today || false}
                              onChange={() => toggleFreshToday(product.id, product.fresh_today)}
                              disabled={updatingId === product.id}
                              className="w-4 h-4 cursor-pointer text-emerald-600 rounded"
                            />
                          </TableCell>
                          <TableCell className="text-right text-xs text-gray-400">
                            {updatingId === product.id ? "Saving..." : "Synced"}
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
