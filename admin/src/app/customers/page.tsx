"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

const mockCustomers = [
  { id: "CUST-1", name: "Ravi Kumar", phone: "+91 9876543210", email: "ravi@example.com", joined: "2026-01-10", role: "customer" },
  { id: "CUST-2", name: "Priya Sharma", phone: "+91 9876543211", email: "priya@example.com", joined: "2026-03-15", role: "customer" },
  { id: "CUST-3", name: "Govind Admin", phone: "+91 9876543212", email: "admin@govind.com", joined: "2025-12-01", role: "admin" },
];

export default function CustomersPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold tracking-tight">Customers</h2>
        <Button>Export</Button>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>All Customers</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Phone</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Role</TableHead>
                <TableHead>Joined</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockCustomers.map((customer) => (
                <TableRow key={customer.id}>
                  <TableCell className="font-medium">{customer.name}</TableCell>
                  <TableCell>{customer.phone}</TableCell>
                  <TableCell>{customer.email}</TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      customer.role === 'admin' ? 'bg-purple-100 text-purple-800' : 'bg-gray-100 text-gray-800'
                    }`}>
                      {customer.role}
                    </span>
                  </TableCell>
                  <TableCell>{customer.joined}</TableCell>
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
