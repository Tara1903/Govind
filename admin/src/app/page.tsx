"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import {
  Users,
  ShoppingCart,
  IndianRupee,
  Package,
  AlertTriangle,
  ArrowRight,
  TrendingUp,
  RefreshCw,
} from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { Button } from "@/components/ui/button";
import { createClient } from "@/lib/supabase/client";

interface DashboardStats {
  totalRevenue: number;
  totalOrders: number;
  totalCustomers: number;
  activeProducts: number;
  lowStockCount: number;
  statusCounts: Record<string, number>;
  recentOrders: any[];
  dailySales: { name: string; total: number }[];
}

export default function Dashboard() {
  const supabase = createClient();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<DashboardStats>({
    totalRevenue: 0,
    totalOrders: 0,
    totalCustomers: 0,
    activeProducts: 0,
    lowStockCount: 0,
    statusCounts: {},
    recentOrders: [],
    dailySales: [],
  });

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      // 1. Fetch Orders
      const { data: orders } = await supabase
        .from("orders")
        .select(`
          id,
          total,
          order_status,
          created_at,
          customer_id,
          profiles!orders_user_id_fkey(name, phone)
        `)
        .order("created_at", { ascending: false });

      // 2. Fetch Customers
      const { count: customerCount } = await supabase
        .from("profiles")
        .select("*", { count: "exact", head: true })
        .eq("role", "customer");

      // 3. Fetch Products
      const { data: products } = await supabase
        .from("products")
        .select("id, active, stock_quantity, low_stock_threshold");

      let totalRevenue = 0;
      const statusCounts: Record<string, number> = {};
      const salesByDay: Record<string, number> = {};

      // Initialize past 7 days
      const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
      const last7Days: string[] = [];
      for (let i = 6; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        const dayName = days[d.getDay()];
        last7Days.push(dayName);
        salesByDay[dayName] = 0;
      }

      if (orders) {
        orders.forEach((o) => {
          const status = o.order_status || "PLACED";
          statusCounts[status] = (statusCounts[status] || 0) + 1;

          if (status !== "CANCELLED") {
            const amount = Number(o.total) || 0;
            totalRevenue += amount;

            const orderDate = new Date(o.created_at);
            const dayName = days[orderDate.getDay()];
            if (salesByDay[dayName] !== undefined) {
              salesByDay[dayName] += amount;
            }
          }
        });
      }

      const dailySales = last7Days.map((name) => ({
        name,
        total: Math.round(salesByDay[name] || 0),
      }));

      let activeProducts = 0;
      let lowStockCount = 0;
      if (products) {
        products.forEach((p) => {
          if (p.active) activeProducts++;
          const threshold = p.low_stock_threshold || 10;
          if ((p.stock_quantity || 0) <= threshold) lowStockCount++;
        });
      }

      setStats({
        totalRevenue,
        totalOrders: orders?.length || 0,
        totalCustomers: customerCount || 0,
        activeProducts,
        lowStockCount,
        statusCounts,
        recentOrders: (orders || []).slice(0, 5),
        dailySales,
      });
    } catch (err) {
      console.error("Dashboard fetch error:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl md:text-3xl font-bold tracking-tight text-gray-900">Dashboard</h2>
          <p className="text-xs md:text-sm text-gray-500">Live operational overview for Govind Fresh & Kitchen</p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={loadDashboardData}
            disabled={loading}
            className="h-9 text-xs font-semibold flex items-center gap-1.5"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Refresh Data
          </Button>
          <Button asChild size="sm" className="h-9 text-xs font-semibold bg-green-700 hover:bg-green-800 text-white">
            <Link href="/orders">Manage Orders</Link>
          </Button>
        </div>
      </div>

      {/* Top Metric Cards */}
      <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="border-l-4 border-l-green-600 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">Total Revenue</CardTitle>
            <div className="h-8 w-8 rounded-full bg-green-100 text-green-700 flex items-center justify-center">
              <IndianRupee className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">
              ₹{stats.totalRevenue.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <p className="text-xs text-gray-500 mt-1">Calculated from non-cancelled orders</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-blue-600 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">Total Orders</CardTitle>
            <div className="h-8 w-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center">
              <ShoppingCart className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">{stats.totalOrders}</div>
            <div className="flex gap-2 text-xs text-gray-500 mt-1">
              <span>Placed: <b>{stats.statusCounts["PLACED"] || 0}</b></span>
              <span>•</span>
              <span>Delivered: <b>{stats.statusCounts["DELIVERED"] || 0}</b></span>
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-purple-600 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">Registered Customers</CardTitle>
            <div className="h-8 w-8 rounded-full bg-purple-100 text-purple-700 flex items-center justify-center">
              <Users className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">{stats.totalCustomers}</div>
            <p className="text-xs text-gray-500 mt-1">Verified customer profiles</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-amber-600 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">Active Catalogue</CardTitle>
            <div className="h-8 w-8 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center">
              <Package className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-900">{stats.activeProducts}</div>
            <div className="flex items-center gap-1 text-xs text-amber-700 font-medium mt-1">
              {stats.lowStockCount > 0 ? (
                <>
                  <AlertTriangle className="h-3 w-3" />
                  <span>{stats.lowStockCount} items low in stock</span>
                </>
              ) : (
                <span>All items stocked</span>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Charts & Status Grid */}
      <div className="grid gap-6 grid-cols-1 lg:grid-cols-7">
        {/* Sales Chart */}
        <Card className="lg:col-span-4 shadow-sm">
          <CardHeader>
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-green-700" />
              Weekly Revenue Distribution
            </CardTitle>
            <CardDescription className="text-xs">Daily gross order volume over the last 7 days</CardDescription>
          </CardHeader>
          <CardContent className="pl-2">
            <div className="h-[280px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={stats.dailySales}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                  <XAxis dataKey="name" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis
                    stroke="#888888"
                    fontSize={12}
                    tickLine={false}
                    axisLine={false}
                    tickFormatter={(val) => `₹${val}`}
                  />
                  <Tooltip
                    formatter={(val: any) => [`₹${val}`, "Revenue"]}
                    contentStyle={{ borderRadius: 8, borderColor: "#e2e8f0" }}
                  />
                  <Bar dataKey="total" fill="#15803d" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Status Distribution */}
        <Card className="lg:col-span-3 shadow-sm">
          <CardHeader>
            <CardTitle className="text-base font-semibold">Order Pipeline Status</CardTitle>
            <CardDescription className="text-xs">Real-time status breakdown across all orders</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {[
                { key: "PLACED", label: "Placed / New", color: "bg-amber-100 text-amber-800 border-amber-300" },
                { key: "CONFIRMED", label: "Confirmed", color: "bg-blue-100 text-blue-800 border-blue-300" },
                { key: "PREPARING", label: "Preparing in Kitchen / Pack", color: "bg-purple-100 text-purple-800 border-purple-300" },
                { key: "READY_FOR_DELIVERY", label: "Ready for Delivery", color: "bg-indigo-100 text-indigo-800 border-indigo-300" },
                { key: "OUT_FOR_DELIVERY", label: "Out for Delivery", color: "bg-cyan-100 text-cyan-800 border-cyan-300" },
                { key: "DELIVERED", label: "Delivered", color: "bg-green-100 text-green-800 border-green-300" },
                { key: "CANCELLED", label: "Cancelled", color: "bg-gray-100 text-gray-700 border-gray-300" },
              ].map(({ key, label, color }) => {
                const count = stats.statusCounts[key] || 0;
                const percentage = stats.totalOrders > 0 ? Math.round((count / stats.totalOrders) * 100) : 0;
                return (
                  <div key={key} className="flex items-center justify-between text-xs">
                    <span className="font-medium text-gray-700 flex items-center gap-2">
                      <span className={`px-2 py-0.5 rounded border text-[11px] font-semibold ${color}`}>
                        {key}
                      </span>
                      {label}
                    </span>
                    <span className="font-bold text-gray-900">
                      {count} <span className="text-gray-400 font-normal">({percentage}%)</span>
                    </span>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Orders Preview */}
      <Card className="shadow-sm">
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <div>
            <CardTitle className="text-base font-semibold">Latest Customer Orders</CardTitle>
            <CardDescription className="text-xs">Most recent transactions across mobile & web</CardDescription>
          </div>
          <Button variant="ghost" size="sm" asChild className="text-xs text-green-700 hover:text-green-800">
            <Link href="/orders" className="flex items-center gap-1">
              View All Orders <ArrowRight className="h-3 w-3" />
            </Link>
          </Button>
        </CardHeader>
        <CardContent className="px-3.5 sm:px-6">
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading live order stream...</div>
          ) : stats.recentOrders.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-500">No orders recorded yet.</div>
          ) : (
            <>
              {/* MOBILE VIEW: Recent Orders Feed */}
              <div className="block md:hidden space-y-2.5">
                {stats.recentOrders.map((order) => (
                  <div
                    key={order.id}
                    className="p-3 bg-white rounded-xl border border-gray-200 shadow-sm space-y-2"
                  >
                    <div className="flex items-center justify-between">
                      <Link
                        href={`/orders/${order.id}`}
                        className="font-mono font-bold text-xs text-emerald-700 hover:underline"
                      >
                        #{order.id.slice(0, 8).toUpperCase()}
                      </Link>
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          order.order_status === "DELIVERED"
                            ? "bg-green-100 text-green-800"
                            : order.order_status === "PLACED"
                            ? "bg-amber-100 text-amber-800"
                            : order.order_status === "CONFIRMED"
                            ? "bg-blue-100 text-blue-800"
                            : "bg-purple-100 text-purple-800"
                        }`}
                      >
                        {order.order_status}
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-xs pt-1 border-t border-gray-100">
                      <div>
                        <div className="font-semibold text-gray-900">
                          {order.profiles?.name || "Customer"}
                        </div>
                        <div className="text-[11px] text-gray-500">
                          {new Date(order.created_at).toLocaleString([], {
                            month: "short",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </div>
                      </div>

                      <div className="text-right">
                        <div className="font-bold text-sm text-gray-900 font-mono">
                          ₹{Number(order.total).toFixed(2)}
                        </div>
                        <Link
                          href={`/orders/${order.id}`}
                          className="text-[11px] font-semibold text-emerald-700 hover:underline inline-flex items-center gap-0.5 mt-0.5"
                        >
                          Inspect <ArrowRight className="h-3 w-3" />
                        </Link>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* DESKTOP VIEW: Dense Recent Orders Table */}
              <div className="hidden md:block overflow-x-auto">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b text-gray-500 text-left">
                      <th className="pb-2 font-medium">Order ID</th>
                      <th className="pb-2 font-medium">Customer</th>
                      <th className="pb-2 font-medium">Date & Time</th>
                      <th className="pb-2 font-medium">Amount</th>
                      <th className="pb-2 font-medium">Status</th>
                      <th className="pb-2 font-medium text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    {stats.recentOrders.map((order) => (
                      <tr key={order.id} className="hover:bg-gray-50">
                        <td className="py-2.5 font-mono font-medium text-gray-900">
                          #{order.id.slice(0, 8).toUpperCase()}
                        </td>
                        <td className="py-2.5 text-gray-700">
                          {order.profiles?.name || "Customer"} ({order.profiles?.phone || "N/A"})
                        </td>
                        <td className="py-2.5 text-gray-500">
                          {new Date(order.created_at).toLocaleString()}
                        </td>
                        <td className="py-2.5 font-semibold text-gray-900">
                          ₹{Number(order.total).toFixed(2)}
                        </td>
                        <td className="py-2.5">
                          <span
                            className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                              order.order_status === "DELIVERED"
                                ? "bg-green-100 text-green-800"
                                : order.order_status === "PLACED"
                                ? "bg-amber-100 text-amber-800"
                                : order.order_status === "CONFIRMED"
                                ? "bg-blue-100 text-blue-800"
                                : "bg-purple-100 text-purple-800"
                            }`}
                          >
                            {order.order_status}
                          </span>
                        </td>
                        <td className="py-2.5 text-right">
                          <Button variant="ghost" size="sm" asChild className="h-7 text-xs">
                            <Link href={`/orders/${order.id}`}>Inspect</Link>
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
