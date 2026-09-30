"use server";

import { createClient } from "@/lib/supabase/server";
import { revalidatePath } from "next/cache";

export async function updateOrderStatus(
  orderId: string,
  newStatus: string,
  notes?: string
) {
  const supabase = await createClient();

  // 1. Update order status
  const { error: orderError } = await supabase
    .from("orders")
    .update({
      order_status: newStatus,
      updated_at: new Date().toISOString(),
    })
    .eq("id", orderId);

  if (orderError) {
    throw new Error(orderError.message);
  }

  // 2. Log status history (trigger might already log, but explicit fallback ensures record)
  try {
    await supabase.from("order_status_history").insert([
      {
        order_id: orderId,
        status: newStatus,
        notes: notes || `Admin updated status to ${newStatus}`,
      },
    ]);
  } catch (historyErr) {
    // Ignore if trigger handled it or unique constraint
  }

  revalidatePath(`/orders/${orderId}`);
  revalidatePath("/orders");
  revalidatePath("/");
}

export async function assignDeliveryPartnerAction(
  orderId: string,
  partnerId: string | null
) {
  const supabase = await createClient();

  const { error } = await supabase
    .from("orders")
    .update({
      delivery_partner_id: partnerId || null,
      updated_at: new Date().toISOString(),
    })
    .eq("id", orderId);

  if (error) {
    throw new Error(error.message);
  }

  revalidatePath(`/orders/${orderId}`);
  revalidatePath("/orders");
}
