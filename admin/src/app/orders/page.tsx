"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

const mockOrders = [
  { id: "ORD-1001", customer: "John Doe", total: "₹450", status: "CONFIRMED", date: "2026-09-18" },
  { id: "ORD-1002", customer: "Jane Smith", total: "₹1,200", status: "PENDING", date: "2026-09-18" },
  { id: "ORD-1003", customer: "Ravi Kumar", total: "₹340", status: "OUT_FOR_DELIVERY", date: "2026-09-17" },
  { id: "ORD-1004", customer: "Priya Sharma", total: "₹890", status: "DELIVERED", date: "2026-09-17" },
];

export default function OrdersPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Orders</h2>
        <Button>Export</Button>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Recent Orders</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Order ID</TableHead>
                <TableHead>Customer</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Total</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockOrders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell className="font-medium">{order.id}</TableCell>
                  <TableCell>{order.customer}</TableCell>
                  <TableCell>{order.date}</TableCell>
                  <TableCell>{order.total}</TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      order.status === 'DELIVERED' ? 'bg-green-100 text-green-800' :
                      order.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-blue-100 text-blue-800'
                    }`}>
                      {order.status}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm">View</Button>
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
