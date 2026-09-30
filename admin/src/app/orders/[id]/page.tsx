import { notFound } from "next/navigation";
import { createClient } from "@/lib/supabase/server";
import OrderDetailsClient from "./OrderDetailsClient";

export default async function OrderDetailsPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();

  // 1. Fetch Order with Customer & Delivery Partner profiles
  const { data: order, error: orderError } = await supabase
    .from("orders")
    .select(`
      *,
      customer:profiles!orders_user_id_fkey(id, name, full_name, phone, email, address),
      delivery_partner:profiles!orders_delivery_partner_id_fkey(id, name, full_name, phone)
    `)
    .eq("id", id)
    .single();

  if (orderError || !order) {
    notFound();
  }

  // 2. Fetch Order Items
  const { data: items } = await supabase
    .from("order_items")
    .select("*")
    .eq("order_id", id);

  // 3. Fetch Status History
  const { data: history } = await supabase
    .from("order_status_history")
    .select("*")
    .eq("order_id", id)
    .order("created_at", { ascending: true });

  // 4. Fetch Delivery Partner candidates for assignment
  const { data: deliveryPartners } = await supabase
    .from("profiles")
    .select("id, name, full_name, phone")
    .in("role", ["delivery", "DELIVERY_PARTNER"]);

  return (
    <div className="space-y-6">
      <OrderDetailsClient
        order={order}
        items={items || []}
        history={history || []}
        deliveryPartners={deliveryPartners || []}
      />
    </div>
  );
}
