"use client";

import { useState, useEffect, useTransition } from "react";
import Link from "next/link";
import { updateOrderStatus, assignDeliveryPartnerAction } from "./actions";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { createClient } from "@/lib/supabase/client";
import {
  ArrowLeft,
  CheckCircle2,
  Clock,
  Truck,
  MapPin,
  User,
  IndianRupee,
  Navigation,
  AlertCircle,
  XCircle,
  Phone,
} from "lucide-react";

const CANONICAL_FLOW = [
  "PLACED",
  "CONFIRMED",
  "PREPARING",
  "READY_FOR_DELIVERY",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
];

export default function OrderDetailsClient({
  order,
  items,
  history,
  deliveryPartners,
}: {
  order: any;
  items: any[];
  history: any[];
  deliveryPartners: any[];
}) {
  const [isPending, startTransition] = useTransition();
  const supabase = createClient();
  const [locationInfo, setLocationInfo] = useState<any | null>(null);

  const currentStatusIndex = CANONICAL_FLOW.indexOf(order.order_status);
  const nextStatus =
    currentStatusIndex !== -1 && currentStatusIndex < CANONICAL_FLOW.length - 1
      ? CANONICAL_FLOW[currentStatusIndex + 1]
      : null;

  const handleAdvanceStatus = (targetStatus?: string) => {
    const toStatus = targetStatus || nextStatus;
    if (toStatus) {
      startTransition(() => {
        updateOrderStatus(order.id, toStatus);
      });
    }
  };

  const handleCancelOrder = () => {
    if (confirm("Are you sure you want to cancel this order? This cannot be undone.")) {
      startTransition(() => {
        updateOrderStatus(order.id, "CANCELLED", "Cancelled by Store Administrator");
      });
    }
  };

  const handleAssignPartner = (partnerId: string) => {
    startTransition(() => {
      assignDeliveryPartnerAction(order.id, partnerId || null);
    });
  };

  // Realtime location subscription for live tracking
  useEffect(() => {
    if (order.order_status === "OUT_FOR_DELIVERY") {
      const fetchLocation = async () => {
        const { data } = await supabase
          .from("delivery_locations")
          .select("*")
          .eq("order_id", order.id)
          .single();

        if (data) {
          setLocationInfo(data);
        }
      };
      fetchLocation();

      const channel = supabase
        .channel(`order_tracking_${order.id}`)
        .on(
          "postgres_changes",
          {
            event: "*",
            schema: "public",
            table: "delivery_locations",
            filter: `order_id=eq.${order.id}`,
          },
          (payload) => {
            setLocationInfo(payload.new);
          }
        )
        .subscribe();

      return () => {
        supabase.removeChannel(channel);
      };
    }
  }, [order.id, order.order_status, supabase]);

  // Group items by experience type
  const itemsByExperience = items.reduce((acc, item) => {
    const exp = item.experience_type || "FRESH";
    if (!acc[exp]) acc[exp] = [];
    acc[exp].push(item);
    return acc;
  }, {} as Record<string, any[]>);

  // Address snapshot parsing
  const addr = order.address_snapshot;
  const formattedAddress = addr
    ? typeof addr === "string"
      ? addr
      : [
          addr.house_number || addr.flat,
          addr.street || addr.street_name,
          addr.landmark,
          addr.area,
          addr.city,
          addr.pincode,
        ]
          .filter(Boolean)
          .join(", ")
    : order.customer?.address || "Address not provided";

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header with Navigation & Next Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b pb-4">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" asChild className="h-9 w-9 p-0">
            <Link href="/orders">
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-2xl font-bold tracking-tight text-gray-900">
                Order #{order.id.slice(0, 8).toUpperCase()}
              </h2>
              <span
                className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                  order.order_status === "DELIVERED"
                    ? "bg-green-100 text-green-800"
                    : order.order_status === "CANCELLED"
                    ? "bg-red-100 text-red-800"
                    : order.order_status === "PLACED"
                    ? "bg-amber-100 text-amber-800"
                    : "bg-blue-100 text-blue-800"
                }`}
              >
                {order.order_status}
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-0.5">
              Placed on {new Date(order.created_at).toLocaleString()}
            </p>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 w-full sm:w-auto">
          {order.order_status !== "DELIVERED" && order.order_status !== "CANCELLED" && (
            <>
              {nextStatus && (
                <Button
                  onClick={() => handleAdvanceStatus()}
                  disabled={isPending}
                  className="min-h-[42px] sm:min-h-0 bg-green-700 hover:bg-green-800 text-white font-semibold text-xs md:text-sm"
                >
                  {isPending ? "Transitioning..." : `Advance to ${nextStatus.replace(/_/g, " ")}`}
                </Button>
              )}
              <Button
                variant="outline"
                onClick={handleCancelOrder}
                disabled={isPending}
                className="min-h-[42px] sm:min-h-0 text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200 text-xs md:text-sm"
              >
                Cancel Order
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Status Progress Lifecycle */}
      <Card className="shadow-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-semibold text-gray-700">Canonical Order Lifecycle</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 sm:grid-cols-6 gap-2">
            {CANONICAL_FLOW.map((step, idx) => {
              const isPast = currentStatusIndex > idx;
              const isCurrent = currentStatusIndex === idx;
              const historyEntry = history.find((h) => h.status === step);

              return (
                <div
                  key={step}
                  className={`p-2.5 rounded-lg border text-center transition-all ${
                    isCurrent
                      ? "border-green-600 bg-green-50 shadow-sm"
                      : isPast
                      ? "border-gray-200 bg-gray-50 text-gray-600"
                      : "border-gray-100 bg-white text-gray-400"
                  }`}
                >
                  <div className="flex items-center justify-center mb-1">
                    {isPast ? (
                      <CheckCircle2 className="h-4 w-4 text-green-600" />
                    ) : isCurrent ? (
                      <Clock className="h-4 w-4 text-green-700 animate-pulse" />
                    ) : (
                      <div className="h-4 w-4 rounded-full border border-gray-300" />
                    )}
                  </div>
                  <div
                    className={`text-[11px] font-bold ${
                      isCurrent ? "text-green-900" : isPast ? "text-gray-800" : "text-gray-400"
                    }`}
                  >
                    {step.replace(/_/g, " ")}
                  </div>
                  {historyEntry && (
                    <div className="text-[10px] text-gray-500 mt-1">
                      {new Date(historyEntry.created_at).toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* Details Grid: Customer & Delivery */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-semibold flex items-center gap-2">
              <User className="h-4 w-4 text-gray-500" /> Customer & Delivery Details
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            <div>
              <span className="text-gray-500 block">Customer Name</span>
              <span className="font-semibold text-gray-900">
                {order.customer?.name || order.customer?.full_name || "Guest Customer"}
              </span>
            </div>
            <div>
              <span className="text-gray-500 block">Phone Contact</span>
              {order.customer?.phone ? (
                <a
                  href={`tel:${order.customer.phone}`}
                  className="font-semibold text-emerald-700 hover:underline inline-flex items-center gap-1 mt-0.5"
                >
                  <Phone className="h-3.5 w-3.5" />
                  {order.customer.phone}
                </a>
              ) : (
                <span className="font-semibold text-gray-900">No phone provided</span>
              )}
            </div>
            <div>
              <span className="text-gray-500 block">Delivery Address (Snapshot)</span>
              <span className="font-medium text-gray-800 flex items-start gap-1 mt-0.5">
                <MapPin className="h-3.5 w-3.5 text-gray-400 mt-0.5 shrink-0" />
                {formattedAddress}
              </span>
            </div>
            <div>
              <span className="text-gray-500 block">Payment Method & Status</span>
              <span className="font-semibold text-gray-900">
                {order.payment_method || "COD"} • {order.payment_status || "PENDING"}
              </span>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-semibold flex items-center gap-2">
              <Truck className="h-4 w-4 text-gray-500" /> Delivery Partner Dispatch
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            <div>
              <label className="text-gray-500 block mb-1">Assigned Partner</label>
              <select
                className="w-full border rounded-md px-3 py-1.5 text-xs bg-white font-medium"
                value={order.delivery_partner_id || ""}
                disabled={isPending || order.order_status === "DELIVERED"}
                onChange={(e) => handleAssignPartner(e.target.value)}
              >
                <option value="">-- No Partner Assigned --</option>
                {deliveryPartners.map((dp) => (
                  <option key={dp.id} value={dp.id}>
                    {dp.name || dp.full_name || "Partner"} ({dp.phone || "No Phone"})
                  </option>
                ))}
              </select>
            </div>

            {/* Live Tracking Information Card */}
            {order.order_status === "OUT_FOR_DELIVERY" && (
              <div className="p-3 bg-cyan-50 border border-cyan-200 rounded-lg text-cyan-900 space-y-1.5 mt-2">
                <div className="font-bold flex items-center gap-1.5 text-xs text-cyan-950">
                  <Navigation className="h-3.5 w-3.5 text-cyan-700 animate-spin" />
                  Live Delivery Tracking Active
                </div>
                {locationInfo ? (
                  <div className="text-[11px] space-y-1 pt-1">
                    <div>
                      <strong>Estimated Arrival:</strong>{" "}
                      {locationInfo.estimated_time_sec > 0
                        ? `${Math.ceil(locationInfo.estimated_time_sec / 60)} mins`
                        : "In transit"}
                    </div>
                    <div>
                      <strong>Distance to Destination:</strong>{" "}
                      {locationInfo.estimated_distance_m > 0
                        ? `${(locationInfo.estimated_distance_m / 1000).toFixed(1)} km`
                        : "Approaching"}
                    </div>
                    <div className="text-gray-500 text-[10px]">
                      Last GPS Ping: {new Date(locationInfo.updated_at).toLocaleTimeString()}
                    </div>
                  </div>
                ) : (
                  <div className="text-[11px] text-cyan-800">
                    Awaiting GPS coordinates from delivery partner device...
                  </div>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Items Breakdown Grouped by Experience */}
      <Card className="shadow-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-base font-semibold">Ordered Items & Pricing Snapshot</CardTitle>
          <CardDescription className="text-xs">
            Historical prices preserved at the exact moment of order checkout
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {(Object.entries(itemsByExperience) as [string, any[]][]).map(([experience, expItems]) => (
            <div key={experience} className="space-y-2">
              <div className="flex items-center gap-2">
                <span
                  className={`px-2 py-0.5 rounded text-[11px] font-bold ${
                    experience === "KITCHEN"
                      ? "bg-orange-100 text-orange-800"
                      : experience === "WHOLESALE"
                      ? "bg-blue-100 text-blue-800"
                      : "bg-emerald-100 text-emerald-800"
                  }`}
                >
                  {experience} Experience
                </span>
                <span className="text-xs text-gray-500 font-medium">({expItems.length} items)</span>
              </div>

              {/* MOBILE VIEW: Ordered Items Cards */}
              <div className="block md:hidden space-y-2">
                {expItems.map((item) => {
                  const unitPrice =
                    Number(item.effective_unit_price) || Number(item.price) || 0;
                  const lineTotal =
                    Number(item.line_total) || item.quantity * unitPrice;
                  const discount = Number(item.discount || item.bulk_discount || 0);

                  return (
                    <div
                      key={item.id}
                      className="p-3 bg-white rounded-lg border border-gray-200 text-xs space-y-1.5 shadow-sm"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <span className="font-semibold text-gray-900 text-sm">
                          {item.product_name}
                        </span>
                        <span className="font-mono font-bold text-sm text-gray-900">
                          ₹{lineTotal.toFixed(2)}
                        </span>
                      </div>

                      <div className="flex items-center justify-between text-gray-500 pt-1 border-t border-gray-100">
                        <span>
                          Qty: <strong className="text-gray-800">{item.quantity} {item.unit || ""}</strong>
                        </span>
                        <span>
                          Rate: <strong className="font-mono text-gray-800">₹{unitPrice.toFixed(2)}</strong>
                        </span>
                      </div>

                      {discount > 0 && (
                        <div className="text-[11px] text-green-700 font-medium">
                          Discount: ₹{discount.toFixed(2)} / unit
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              {/* DESKTOP VIEW: Ordered Items Table */}
              <div className="hidden md:block border rounded-md overflow-hidden">
                <Table>
                  <TableHeader>
                    <TableRow className="bg-gray-50 text-xs">
                      <TableHead>Item Name</TableHead>
                      <TableHead>Quantity</TableHead>
                      <TableHead>Historical Unit Price</TableHead>
                      <TableHead>Discount / Savings</TableHead>
                      <TableHead className="text-right">Line Total</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {expItems.map((item) => {
                      const unitPrice =
                        Number(item.effective_unit_price) || Number(item.price) || 0;
                      const lineTotal =
                        Number(item.line_total) || item.quantity * unitPrice;
                      const discount = Number(item.discount || item.bulk_discount || 0);

                      return (
                        <TableRow key={item.id} className="text-xs">
                          <TableCell className="font-semibold text-gray-900">
                            {item.product_name}
                          </TableCell>
                          <TableCell className="text-gray-700">
                            {item.quantity} {item.unit || ""}
                          </TableCell>
                          <TableCell className="font-mono text-gray-800">
                            ₹{unitPrice.toFixed(2)}
                          </TableCell>
                          <TableCell className="text-green-700 font-medium">
                            {discount > 0 ? `₹${discount.toFixed(2)} / unit` : "—"}
                          </TableCell>
                          <TableCell className="text-right font-bold text-gray-900 font-mono">
                            ₹{lineTotal.toFixed(2)}
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </div>
            </div>
          ))}

          {/* Pricing Summary Breakdown */}
          <div className="border-t pt-4 flex justify-end">
            <div className="w-full sm:w-64 space-y-1.5 text-xs">
              <div className="flex justify-between text-gray-600">
                <span>Subtotal:</span>
                <span className="font-mono font-medium">₹{Number(order.subtotal).toFixed(2)}</span>
              </div>
              {Number(order.discount) > 0 && (
                <div className="flex justify-between text-green-700">
                  <span>Coupon Discount:</span>
                  <span className="font-mono font-medium">-₹{Number(order.discount).toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between text-gray-600">
                <span>Delivery Charge:</span>
                <span className="font-mono font-medium">
                  {Number(order.delivery_charge) === 0 ? "FREE" : `₹${Number(order.delivery_charge).toFixed(2)}`}
                </span>
              </div>
              <div className="border-t pt-2 flex justify-between font-bold text-sm text-gray-900">
                <span>Total Amount:</span>
                <span className="font-mono text-green-700 text-base">₹{Number(order.total).toFixed(2)}</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
