"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Truck, RefreshCw, Navigation, Clock, CheckCircle2, Phone } from "lucide-react";

export default function DeliveriesPage() {
  const supabase = createClient();
  const [deliveries, setDeliveries] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDeliveries();

    // Subscribe to realtime location changes
    const channel = supabase
      .channel("delivery_locations_feed")
      .on(
        "postgres_changes",
        {
          event: "*",
          schema: "public",
          table: "delivery_locations",
        },
        () => {
          fetchDeliveries();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [supabase]);

  async function fetchDeliveries() {
    setLoading(true);
    // 1. Fetch active delivery orders
    const { data: orders, error } = await supabase
      .from("orders")
      .select(`
        *,
        partner:profiles!orders_delivery_partner_id_fkey(name, full_name, phone),
        customer:profiles!orders_user_id_fkey(name, full_name, phone)
      `)
      .in("order_status", ["READY_FOR_DELIVERY", "OUT_FOR_DELIVERY"])
      .order("created_at", { ascending: false });

    if (!error && orders) {
      // 2. Fetch locations for these orders
      const orderIds = orders.map((o) => o.id);
      const { data: locations } = await supabase
        .from("delivery_locations")
        .select("*")
        .in("order_id", orderIds);

      const locMap: Record<string, any> = {};
      if (locations) {
        locations.forEach((l) => {
          locMap[l.order_id] = l;
        });
      }

      const merged = orders.map((o) => ({
        ...o,
        location: locMap[o.id] || null,
      }));
      setDeliveries(merged);
    }
    setLoading(false);
  }

  const getFreshness = (timestamp?: string) => {
    if (!timestamp) return "No GPS signal";
    const diff = Math.floor((Date.now() - new Date(timestamp).getTime()) / 1000);
    if (diff < 60) return `${diff}s ago`;
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    return `${Math.floor(diff / 3600)}h ago`;
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl md:text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <Truck className="h-6 w-6 md:h-7 md:w-7 text-cyan-600" />
            Live Delivery Operations
          </h2>
          <p className="text-xs md:text-sm text-gray-500">
            Real-time delivery partner tracking, GPS pings, distance and ETA calculations
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={fetchDeliveries}
          disabled={loading}
          className="self-start sm:self-auto h-9 text-xs font-semibold"
        >
          <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
          Refresh Deliveries
        </Button>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3 px-3.5 sm:px-6">
          <CardTitle className="text-base font-semibold">
            Active Deliveries in Transit ({deliveries.length})
          </CardTitle>
          <CardDescription className="text-xs">
            Orders currently in <code>READY_FOR_DELIVERY</code> or <code>OUT_FOR_DELIVERY</code> status
          </CardDescription>
        </CardHeader>
        <CardContent className="px-3.5 sm:px-6">
          {loading ? (
            <div className="py-12 text-center text-sm text-gray-500">Loading delivery tracking feed...</div>
          ) : deliveries.length === 0 ? (
            <div className="py-12 text-center text-gray-500 space-y-2">
              <CheckCircle2 className="h-10 w-10 text-gray-300 mx-auto" />
              <div className="text-sm font-semibold text-gray-700">No active deliveries in transit</div>
              <p className="text-xs text-gray-400 max-w-sm mx-auto">
                When customer orders are marked as Ready or Out for Delivery and assigned to a delivery partner, they will appear here in real-time.
              </p>
              <Button asChild size="sm" variant="outline" className="mt-2 text-xs">
                <Link href="/orders">Go to Orders</Link>
              </Button>
            </div>
          ) : (
            <>
              {/* MOBILE VIEW: Touch-Friendly Delivery Cards */}
              <div className="block md:hidden space-y-3">
                {deliveries.map((d) => {
                  const loc = d.location;
                  const partnerName = d.partner?.name || d.partner?.full_name || "Unassigned";
                  const partnerPhone = d.partner?.phone;
                  const custName = d.customer?.name || d.customer?.full_name || "Customer";
                  const custPhone = d.customer?.phone;

                  const etaText = loc
                    ? loc.estimated_time_sec > 0
                      ? `${Math.ceil(loc.estimated_time_sec / 60)} mins`
                      : "Arriving"
                    : "Awaiting GPS";

                  const distanceText = loc
                    ? loc.estimated_distance_m > 0
                      ? `${(loc.estimated_distance_m / 1000).toFixed(1)} km`
                      : "Approaching"
                    : "—";

                  return (
                    <div
                      key={d.id}
                      className="p-3.5 bg-white rounded-xl border border-gray-200 shadow-sm space-y-3"
                    >
                      {/* Top Header: Order ID + Status Badge */}
                      <div className="flex items-center justify-between">
                        <Link
                          href={`/orders/${d.id}`}
                          className="font-mono font-bold text-xs text-emerald-700 hover:underline"
                        >
                          #{d.id.slice(0, 8).toUpperCase()}
                        </Link>
                        <span
                          className={`px-2.5 py-0.5 rounded text-[10px] font-bold ${
                            d.order_status === "OUT_FOR_DELIVERY"
                              ? "bg-cyan-100 text-cyan-800"
                              : "bg-indigo-100 text-indigo-800"
                          }`}
                        >
                          {d.order_status}
                        </span>
                      </div>

                      {/* Live Metrics: ETA & Distance */}
                      <div className="grid grid-cols-2 gap-2 p-2.5 bg-cyan-50/60 rounded-lg border border-cyan-100 text-xs">
                        <div>
                          <div className="text-[10px] text-cyan-700 uppercase font-semibold">ETA</div>
                          <div className="font-bold text-cyan-950 mt-0.5 flex items-center gap-1">
                            <Clock className="h-3.5 w-3.5 text-cyan-700" />
                            {etaText}
                          </div>
                        </div>
                        <div>
                          <div className="text-[10px] text-cyan-700 uppercase font-semibold">Distance</div>
                          <div className="font-bold text-cyan-950 mt-0.5 flex items-center gap-1">
                            <Navigation className="h-3.5 w-3.5 text-cyan-700" />
                            {distanceText}
                          </div>
                        </div>
                      </div>

                      {/* Assigned Rider & Customer Info */}
                      <div className="space-y-2 text-xs pt-1">
                        {/* Rider */}
                        <div className="flex items-center justify-between">
                          <div>
                            <div className="text-[10px] text-gray-400 uppercase font-semibold">Rider</div>
                            <div className="font-semibold text-gray-900">{partnerName}</div>
                          </div>
                          {partnerPhone ? (
                            <a
                              href={`tel:${partnerPhone}`}
                              className="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-emerald-50 text-emerald-700 text-xs font-semibold active:bg-emerald-100"
                            >
                              <Phone className="h-3.5 w-3.5" /> Call Rider
                            </a>
                          ) : (
                            <span className="text-[11px] text-gray-400">No Phone</span>
                          )}
                        </div>

                        {/* Customer */}
                        <div className="flex items-center justify-between border-t border-gray-100 pt-2">
                          <div>
                            <div className="text-[10px] text-gray-400 uppercase font-semibold">Customer</div>
                            <div className="font-semibold text-gray-900">{custName}</div>
                          </div>
                          {custPhone ? (
                            <a
                              href={`tel:${custPhone}`}
                              className="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-gray-100 text-gray-700 text-xs font-semibold active:bg-gray-200"
                            >
                              <Phone className="h-3.5 w-3.5" /> Call Customer
                            </a>
                          ) : (
                            <span className="text-[11px] text-gray-400">No Phone</span>
                          )}
                        </div>
                      </div>

                      {/* GPS ping footer & Track Button */}
                      <div className="pt-2 border-t border-gray-100 flex items-center justify-between gap-2">
                        <div className="text-[10px] text-gray-400">
                          GPS: {getFreshness(loc?.updated_at || loc?.created_at)}
                        </div>
                        <Button asChild size="sm" className="h-9 px-3 text-xs bg-emerald-700 hover:bg-emerald-800 text-white font-semibold">
                          <Link href={`/orders/${d.id}`} className="flex items-center gap-1">
                            Track Order
                          </Link>
                        </Button>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* DESKTOP VIEW: Operational Table */}
              <div className="hidden md:block overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Order</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Assigned Partner</TableHead>
                      <TableHead>Customer</TableHead>
                      <TableHead>Last Coordinates</TableHead>
                      <TableHead>Freshness</TableHead>
                      <TableHead>Distance</TableHead>
                      <TableHead>ETA</TableHead>
                      <TableHead className="text-right">Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {deliveries.map((d) => {
                      const loc = d.location;
                      const partnerName = d.partner?.name || d.partner?.full_name || "Unassigned";
                      const custName = d.customer?.name || d.customer?.full_name || "Customer";

                      const etaText = loc
                        ? loc.estimated_time_sec > 0
                          ? `${Math.ceil(loc.estimated_time_sec / 60)} mins`
                          : "Arriving"
                        : "Awaiting GPS";

                      const distanceText = loc
                        ? loc.estimated_distance_m > 0
                          ? `${(loc.estimated_distance_m / 1000).toFixed(1)} km`
                          : "Approaching"
                        : "—";

                      return (
                        <TableRow key={d.id} className="hover:bg-gray-50/50">
                          <TableCell>
                            <Link
                              href={`/orders/${d.id}`}
                              className="font-mono font-bold text-xs text-green-700 hover:underline"
                            >
                              #{d.id.slice(0, 8).toUpperCase()}
                            </Link>
                          </TableCell>
                          <TableCell>
                            <span
                              className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                                d.order_status === "OUT_FOR_DELIVERY"
                                  ? "bg-cyan-100 text-cyan-800"
                                  : "bg-indigo-100 text-indigo-800"
                              }`}
                            >
                              {d.order_status}
                            </span>
                          </TableCell>
                          <TableCell>
                            <div className="text-xs font-semibold text-gray-900">{partnerName}</div>
                            <div className="text-[10px] text-gray-400">{d.partner?.phone || "No Phone"}</div>
                          </TableCell>
                          <TableCell>
                            <div className="text-xs text-gray-800">{custName}</div>
                            <div className="text-[10px] text-gray-400">{d.customer?.phone || "No Phone"}</div>
                          </TableCell>
                          <TableCell className="font-mono text-xs text-gray-600">
                            {loc && loc.latitude
                              ? `${loc.latitude.toFixed(4)}, ${loc.longitude.toFixed(4)}`
                              : "Waiting GPS"}
                          </TableCell>
                          <TableCell className="text-xs text-gray-500">
                            {getFreshness(loc?.updated_at || loc?.created_at)}
                          </TableCell>
                          <TableCell className="text-xs font-semibold text-gray-800">
                            {distanceText}
                          </TableCell>
                          <TableCell>
                            <span className="text-xs font-bold text-cyan-800 bg-cyan-50 px-2 py-0.5 rounded border border-cyan-200">
                              {etaText}
                            </span>
                          </TableCell>
                          <TableCell className="text-right">
                            <Button variant="ghost" size="sm" asChild className="h-7 text-xs font-semibold text-green-700">
                              <Link href={`/orders/${d.id}`}>Track</Link>
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
