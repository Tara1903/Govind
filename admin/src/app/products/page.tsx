"use client";

import { useEffect, useState } from "react";
import { createClient } from "../../../utils/supabase/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

export default function ProductsPage() {
  const supabase = createClient();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // Form state
  const [isEditing, setIsEditing] = useState(false);
  const [currentProduct, setCurrentProduct] = useState<any>(null);
  const [formData, setFormData] = useState({
    name: '', price: '', selling_price: '', unit: '', stock_quantity: '', 
    description: '', image_url: '',
    active: true, category_id: '',
    on_fresh_board: false, product_type: 'SINGLE', bundle_items: '',
    bulk_available: false
  });

  const [categories, setCategories] = useState<any[]>([]);

  useEffect(() => {
    fetchCategories();
    fetchProducts();
  }, []);

  async function fetchCategories() {
    const { data } = await supabase.from('categories').select('*');
    if (data) setCategories(data);
  }

  async function fetchProducts() {
    setLoading(true);
    const { data, error } = await supabase
      .from('products')
      .select(`
        *,
        category:categories(name)
      `)
      .order('created_at', { ascending: false });

    if (!error && data) {
      setProducts(data);
    }
    setLoading(false);
  }

  const handleEdit = (product: any) => {
    setCurrentProduct(product);
    setFormData({
      name: product.name || '',
      price: product.price?.toString() || '',
      selling_price: product.selling_price?.toString() || '',
      unit: product.unit || '',
      stock_quantity: product.stock_quantity?.toString() || '',
      description: product.description || '',
      image_url: product.image_url || '',
      active: product.active,
      category_id: product.category_id || '',
      on_fresh_board: product.on_fresh_board || false,
      product_type: product.product_type || 'SINGLE',
      bundle_items: product.bundle_items ? JSON.stringify(product.bundle_items) : '',
      bulk_available: product.bulk_available || false
    });
    setIsEditing(true);
  };

  const handleAdd = () => {
    setCurrentProduct(null);
    setFormData({
      name: '', price: '', selling_price: '', unit: '1 item', stock_quantity: '0', 
      description: '', image_url: '',
      active: true, category_id: '',
      on_fresh_board: false, product_type: 'SINGLE', bundle_items: '',
      bulk_available: false
    });
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    setCurrentProduct(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    let parsedBundleItems = null;
    if (formData.bundle_items && formData.bundle_items.trim() !== '') {
      try {
        parsedBundleItems = JSON.parse(formData.bundle_items);
      } catch (e) {
        alert("Invalid JSON in Bundle Items");
        setLoading(false);
        return;
      }
    }

    if (currentProduct) {
      // Update
      await supabase.from('products').update({
        name: formData.name,
        price: parseFloat(formData.price),
        selling_price: parseFloat(formData.selling_price) || parseFloat(formData.price),
        unit: formData.unit || '1 item',
        stock_quantity: parseInt(formData.stock_quantity, 10),
        description: formData.description,
        image_url: formData.image_url,
        active: formData.active,
        category_id: formData.category_id || null,
        on_fresh_board: formData.on_fresh_board,
        product_type: formData.product_type,
        bundle_items: parsedBundleItems,
        bulk_available: formData.bulk_available
      }).eq('id', currentProduct.id);
    } else {
      // Insert
      // Generate simple slug
      const slug = formData.name.toLowerCase().replace(/\s+/g, '-');
      await supabase.from('products').insert([{
        name: formData.name,
        slug,
        price: parseFloat(formData.price),
        selling_price: parseFloat(formData.selling_price) || parseFloat(formData.price),
        unit: formData.unit || '1 item',
        stock_quantity: parseInt(formData.stock_quantity, 10),
        description: formData.description,
        image_url: formData.image_url,
        active: formData.active,
        category_id: formData.category_id || null,
        on_fresh_board: formData.on_fresh_board,
        product_type: formData.product_type,
        bundle_items: parsedBundleItems,
        bulk_available: formData.bulk_available
      }]);
    }
    setIsEditing(false);
    fetchProducts();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Products (Govind)</h2>
        {!isEditing && <Button onClick={handleAdd}>Add Product</Button>}
      </div>
      
      {isEditing ? (
        <Card>
          <CardHeader>
            <CardTitle>{currentProduct ? 'Edit Product' : 'Add Product'}</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4 max-w-md">
              <div>
                <label className="block text-sm font-medium mb-1">Name</label>
                <input required type="text" className="w-full border px-3 py-2 rounded" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Category</label>
                <select className="w-full border px-3 py-2 rounded" value={formData.category_id} onChange={e => setFormData({...formData, category_id: e.target.value})}>
                  <option value="">Select Category</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Original Price</label>
                <input required type="number" step="0.01" className="w-full border px-3 py-2 rounded" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Selling Price (Discounted)</label>
                <input type="number" step="0.01" className="w-full border px-3 py-2 rounded" value={formData.selling_price} onChange={e => setFormData({...formData, selling_price: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Unit (e.g. 1 kg, 500 g, 1 bunch)</label>
                <input required type="text" className="w-full border px-3 py-2 rounded" value={formData.unit} onChange={e => setFormData({...formData, unit: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Image URL</label>
                <input type="url" className="w-full border px-3 py-2 rounded" value={formData.image_url} onChange={e => setFormData({...formData, image_url: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Description</label>
                <textarea className="w-full border px-3 py-2 rounded" value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Stock</label>
                <input required type="number" className="w-full border px-3 py-2 rounded" value={formData.stock_quantity} onChange={e => setFormData({...formData, stock_quantity: e.target.value})} />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-1">Product Type</label>
                <select className="w-full border px-3 py-2 rounded" value={formData.product_type} onChange={e => setFormData({...formData, product_type: e.target.value})}>
                  <option value="SINGLE">Single</option>
                  <option value="PACK">Pack</option>
                  <option value="COMBO">Combo</option>
                </select>
              </div>

              {(formData.product_type === 'PACK' || formData.product_type === 'COMBO') && (
                <div>
                  <label className="block text-sm font-medium mb-1">Bundle Items (JSON)</label>
                  <textarea 
                    className="w-full border px-3 py-2 rounded h-24 font-mono text-sm" 
                    placeholder='[{"product_id": "...", "qty": 1}]'
                    value={formData.bundle_items} 
                    onChange={e => setFormData({...formData, bundle_items: e.target.value})} 
                  />
                  <p className="text-xs text-gray-500 mt-1">Must be valid JSON array of objects.</p>
                </div>
              )}

              <div className="flex items-center">
                <input type="checkbox" id="active" checked={formData.active} onChange={e => setFormData({...formData, active: e.target.checked})} className="mr-2" />
                <label htmlFor="active" className="text-sm font-medium">Active</label>
              </div>
              <div className="flex items-center">
                <input type="checkbox" id="bulk_available" checked={formData.bulk_available} onChange={e => setFormData({...formData, bulk_available: e.target.checked})} className="mr-2" />
                <label htmlFor="bulk_available" className="text-sm font-medium">Available for Wholesale (Bulk)</label>
              </div>
              <div className="flex items-center">
                <input type="checkbox" id="bulk_available" checked={formData.bulk_available} onChange={e => setFormData({...formData, bulk_available: e.target.checked})} className="mr-2" />
                <label htmlFor="bulk_available" className="text-sm font-medium">Available for Wholesale (Bulk)</label>
              </div>
              
              <div className="flex items-center">
                <input type="checkbox" id="on_fresh_board" checked={formData.on_fresh_board} onChange={e => setFormData({...formData, on_fresh_board: e.target.checked})} className="mr-2" />
                <label htmlFor="on_fresh_board" className="text-sm font-medium">Show on Fresh Board</label>
              </div>

              <div className="flex gap-2 pt-2">
                <Button type="submit">Save</Button>
                <Button type="button" variant="ghost" onClick={handleCancel}>Cancel</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>All Products</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p>Loading products...</p>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Category</TableHead>
                      <TableHead>Price</TableHead>
                      <TableHead>Stock</TableHead>
                      <TableHead>Fresh Board</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {products.map((product) => (
                      <TableRow key={product.id}>
                        <TableCell className="font-medium">{product.name}</TableCell>
                        <TableCell>{product.product_type || 'SINGLE'}</TableCell>
                        <TableCell>{product.category?.name || 'N/A'}</TableCell>
                        <TableCell>₹{product.price}</TableCell>
                        <TableCell>{product.stock_quantity}</TableCell>
                        <TableCell>{product.on_fresh_board ? 'Yes' : 'No'}</TableCell>
                        <TableCell>
                          <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                            product.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                          }`}>
                            {product.active ? 'Active' : 'Inactive'}
                          </span>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button variant="ghost" size="sm" onClick={() => handleEdit(product)}>Edit</Button>
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




