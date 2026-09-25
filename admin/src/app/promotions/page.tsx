"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

const mockPromotions = [
  { id: "PROMO-1", title: "Monsoon Mega Sale", priority: 1, startDate: "2026-07-01", endDate: "2026-08-31", status: "Inactive" },
  { id: "PROMO-2", title: "Diwali Special Offer", priority: 2, startDate: "2026-10-15", endDate: "2026-11-15", status: "Active" },
  { id: "PROMO-3", title: "Weekend Flash Deals", priority: 3, startDate: "2026-09-18", endDate: "2026-09-20", status: "Active" },
];

export default function PromotionsPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Promotions & Banners</h2>
        <Button>Create Promotion</Button>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Active Promotions</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Title</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Start Date</TableHead>
                <TableHead>End Date</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockPromotions.map((promo) => (
                <TableRow key={promo.id}>
                  <TableCell className="font-medium">{promo.title}</TableCell>
                  <TableCell>{promo.priority}</TableCell>
                  <TableCell>{promo.startDate}</TableCell>
                  <TableCell>{promo.endDate}</TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      promo.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {promo.status}
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
