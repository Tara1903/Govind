"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

const mockInventory = [
  { id: "PRD-1", name: "Fresh Apples", stock: 120, lowStockThreshold: 20, status: "In Stock" },
  { id: "PRD-2", name: "Organic Spinach", stock: 5, lowStockThreshold: 10, status: "Low Stock" },
  { id: "PRD-3", name: "Almonds", stock: 50, lowStockThreshold: 10, status: "In Stock" },
  { id: "PRD-4", name: "Cow Milk", stock: 0, lowStockThreshold: 5, status: "Out of Stock" },
];

export default function InventoryPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Inventory</h2>
        <Button>Update Stock</Button>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Inventory Status</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Product Name</TableHead>
                <TableHead>Current Stock</TableHead>
                <TableHead>Low Stock Threshold</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockInventory.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="font-medium">{item.name}</TableCell>
                  <TableCell>{item.stock}</TableCell>
                  <TableCell>{item.lowStockThreshold}</TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      item.status === 'In Stock' ? 'bg-green-100 text-green-800' :
                      item.status === 'Low Stock' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {item.status}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm">Update</Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
