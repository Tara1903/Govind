"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { createClient } from "@/lib/supabase/client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Truck, RefreshCw, Navigation, Clock, CheckCircle2 } from "lucide-react";

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
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center gap-2">
            <Truck className="h-7 w-7 text-cyan-600" />
            Live Delivery Operations
          </h2>
          <p className="text-sm text-gray-500">
            Real-time delivery partner tracking, GPS pings, distance and ETA calculations
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchDeliveries} disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-1.5 ${loading ? "animate-spin" : ""}`} />
          Refresh Deliveries
        </Button>
      </div>

      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <CardTitle className="text-base font-semibold">
            Active Deliveries in Transit ({deliveries.length})
          </CardTitle>
          <CardDescription className="text-xs">
            Orders currently in <code>READY_FOR_DELIVERY</code> or <code>OUT_FOR_DELIVERY</code> status
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-sm text-gray-500">Loading delivery tracking feed...</div>
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
            <div className="overflow-x-auto">
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
          )}
        </CardContent>
      </Card>
    </div>
  );
}
