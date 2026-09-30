"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Sparkles, Search, RefreshCw } from "lucide-react";

export default function FreshBoardPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    fetchProducts();
  }, []);

  async function fetchProducts() {
    setLoading(true);
    const { data, error } = await supabase
      .from("products")
      .select("id, name, on_fresh_board, price, selling_price, active, unit, experience_type")
      .order("on_fresh_board", { ascending: false })
      .order("name", { ascending: true });

    if (!error && data) {
      setProducts(data);
    }
    setLoading(false);
  }

  async function toggleFreshBoard(id: string, currentValue: boolean) {
    const newValue = !currentValue;
    // Optimistic update
    setProducts((prev) =>
      prev.map((p) => (p.id === id ? { ...p, on_fresh_board: newValue } : p))
    );

    const { error } = await supabase
      .from("products")
      .update({ on_fresh_board: newValue })
      .eq("id", id);

    if (error) {
      alert("Error toggling Fresh Board: " + error.message);
      fetchProducts();
    }
  }

  async function updateSellingPrice(id: string, newPrice: string) {
    const priceNum = parseFloat(newPrice);
    if (isNaN(priceNum) || priceNum < 0) return;

    setProducts((prev) =>
      prev.map((p) => (p.id === id ? { ...p, selling_price: priceNum } : p))
    );

    await supabase.from("products").update({ selling_price: priceNum }).eq("id", id);
  }

  const filteredProducts = products.filter((p) =>
    p.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const activeOnBoardCount = products.filter((p) => p.on_fresh_board).length;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <Sparkles className="h-7 w-7 text-green-600" />
            Fresh Board Controller
          </h2>
          <p className="text-sm text-gray-500">
            Feature marquee produce on the customer homepage &quot;Today at Govind&quot; board
          </p>
        </div>
        <div className="flex items-center gap-3">
          <div className="px-3 py-1.5 rounded-lg bg-green-50 border border-green-200 text-xs font-semibold text-green-800">
            {activeOnBoardCount} Featured on Board
          </div>
          <Button variant="outline" size="sm" onClick={fetchProducts} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </Button>
        </div>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <CardTitle className="text-base font-semibold">
                Board Candidate Products ({filteredProducts.length})
              </CardTitle>
              <CardDescription className="text-xs">
                Toggle the switch to display or remove items from the customer Fresh Board
              </CardDescription>
            </div>
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
              <input
                type="text"
                placeholder="Search products..."
                className="pl-8 pr-3 py-1.5 text-xs border rounded-md w-52 bg-white"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading products...</div>
          ) : filteredProducts.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">No matching products found.</div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Product Name</TableHead>
                    <TableHead>Unit</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>On Fresh Board?</TableHead>
                    <TableHead>Selling Price (₹)</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredProducts.map((product) => (
                    <TableRow key={product.id} className={product.on_fresh_board ? "bg-green-50/30" : ""}>
                      <TableCell className="font-semibold text-xs text-gray-900">
                        {product.name}
                      </TableCell>
                      <TableCell className="text-xs text-gray-500">{product.unit}</TableCell>
                      <TableCell>
                        <span
                          className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold ${
                            product.active
                              ? "bg-green-100 text-green-800"
                              : "bg-gray-100 text-gray-600"
                          }`}
                        >
                          {product.active ? "Active" : "Inactive"}
                        </span>
                      </TableCell>
                      <TableCell>
                        <label className="relative inline-flex items-center cursor-pointer">
                          <input
                            type="checkbox"
                            className="sr-only peer"
                            checked={product.on_fresh_board || false}
                            onChange={() => toggleFreshBoard(product.id, product.on_fresh_board || false)}
                          />
                          <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-green-600"></div>
                        </label>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1.5">
                          <span className="text-xs text-gray-500">₹</span>
                          <input
                            type="number"
                            step="0.01"
                            className="w-24 border px-2 py-1 rounded text-xs font-semibold text-green-700 bg-white"
                            defaultValue={product.selling_price || product.price}
                            key={`fb-${product.id}-${product.selling_price}`}
                            onBlur={(e) => updateSellingPrice(product.id, e.target.value)}
                          />
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
    </div>
  );
}
