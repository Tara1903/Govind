"use client";

import { useEffect, useState } from "react";
import { createClient } from "../../../utils/supabase/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

export default function FreshBoardPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProducts();
  }, []);

  async function fetchProducts() {
    setLoading(true);
    const { data, error } = await supabase
      .from('products')
      .select('id, name, on_fresh_board, selling_price, active')
      .order('name', { ascending: true });

    if (!error && data) {
      setProducts(data);
    }
    setLoading(false);
  }

  async function toggleFreshBoard(id: string, currentValue: boolean) {
    const newValue = !currentValue;
    // Optimistic update
    setProducts(products.map(p => p.id === id ? { ...p, on_fresh_board: newValue } : p));
    
    await supabase.from('products').update({ on_fresh_board: newValue }).eq('id', id);
  }

  async function updateSellingPrice(id: string, newPrice: string) {
    const priceNum = parseFloat(newPrice);
    if (isNaN(priceNum)) return;
    
    setProducts(products.map(p => p.id === id ? { ...p, selling_price: priceNum } : p));
    await supabase.from('products').update({ selling_price: priceNum }).eq('id', id);
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Fresh Board Controller</h2>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Manage Fresh Board Products</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p>Loading products...</p>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Product Name</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>On Fresh Board</TableHead>
                    <TableHead>Selling Price (₹)</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {products.map((product) => (
                    <TableRow key={product.id}>
                      <TableCell className="font-medium">{product.name}</TableCell>
                      <TableCell>
                        <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                          product.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                        }`}>
                          {product.active ? 'Active' : 'Inactive'}
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
                        <div className="flex items-center">
                          <span className="mr-2 text-sm text-gray-500">₹</span>
                          <input 
                            type="number" 
                            step="0.01"
                            className="w-24 border px-2 py-1 rounded text-sm" 
                            value={product.selling_price || 0} 
                            onChange={e => {
                              const val = e.target.value;
                              setProducts(products.map(p => p.id === product.id ? { ...p, selling_price: val } : p));
                            }}
                            onBlur={e => updateSellingPrice(product.id, e.target.value)}
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
