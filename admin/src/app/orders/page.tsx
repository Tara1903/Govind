"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Search, RefreshCw, ShoppingCart, Truck, Phone, ChevronRight } from "lucide-react";

const CANONICAL_STATUSES = [
  "PLACED",
  "CONFIRMED",
  "PREPARING",
  "READY_FOR_DELIVERY",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "CANCELLED",
];

export default function OrdersPage() {
  const supabase = createClient();
  const [orders, setOrders] = useState<any[]>([]);
  const [deliveryPartners, setDeliveryPartners] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  async function fetchData() {
    setLoading(true);
    const [{ data: partners }, { data: orderList, error }] = await Promise.all([
      supabase
        .from("profiles")
        .select("id, name, full_name, phone")
        .in("role", ["delivery", "DELIVERY_PARTNER"]),
      supabase
        .from("orders")
        .select(`
          *,
          customer:profiles!orders_user_id_fkey(name, full_name, phone),
          delivery_partner:profiles!orders_delivery_partner_id_fkey(name, full_name, phone)
        `)
        .order("created_at", { ascending: false }),
    ]);

    if (partners) setDeliveryPartners(partners);
    if (!error && orderList) setOrders(orderList);
    setLoading(false);
  }

  const updateStatus = async (orderId: string, newStatus: string) => {
    setUpdatingId(orderId);
    setOrders((prev) =>
      prev.map((o) => (o.id === orderId ? { ...o, order_status: newStatus } : o))
    );

    const { error } = await supabase
      .from("orders")
      .update({ order_status: newStatus, updated_at: new Date().toISOString() })
      .eq("id", orderId);

    if (error) {
      alert("Error updating order status: " + error.message);
      fetchData();
    }
    setUpdatingId(null);
  };

  const assignDeliveryPartner = async (orderId: string, partnerId: string) => {
    setUpdatingId(orderId);
    const assignedId = partnerId || null;

    setOrders((prev) =>
      prev.map((o) =>
        o.id === orderId ? { ...o, delivery_partner_id: assignedId } : o
      )
    );

    const { error } = await supabase
      .from("orders")
      .update({ delivery_partner_id: assignedId, updated_at: new Date().toISOString() })
      .eq("id", orderId);

    if (error) {
      alert("Error assigning delivery partner: " + error.message);
      fetchData();
    }
    setUpdatingId(null);
  };

  const filteredOrders = orders.filter((order) => {
    const q = searchQuery.toLowerCase();
    const orderId = order.id.toLowerCase();
    const custName = (order.customer?.name || order.customer?.full_name || "").toLowerCase();
    const custPhone = order.customer?.phone || "";

    const matchesSearch = orderId.includes(q) || custName.includes(q) || custPhone.includes(q);
    const matchesStatus = statusFilter === "ALL" || order.order_status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case "DELIVERED":
        return "bg-green-100 text-green-800 border-green-300";
      case "PLACED":
        return "bg-amber-100 text-amber-800 border-amber-300";
      case "CONFIRMED":
        return "bg-blue-100 text-blue-800 border-blue-300";
      case "PREPARING":
        return "bg-purple-100 text-purple-800 border-purple-300";
      case "READY_FOR_DELIVERY":
        return "bg-indigo-100 text-indigo-800 border-indigo-300";
      case "OUT_FOR_DELIVERY":
        return "bg-cyan-100 text-cyan-800 border-cyan-300";
      case "CANCELLED":
        return "bg-red-100 text-red-800 border-red-300";
      default:
        return "bg-gray-100 text-gray-800 border-gray-300";
    }
  };

  return (
    <div className="space-y-4 md:space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl md:text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <ShoppingCart className="h-6 w-6 md:h-7 md:w-7 text-emerald-700" />
            Orders Management
          </h2>
          <p className="text-xs md:text-sm text-gray-500">
            Real-time pipeline, status lifecycle transitions, and rider dispatch
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={fetchData}
          disabled={loading}
          className="self-start sm:self-auto h-9 text-xs font-semibold"
        >
          <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
          Refresh Orders
        </Button>
      </div>

      {/* Status Filter Carousel - Touch Optimized */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-2 text-xs scrollbar-none -mx-1 px-1">
        {["ALL", ...CANONICAL_STATUSES].map((st) => {
          const count = st === "ALL" ? orders.length : orders.filter((o) => o.order_status === st).length;
          return (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`px-3 py-1.5 rounded-full font-semibold whitespace-nowrap transition-all border text-xs min-h-[36px] flex items-center gap-1.5 ${
                statusFilter === st
                  ? "bg-emerald-700 text-white border-emerald-700 shadow-sm"
                  : "bg-white text-gray-700 border-gray-200 hover:bg-gray-50 active:bg-gray-100"
              }`}
            >
              <span>{st.replace(/_/g, " ")}</span>
              <span
                className={`px-1.5 py-0.2 rounded-full text-[10px] ${
                  statusFilter === st ? "bg-white/20 text-white" : "bg-gray-100 text-gray-600"
                }`}
              >
                {count}
              </span>
            </button>
          );
        })}
      </div>

      <Card className="shadow-sm border-gray-200">
        <CardHeader className="pb-3 px-3.5 sm:px-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <CardTitle className="text-base font-semibold">
                Customer Orders ({filteredOrders.length})
              </CardTitle>
              <CardDescription className="text-xs">
                Showing live orders matching current search and status filters
              </CardDescription>
            </div>
            {/* Search Input */}
            <div className="relative w-full sm:w-64">
              <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search order ID, name, phone..."
                className="pl-9 pr-3 py-2 text-xs md:text-sm border rounded-lg w-full bg-white focus:outline-none focus:ring-2 focus:ring-emerald-600"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>

        <CardContent className="px-3.5 sm:px-6">
          {loading ? (
            <div className="py-12 text-center text-sm text-gray-500">Loading orders...</div>
          ) : filteredOrders.length === 0 ? (
            <div className="py-12 text-center text-sm text-gray-500">
              No orders match the current criteria.
            </div>
          ) : (
            <>
              {/* MOBILE VIEW: Touch-Friendly Responsive Card Feed */}
              <div className="block md:hidden space-y-3">
                {filteredOrders.map((order) => {
                  const custName = order.customer?.name || order.customer?.full_name || "Customer";
                  const custPhone = order.customer?.phone || "";
                  const isSaving = updatingId === order.id;

                  return (
                    <div
                      key={order.id}
                      className="p-3.5 bg-white rounded-xl border border-gray-200 shadow-sm space-y-3"
                    >
                      {/* Top Bar: Order ID, Experience, and Date */}
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Link
                            href={`/orders/${order.id}`}
                            className="font-mono font-bold text-xs text-emerald-700 hover:underline"
                          >
                            #{order.id.slice(0, 8).toUpperCase()}
                          </Link>
                          <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-gray-100 text-gray-700">
                            {order.experience_type || "MIXED"}
                          </span>
                        </div>
                        <span className="text-[11px] text-gray-400">
                          {new Date(order.created_at).toLocaleString([], {
                            month: "short",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </span>
                      </div>

                      {/* Customer Info & Quick Call */}
                      <div className="flex items-center justify-between pt-1 border-t border-gray-100">
                        <div>
                          <div className="font-semibold text-xs text-gray-900">{custName}</div>
                          <div className="text-[11px] text-gray-500">{custPhone || "No Phone"}</div>
                        </div>
                        {custPhone && (
                          <a
                            href={`tel:${custPhone}`}
                            className="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-emerald-50 text-emerald-700 text-xs font-semibold active:bg-emerald-100"
                          >
                            <Phone className="h-3.5 w-3.5" />
                            Call
                          </a>
                        )}
                      </div>

                      {/* Total & Payment Method */}
                      <div className="flex items-center justify-between py-1 bg-gray-50 px-2.5 rounded-lg text-xs">
                        <span className="text-gray-500">Order Total:</span>
                        <div className="text-right">
                          <span className="font-bold text-sm text-gray-900">
                            ₹{Number(order.total).toFixed(2)}
                          </span>
                          <span className="text-[10px] text-gray-400 ml-1.5 font-medium">
                            ({order.payment_method || "COD"} • {order.payment_status || "PENDING"})
                          </span>
                        </div>
                      </div>

                      {/* Lifecycle Status Selector */}
                      <div className="space-y-1">
                        <label className="text-[11px] font-medium text-gray-500">Order Status:</label>
                        <select
                          disabled={isSaving}
                          className={`w-full text-xs font-semibold rounded-lg px-3 py-2 border transition-all cursor-pointer min-h-[40px] ${getStatusColor(
                            order.order_status
                          )}`}
                          value={order.order_status}
                          onChange={(e) => updateStatus(order.id, e.target.value)}
                        >
                          {CANONICAL_STATUSES.map((st) => (
                            <option key={st} value={st}>
                              {st.replace(/_/g, " ")}
                            </option>
                          ))}
                        </select>
                      </div>

                      {/* Delivery Partner Dispatch if Preparing / Ready / Out */}
                      {["PREPARING", "READY_FOR_DELIVERY", "OUT_FOR_DELIVERY"].includes(
                        order.order_status
                      ) && (
                        <div className="space-y-1">
                          <label className="text-[11px] font-medium text-gray-500">
                            Assign Delivery Rider:
                          </label>
                          <select
                            disabled={isSaving}
                            className="w-full text-xs rounded-lg border px-3 py-2 bg-white min-h-[40px]"
                            value={order.delivery_partner_id || ""}
                            onChange={(e) => assignDeliveryPartner(order.id, e.target.value)}
                          >
                            <option value="">-- Assign Rider --</option>
                            {deliveryPartners.map((dp) => (
                              <option key={dp.id} value={dp.id}>
                                {dp.name || dp.full_name || dp.phone}
                              </option>
                            ))}
                          </select>
                        </div>
                      )}

                      {/* View Details Button */}
                      <Button
                        variant="outline"
                        size="sm"
                        asChild
                        className="w-full h-9 text-xs font-bold text-emerald-800 bg-emerald-50/50 hover:bg-emerald-100 border-emerald-200 justify-center"
                      >
                        <Link href={`/orders/${order.id}`} className="flex items-center gap-1.5">
                          View Order Details
                          <ChevronRight className="h-4 w-4" />
                        </Link>
                      </Button>
                    </div>
                  );
                })}
              </div>

              {/* DESKTOP VIEW: Full Wide Table */}
              <div className="hidden md:block overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Order ID</TableHead>
                      <TableHead>Customer</TableHead>
                      <TableHead>Experience</TableHead>
                      <TableHead>Total Amount</TableHead>
                      <TableHead>Order Status</TableHead>
                      <TableHead>Delivery Dispatch</TableHead>
                      <TableHead>Date & Time</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredOrders.map((order) => {
                      const custName = order.customer?.name || order.customer?.full_name || "Customer";
                      const custPhone = order.customer?.phone || "N/A";
                      const isSaving = updatingId === order.id;

                      return (
                        <TableRow key={order.id} className="hover:bg-gray-50/50">
                          <TableCell>
                            <Link
                              href={`/orders/${order.id}`}
                              className="font-mono font-bold text-xs text-emerald-700 hover:underline"
                            >
                              #{order.id.slice(0, 8).toUpperCase()}
                            </Link>
                          </TableCell>
                          <TableCell>
                            <div className="font-semibold text-xs text-gray-900">{custName}</div>
                            <div className="text-[11px] text-gray-500">{custPhone}</div>
                          </TableCell>
                          <TableCell>
                            <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-gray-100 text-gray-800">
                              {order.experience_type || "MIXED"}
                            </span>
                          </TableCell>
                          <TableCell>
                            <div className="font-bold text-xs text-gray-900">
                              ₹{Number(order.total).toFixed(2)}
                            </div>
                            <div className="text-[10px] text-gray-400 font-medium">
                              {order.payment_method || "COD"} • {order.payment_status || "PENDING"}
                            </div>
                          </TableCell>
                          <TableCell>
                            <select
                              disabled={isSaving}
                              className={`text-xs font-semibold rounded-md px-2 py-1 border transition-all cursor-pointer ${getStatusColor(
                                order.order_status
                              )}`}
                              value={order.order_status}
                              onChange={(e) => updateStatus(order.id, e.target.value)}
                            >
                              {CANONICAL_STATUSES.map((st) => (
                                <option key={st} value={st}>
                                  {st.replace(/_/g, " ")}
                                </option>
                              ))}
                            </select>
                          </TableCell>
                          <TableCell>
                            {["PREPARING", "READY_FOR_DELIVERY", "OUT_FOR_DELIVERY"].includes(
                              order.order_status
                            ) ? (
                              <select
                                disabled={isSaving}
                                className="text-xs rounded border px-2 py-1 bg-white max-w-[140px]"
                                value={order.delivery_partner_id || ""}
                                onChange={(e) => assignDeliveryPartner(order.id, e.target.value)}
                              >
                                <option value="">-- Assign Partner --</option>
                                {deliveryPartners.map((dp) => (
                                  <option key={dp.id} value={dp.id}>
                                    {dp.name || dp.full_name || dp.phone}
                                  </option>
                                ))}
                              </select>
                            ) : (
                              <span className="text-xs text-gray-500">
                                {order.delivery_partner?.name ||
                                  order.delivery_partner?.full_name ||
                                  (order.delivery_partner_id ? "Assigned" : "Unassigned")}
                              </span>
                            )}
                          </TableCell>
                          <TableCell className="text-xs text-gray-500 whitespace-nowrap">
                            {new Date(order.created_at).toLocaleString([], {
                              month: "short",
                              day: "numeric",
                              hour: "2-digit",
                              minute: "2-digit",
                            })}
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="sm"
                              asChild
                              className="h-7 text-xs font-semibold text-emerald-700"
                            >
                              <Link href={`/orders/${order.id}`}>View Details</Link>
                            </Button>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
