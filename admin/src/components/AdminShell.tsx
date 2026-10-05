"use client";

import React, { useState, useEffect } from "react";
import { usePathname } from "next/navigation";
import { Sidebar } from "@/components/sidebar";
import { Header } from "@/components/header";
import { MobileDrawer } from "@/components/MobileDrawer";
import { MobileBottomNav } from "@/components/MobileBottomNav";
import { createClient } from "@/lib/supabase/client";

export function AdminShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isAuthPage = pathname === "/login" || pathname === "/unauthorized";

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [adminUser, setAdminUser] = useState<{ name: string; email: string } | null>(null);
  const [pendingOrdersCount, setPendingOrdersCount] = useState<number>(0);

  const supabase = createClient();

  useEffect(() => {
    if (isAuthPage) return;

    async function loadData() {
      // 1. Load user profile
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (user) {
        const { data: profile } = await supabase
          .from("profiles")
          .select("name, full_name, email, role")
          .eq("id", user.id)
          .single();

        setAdminUser({
          name: profile?.name || profile?.full_name || "Admin",
          email: profile?.email || user.email || "",
        });
      }

      // 2. Load pending orders count for badge
      const { count } = await supabase
        .from("orders")
        .select("id", { count: "exact", head: true })
        .in("order_status", ["PLACED", "PREPARING", "READY_FOR_DELIVERY"]);

      if (count !== null) {
        setPendingOrdersCount(count);
      }
    }

    loadData();

    // Subscribe to realtime orders for badge count updates
    const channel = supabase
      .channel("admin_orders_badge")
      .on(
        "postgres_changes",
        {
          event: "*",
          schema: "public",
          table: "orders",
        },
        () => {
          loadData();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [isAuthPage, supabase]);

  if (isAuthPage) {
    return <main className="min-h-screen bg-gray-50">{children}</main>;
  }

  return (
    <div className="flex h-screen overflow-hidden bg-gray-50 antialiased">
      {/* Desktop Sidebar */}
      <Sidebar />

      {/* Mobile Slide-Over Drawer */}
      <MobileDrawer
        isOpen={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        adminUser={adminUser}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-full overflow-hidden relative">
        {/* Top Header */}
        <Header onOpenDrawer={() => setDrawerOpen(true)} />

        {/* Scrollable Viewport with mobile bottom navigation spacing */}
        <main className="flex-1 overflow-y-auto overscroll-contain p-3.5 sm:p-6 pb-24 md:pb-6 bg-gray-50">
          <div className="max-w-7xl mx-auto w-full">{children}</div>
        </main>

        {/* Mobile Sticky Bottom Navigation Bar */}
        <MobileBottomNav
          onOpenDrawer={() => setDrawerOpen(true)}
          pendingOrdersCount={pendingOrdersCount}
        />
      </div>
    </div>
  );
}
