"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

const mockCoupons = [
  { id: "CPN-1", code: "WELCOME50", type: "fixed", value: "₹50", minOrder: "₹200", status: "Active" },
  { id: "CPN-2", code: "SUMMER20", type: "percentage", value: "20%", minOrder: "₹500", status: "Active" },
  { id: "CPN-3", code: "FREEDEL", type: "fixed", value: "₹40", minOrder: "₹100", status: "Expired" },
];

export default function CouponsPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Coupons</h2>
        <Button>Add Coupon</Button>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>All Coupons</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Code</TableHead>
                <TableHead>Discount</TableHead>
                <TableHead>Min Order</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockCoupons.map((coupon) => (
                <TableRow key={coupon.id}>
                  <TableCell className="font-medium">{coupon.code}</TableCell>
                  <TableCell>{coupon.value}</TableCell>
                  <TableCell>{coupon.minOrder}</TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      coupon.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {coupon.status}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm">Edit</Button>
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
