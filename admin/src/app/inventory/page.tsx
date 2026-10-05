"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Search, AlertTriangle, CheckCircle2, XCircle, RefreshCw, Plus, Minus } from "lucide-react";

export default function InventoryPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<"ALL" | "LOW" | "OUT" | "IN">("ALL");
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  useEffect(() => {
    fetchInventory();
  }, []);

  async function fetchInventory() {
    setLoading(true);
    const { data, error } = await supabase
      .from("products")
      .select("id, name, unit, stock_quantity, low_stock_threshold, selling_price, active, experience_type")
      .order("stock_quantity", { ascending: true });

    if (!error && data) {
      setProducts(data);
    }
    setLoading(false);
  }

  const updateStock = async (id: string, newStock: number, threshold?: number) => {
    const validStock = Math.max(0, newStock);
    setUpdatingId(id);

    // Optimistic update
    setProducts((prev) =>
      prev.map((p) =>
        p.id === id
          ? {
              ...p,
              stock_quantity: validStock,
              low_stock_threshold: threshold !== undefined ? threshold : p.low_stock_threshold,
            }
          : p
      )
    );

    const updatePayload: any = { stock_quantity: validStock };
    if (threshold !== undefined) updatePayload.low_stock_threshold = threshold;

    const { error } = await supabase.from("products").update(updatePayload).eq("id", id);
    if (error) {
      alert("Error updating inventory: " + error.message);
      fetchInventory();
    }
    setUpdatingId(null);
  };

  const getStockStatus = (stock: number, threshold: number) => {
    if (stock <= 0) return { label: "Out of Stock", color: "bg-red-100 text-red-800 border-red-300", type: "OUT" };
    if (stock <= threshold) return { label: "Low Stock", color: "bg-amber-100 text-amber-800 border-amber-300", type: "LOW" };
    return { label: "In Stock", color: "bg-green-100 text-green-800 border-green-300", type: "IN" };
  };

  const filteredProducts = products.filter((p) => {
    const threshold = p.low_stock_threshold || 10;
    const status = getStockStatus(p.stock_quantity, threshold).type;
    const matchesSearch = p.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === "ALL" || status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const lowStockCount = products.filter((p) => p.stock_quantity > 0 && p.stock_quantity <= (p.low_stock_threshold || 10)).length;
  const outOfStockCount = products.filter((p) => p.stock_quantity <= 0).length;

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl md:text-3xl font-bold tracking-tight text-gray-900">Inventory Management</h2>
          <p className="text-xs md:text-sm text-gray-500">Live stock levels, threshold warnings, and inventory adjustments</p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchInventory} disabled={loading} className="self-start sm:self-auto h-9 text-xs font-semibold flex items-center gap-1.5">
          <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      {/* Stock Health Badges */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div
          onClick={() => setStatusFilter("ALL")}
          className={`p-3.5 rounded-xl border cursor-pointer transition-all ${
            statusFilter === "ALL" ? "border-green-600 bg-green-50/50 shadow-sm" : "bg-white"
          }`}
        >
          <div className="text-[11px] font-semibold text-gray-500 uppercase">Total Tracked Items</div>
          <div className="text-xl md:text-2xl font-bold text-gray-900 mt-0.5">{products.length}</div>
        </div>

        <div
          onClick={() => setStatusFilter("LOW")}
          className={`p-3.5 rounded-xl border cursor-pointer transition-all ${
            statusFilter === "LOW" ? "border-amber-600 bg-amber-50/50 shadow-sm" : "bg-white"
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-semibold text-amber-700 uppercase">Low Stock Alerts</span>
            <AlertTriangle className="h-4 w-4 text-amber-600" />
          </div>
          <div className="text-xl md:text-2xl font-bold text-amber-700 mt-0.5">{lowStockCount}</div>
        </div>

        <div
          onClick={() => setStatusFilter("OUT")}
          className={`p-3.5 rounded-xl border cursor-pointer transition-all ${
            statusFilter === "OUT" ? "border-red-600 bg-red-50/50 shadow-sm" : "bg-white"
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-semibold text-red-700 uppercase">Out of Stock</span>
            <XCircle className="h-4 w-4 text-red-600" />
          </div>
          <div className="text-xl md:text-2xl font-bold text-red-700 mt-0.5">{outOfStockCount}</div>
        </div>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3 px-3.5 sm:px-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <CardTitle className="text-base font-semibold">
                Catalogue Stock List ({filteredProducts.length})
              </CardTitle>
              <CardDescription className="text-xs">
                Adjust stock quantities with automatic persistence
              </CardDescription>
            </div>
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
              <div className="relative flex-1 sm:flex-initial">
                <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search item..."
                  className="pl-8 pr-3 py-1.5 text-xs border rounded-md w-full sm:w-48 bg-white"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>

              <select
                className="text-xs border rounded-md px-2.5 py-1.5 bg-white font-medium"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as any)}
              >
                <option value="ALL">All Items</option>
                <option value="IN">In Stock</option>
                <option value="LOW">Low Stock</option>
                <option value="OUT">Out of Stock</option>
              </select>
            </div>
          </div>
        </CardHeader>
        <CardContent className="px-3.5 sm:px-6">
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading live stock data...</div>
          ) : filteredProducts.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">No products match current filters.</div>
          ) : (
            <>
              {/* MOBILE VIEW: Touch-Friendly Inventory Adjustment Cards */}
              <div className="block md:hidden space-y-3">
                {filteredProducts.map((item) => {
                  const threshold = item.low_stock_threshold || 10;
                  const status = getStockStatus(item.stock_quantity, threshold);
                  const isSaving = updatingId === item.id;

                  return (
                    <div
                      key={item.id}
                      className="p-3.5 bg-white rounded-xl border border-gray-200 shadow-sm space-y-3"
                    >
                      {/* Header: Name, Experience, Status */}
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <div className="flex items-center gap-1.5 mb-1">
                            <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-gray-100 text-gray-700">
                              {item.experience_type}
                            </span>
                            <span className="text-xs text-gray-500 font-medium">({item.unit})</span>
                          </div>
                          <h3 className="font-semibold text-sm text-gray-900 leading-snug">
                            {item.name}
                          </h3>
                        </div>
                        <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold border shrink-0 ${status.color}`}>
                          {status.label}
                        </span>
                      </div>

                      {/* Inputs: Current Stock & Low Threshold */}
                      <div className="grid grid-cols-2 gap-2 pt-1 border-t border-gray-100 text-xs">
                        <div>
                          <label className="text-[10px] text-gray-500 uppercase font-semibold block mb-1">
                            Current Stock
                          </label>
                          <input
                            type="number"
                            min="0"
                            className="w-full border px-3 py-2 rounded-lg text-sm text-center font-bold bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-emerald-600"
                            defaultValue={item.stock_quantity}
                            key={`${item.id}-${item.stock_quantity}`}
                            onBlur={(e) => {
                              const val = parseInt(e.target.value, 10);
                              if (!isNaN(val) && val !== item.stock_quantity) {
                                updateStock(item.id, val);
                              }
                            }}
                          />
                        </div>
                        <div>
                          <label className="text-[10px] text-gray-500 uppercase font-semibold block mb-1">
                            Low Alert Threshold
                          </label>
                          <input
                            type="number"
                            min="1"
                            className="w-full border px-3 py-2 rounded-lg text-sm text-center text-gray-700 bg-white focus:outline-none focus:ring-2 focus:ring-emerald-600"
                            defaultValue={threshold}
                            key={`th-${item.id}-${threshold}`}
                            onBlur={(e) => {
                              const val = parseInt(e.target.value, 10);
                              if (!isNaN(val) && val !== threshold) {
                                updateStock(item.id, item.stock_quantity, val);
                              }
                            }}
                          />
                        </div>
                      </div>

                      {/* Quick Stepper Buttons */}
                      <div className="pt-1">
                        <div className="text-[10px] text-gray-500 font-semibold uppercase mb-1.5">
                          Quick Adjustment
                        </div>
                        <div className="grid grid-cols-4 gap-1.5">
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-10 text-xs font-bold border-gray-300 text-gray-700 active:bg-gray-100"
                            disabled={isSaving || item.stock_quantity <= 0}
                            onClick={() => updateStock(item.id, item.stock_quantity - 5)}
                          >
                            -5
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-10 text-xs font-bold border-gray-300 text-gray-700 active:bg-gray-100"
                            disabled={isSaving || item.stock_quantity <= 0}
                            onClick={() => updateStock(item.id, item.stock_quantity - 1)}
                          >
                            -1
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-10 text-xs font-bold bg-emerald-50 text-emerald-800 border-emerald-200 active:bg-emerald-100"
                            disabled={isSaving}
                            onClick={() => updateStock(item.id, item.stock_quantity + 1)}
                          >
                            +1
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-10 text-xs font-bold bg-emerald-50 text-emerald-800 border-emerald-200 active:bg-emerald-100"
                            disabled={isSaving}
                            onClick={() => updateStock(item.id, item.stock_quantity + 5)}
                          >
                            +5
                          </Button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* DESKTOP VIEW: Inventory Table */}
              <div className="hidden md:block overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Product Name</TableHead>
                      <TableHead>Experience</TableHead>
                      <TableHead>Unit</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Stock Quantity</TableHead>
                      <TableHead>Low Threshold</TableHead>
                      <TableHead className="text-right">Quick Stock Adjustment</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredProducts.map((item) => {
                      const threshold = item.low_stock_threshold || 10;
                      const status = getStockStatus(item.stock_quantity, threshold);
                      const isSaving = updatingId === item.id;

                      return (
                        <TableRow key={item.id}>
                          <TableCell className="font-semibold text-xs text-gray-900">
                            {item.name}
                          </TableCell>
                          <TableCell>
                            <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-gray-100 text-gray-700">
                              {item.experience_type}
                            </span>
                          </TableCell>
                          <TableCell className="text-xs text-gray-500">{item.unit}</TableCell>
                          <TableCell>
                            <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold border ${status.color}`}>
                              {status.label}
                            </span>
                          </TableCell>
                          <TableCell>
                            <input
                              type="number"
                              min="0"
                              className="w-20 border px-2 py-1 rounded text-xs text-center font-bold bg-white"
                              defaultValue={item.stock_quantity}
                              key={`${item.id}-${item.stock_quantity}`}
                              onBlur={(e) => {
                                const val = parseInt(e.target.value, 10);
                                if (!isNaN(val) && val !== item.stock_quantity) {
                                  updateStock(item.id, val);
                                }
                              }}
                            />
                          </TableCell>
                          <TableCell>
                            <input
                              type="number"
                              min="1"
                              className="w-16 border px-2 py-1 rounded text-xs text-center text-gray-600 bg-white"
                              defaultValue={threshold}
                              key={`th-${item.id}-${threshold}`}
                              onBlur={(e) => {
                                const val = parseInt(e.target.value, 10);
                                if (!isNaN(val) && val !== threshold) {
                                  updateStock(item.id, item.stock_quantity, val);
                                }
                              }}
                            />
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1">
                              <Button
                                variant="outline"
                                size="sm"
                                className="h-7 px-2 text-xs"
                                disabled={isSaving || item.stock_quantity <= 0}
                                onClick={() => updateStock(item.id, item.stock_quantity - 5)}
                                title="Decrease 5"
                              >
                                -5
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                className="h-7 px-2 text-xs"
                                disabled={isSaving || item.stock_quantity <= 0}
                                onClick={() => updateStock(item.id, item.stock_quantity - 1)}
                                title="Decrease 1"
                              >
                                -1
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                className="h-7 px-2 text-xs bg-green-50 text-green-700 hover:bg-green-100"
                                disabled={isSaving}
                                onClick={() => updateStock(item.id, item.stock_quantity + 1)}
                                title="Increase 1"
                              >
                                +1
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                className="h-7 px-2 text-xs bg-green-50 text-green-700 hover:bg-green-100"
                                disabled={isSaving}
                                onClick={() => updateStock(item.id, item.stock_quantity + 5)}
                                title="Increase 5"
                              >
                                +5
                              </Button>
                            </div>
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
