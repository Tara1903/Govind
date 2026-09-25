"use client";

import { useEffect, useState } from "react";
import { createClient } from "../../../utils/supabase/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

export default function DailyRatesPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  useEffect(() => {
    fetchDailyRates();
  }, []);

  async function fetchDailyRates() {
    setLoading(true);
    // Fetch products that might be vegetables (we can just fetch all or filter by category if we knew the ID)
    // For now, let's fetch all active products that are "fresh_today" or we can just fetch all products
    // and let the admin filter. To keep it simple, we fetch all active products for now.
    // Assuming 'Vegetables' is a category, we try to match it.
    const { data, error } = await supabase
      .from('products')
      .select(`
        *,
        category:categories(name)
      `)
      .eq('active', true)
      .order('name', { ascending: true });

    if (!error && data) {
      // Filter for Fresh Vegetables in frontend for flexibility if category name varies
      const vegProducts = data.filter(p => p?.category?.name?.toLowerCase()?.includes('vegetable') || p?.category?.name?.toLowerCase()?.includes('fruit') || p.fresh_today);
      setProducts(vegProducts.length > 0 ? vegProducts : data);
    }
    setLoading(false);
  }

  async function updatePrice(id: string, newPrice: number) {
    setUpdatingId(id);
    const { error } = await supabase
      .from('products')
      .update({ price: newPrice, selling_price: newPrice })
      .eq('id', id);
      
    if (!error) {
      setProducts(products.map(p => p.id === id ? { ...p, price: newPrice, selling_price: newPrice } : p));
    } else {
      alert("Error updating price");
    }
    setUpdatingId(null);
  }
  
  async function toggleFreshToday(id: string, currentValue: boolean) {
    setUpdatingId(id);
    const { error } = await supabase
      .from('products')
      .update({ fresh_today: !currentValue })
      .eq('id', id);
      
    if (!error) {
      setProducts(products.map(p => p.id === id ? { ...p, fresh_today: !currentValue } : p));
    }
    setUpdatingId(null);
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Daily Rate List</h2>
        <Button onClick={fetchDailyRates}>Refresh</Button>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Fresh Produce - Quick Price Edit</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p>Loading...</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product</TableHead>
                  <TableHead>Unit</TableHead>
                  <TableHead>Current Price (₹)</TableHead>
                  <TableHead>Fresh Today?</TableHead>
                  <TableHead className="text-right">Quick Update</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {products.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell className="font-medium">{product.name}</TableCell>
                    <TableCell>{product.unit}</TableCell>
                    <TableCell>
                      <input 
                        type="number" 
                        defaultValue={product.price}
                        className="w-24 p-1 border rounded"
                        onBlur={(e) => {
                          const val = parseFloat(e.target.value);
                          if (val !== product.price && !isNaN(val)) {
                            updatePrice(product.id, val);
                          }
                        }}
                        disabled={updatingId === product.id}
                      />
                    </TableCell>
                    <TableCell>
                      <input 
                        type="checkbox" 
                        checked={product.fresh_today || false}
                        onChange={() => toggleFreshToday(product.id, product.fresh_today)}
                        disabled={updatingId === product.id}
                        className="w-5 h-5 cursor-pointer"
                      />
                    </TableCell>
                    <TableCell className="text-right text-sm text-gray-500">
                      {updatingId === product.id ? "Updating..." : "Auto-saves on blur"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
