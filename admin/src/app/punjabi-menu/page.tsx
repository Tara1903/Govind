"use client";

import { useEffect, useState } from "react";
import { createClient } from "../../../utils/supabase/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

export default function PunjabiMenuPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  useEffect(() => {
    fetchMenu();
  }, []);

  async function fetchMenu() {
    setLoading(true);
    // Fetch products that might be Punjabi Food
    // We can filter by category or just load all products where category includes 'punjabi' or 'food'
    const { data, error } = await supabase
      .from('products')
      .select(`
        *,
        category:categories(name)
      `)
      .order('name', { ascending: true });

    if (!error && data) {
      // For now, let's just show products that are not 'Vegetables'/'Fruits' as Punjabi Menu items,
      // or simply show all if none found so admin can manage.
      const menuItems = data.filter(p => !p.category?.name?.toLowerCase()?.includes('vegetable') && !p.category?.name?.toLowerCase()?.includes('fruit'));
      setProducts(menuItems.length > 0 ? menuItems : data);
    }
    setLoading(false);
  }

  async function updateField(id: string, field: string, value: any) {
    setUpdatingId(id);
    const { error } = await supabase
      .from('products')
      .update({ [field]: value })
      .eq('id', id);
      
    if (!error) {
      setProducts(products.map(p => p.id === id ? { ...p, [field]: value } : p));
    } else {
      alert("Error updating " + field);
    }
    setUpdatingId(null);
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Today's Punjabi Menu</h2>
        <Button onClick={fetchMenu}>Refresh</Button>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Manage Today's Menu Items</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p>Loading...</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Dish Name</TableHead>
                  <TableHead>Menu Date</TableHead>
                  <TableHead>Daily Special?</TableHead>
                  <TableHead>Combo?</TableHead>
                  <TableHead>Active?</TableHead>
                  <TableHead className="text-right">Action</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {products.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell className="font-medium">{product.name}</TableCell>
                    <TableCell>
                      <input 
                        type="date" 
                        defaultValue={product.menu_date || ''}
                        onChange={(e) => updateField(product.id, 'menu_date', e.target.value || null)}
                        disabled={updatingId === product.id}
                        className="p-1 border rounded"
                      />
                    </TableCell>
                    <TableCell>
                      <input 
                        type="checkbox" 
                        checked={product.daily_special || false}
                        onChange={(e) => updateField(product.id, 'daily_special', e.target.checked)}
                        disabled={updatingId === product.id}
                        className="w-5 h-5 cursor-pointer"
                      />
                    </TableCell>
                    <TableCell>
                      <input 
                        type="checkbox" 
                        checked={product.combo || false}
                        onChange={(e) => updateField(product.id, 'combo', e.target.checked)}
                        disabled={updatingId === product.id}
                        className="w-5 h-5 cursor-pointer"
                      />
                    </TableCell>
                    <TableCell>
                      <input 
                        type="checkbox" 
                        checked={product.active || false}
                        onChange={(e) => updateField(product.id, 'active', e.target.checked)}
                        disabled={updatingId === product.id}
                        className="w-5 h-5 cursor-pointer"
                      />
                    </TableCell>
                    <TableCell className="text-right text-sm text-gray-500">
                      {updatingId === product.id ? "Saving..." : ""}
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
