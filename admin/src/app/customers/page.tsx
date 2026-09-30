"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Users, Search, RefreshCw, Eye, ShoppingCart, IndianRupee, X } from "lucide-react";

export default function CustomersPage() {
  const supabase = createClient();
  const [customers, setCustomers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");

  // Selected customer for modal
  const [selectedCustomer, setSelectedCustomer] = useState<any | null>(null);
  const [customerOrders, setCustomerOrders] = useState<any[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(false);

  useEffect(() => {
    fetchCustomers();
  }, []);

  async function fetchCustomers() {
    setLoading(true);
    // 1. Fetch profiles
    const { data: profiles, error } = await supabase
      .from("profiles")
      .select("*")
      .order("created_at", { ascending: false });

    // 2. Fetch all orders to compute stats
    const { data: orders } = await supabase
      .from("orders")
      .select("customer_id, total, order_status");

    const orderStats: Record<string, { count: number; spend: number }> = {};
    if (orders) {
      orders.forEach((o) => {
        if (o.customer_id) {
          if (!orderStats[o.customer_id]) {
            orderStats[o.customer_id] = { count: 0, spend: 0 };
          }
          orderStats[o.customer_id].count++;
          if (o.order_status !== "CANCELLED") {
            orderStats[o.customer_id].spend += Number(o.total) || 0;
          }
        }
      });
    }

    if (!error && profiles) {
      const enriched = profiles.map((p) => ({
        ...p,
        orderCount: orderStats[p.id]?.count || 0,
        totalSpend: orderStats[p.id]?.spend || 0,
      }));
      setCustomers(enriched);
    }
    setLoading(false);
  }

  const handleViewCustomer = async (cust: any) => {
    setSelectedCustomer(cust);
    setLoadingOrders(true);
    const { data: orders } = await supabase
      .from("orders")
      .select("*")
      .eq("customer_id", cust.id)
      .order("created_at", { ascending: false });

    setCustomerOrders(orders || []);
    setLoadingOrders(false);
  };

  const filteredCustomers = customers.filter((c) => {
    const query = searchQuery.toLowerCase();
    const matchesSearch =
      (c.name && c.name.toLowerCase().includes(query)) ||
      (c.full_name && c.full_name.toLowerCase().includes(query)) ||
      (c.phone && c.phone.includes(query)) ||
      (c.email && c.email.toLowerCase().includes(query));

    const matchesRole = roleFilter === "ALL" || c.role?.toLowerCase() === roleFilter.toLowerCase();
    return matchesSearch && matchesRole;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900">Customers & Users</h2>
          <p className="text-sm text-gray-500">Live profiles, order volume, and customer activity</p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchCustomers} disabled={loading} className="flex items-center gap-1.5">
          <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <CardTitle className="text-base font-semibold">
                Customer Database ({filteredCustomers.length})
              </CardTitle>
              <CardDescription className="text-xs">
                Synchronized with the authentication and profiles system
              </CardDescription>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <div className="relative">
                <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search by name, phone, email..."
                  className="pl-8 pr-3 py-1.5 text-xs border rounded-md w-60 bg-white"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>

              <select
                className="text-xs border rounded-md px-2.5 py-1.5 bg-white font-medium"
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
              >
                <option value="ALL">All Roles</option>
                <option value="customer">Customers</option>
                <option value="delivery">Delivery Partners</option>
                <option value="admin">Administrators</option>
              </select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading user profiles...</div>
          ) : filteredCustomers.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">No matching profiles found.</div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Customer Name</TableHead>
                    <TableHead>Contact (Phone / Email)</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead>Total Orders</TableHead>
                    <TableHead>Total Spend</TableHead>
                    <TableHead>Registered</TableHead>
                    <TableHead className="text-right">Action</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredCustomers.map((cust) => (
                    <TableRow key={cust.id}>
                      <TableCell>
                        <div className="font-semibold text-xs text-gray-900">
                          {cust.name || cust.full_name || "Guest Customer"}
                        </div>
                        <div className="text-[10px] text-gray-400 font-mono">
                          {cust.id.slice(0, 8)}...
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="text-xs text-gray-800">{cust.phone || "No Phone"}</div>
                        <div className="text-[11px] text-gray-500">{cust.email || "No Email"}</div>
                      </TableCell>
                      <TableCell>
                        <span
                          className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold ${
                            cust.role === "admin"
                              ? "bg-purple-100 text-purple-800"
                              : cust.role === "delivery"
                              ? "bg-cyan-100 text-cyan-800"
                              : "bg-green-100 text-green-800"
                          }`}
                        >
                          {cust.role || "customer"}
                        </span>
                      </TableCell>
                      <TableCell className="text-xs font-semibold text-gray-900">
                        {cust.orderCount} orders
                      </TableCell>
                      <TableCell className="text-xs font-bold text-gray-900">
                        ₹{Number(cust.totalSpend).toFixed(2)}
                      </TableCell>
                      <TableCell className="text-xs text-gray-500">
                        {cust.created_at ? new Date(cust.created_at).toLocaleDateString() : "N/A"}
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 text-xs flex items-center gap-1 ml-auto"
                          onClick={() => handleViewCustomer(cust)}
                        >
                          <Eye className="h-3.5 w-3.5" />
                          View
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Customer Detail Drawer / Modal */}
      {selectedCustomer && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
          <Card className="max-w-2xl w-full bg-white shadow-2xl max-h-[85vh] flex flex-col">
            <CardHeader className="flex flex-row items-center justify-between border-b pb-4">
              <div>
                <CardTitle className="text-lg font-bold">
                  {selectedCustomer.name || selectedCustomer.full_name || "Customer Details"}
                </CardTitle>
                <CardDescription className="text-xs">
                  ID: {selectedCustomer.id}
                </CardDescription>
              </div>
              <Button
                variant="ghost"
                size="sm"
                className="h-8 w-8 p-0"
                onClick={() => setSelectedCustomer(null)}
              >
                <X className="h-4 w-4" />
              </Button>
            </CardHeader>
            <CardContent className="overflow-y-auto p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4 p-4 rounded-lg bg-gray-50 border text-xs">
                <div>
                  <span className="text-gray-500 block">Phone Number</span>
                  <span className="font-semibold text-gray-800">{selectedCustomer.phone || "N/A"}</span>
                </div>
                <div>
                  <span className="text-gray-500 block">Email Address</span>
                  <span className="font-semibold text-gray-800">{selectedCustomer.email || "N/A"}</span>
                </div>
                <div>
                  <span className="text-gray-500 block">System Role</span>
                  <span className="font-semibold capitalize text-gray-800">{selectedCustomer.role || "customer"}</span>
                </div>
                <div>
                  <span className="text-gray-500 block">Registered On</span>
                  <span className="font-semibold text-gray-800">
                    {selectedCustomer.created_at ? new Date(selectedCustomer.created_at).toLocaleString() : "N/A"}
                  </span>
                </div>
              </div>

              <div>
                <h4 className="text-sm font-bold text-gray-900 mb-2 flex items-center gap-1.5">
                  <ShoppingCart className="h-4 w-4 text-green-700" />
                  Order History ({customerOrders.length})
                </h4>
                {loadingOrders ? (
                  <div className="py-4 text-center text-xs text-gray-500">Loading order history...</div>
                ) : customerOrders.length === 0 ? (
                  <div className="py-4 text-center text-xs text-gray-500">No orders placed by this customer yet.</div>
                ) : (
                  <div className="border rounded-md divide-y text-xs">
                    {customerOrders.map((o) => (
                      <div key={o.id} className="p-3 flex items-center justify-between hover:bg-gray-50">
                        <div>
                          <div className="font-mono font-semibold">#{o.id.slice(0, 8).toUpperCase()}</div>
                          <div className="text-[11px] text-gray-500">{new Date(o.created_at).toLocaleString()}</div>
                        </div>
                        <div className="text-right">
                          <div className="font-bold text-gray-900">₹{Number(o.total).toFixed(2)}</div>
                          <span className="text-[10px] font-semibold px-1.5 py-0.5 rounded bg-gray-100 text-gray-700">
                            {o.order_status}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
