"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  ShoppingCart,
  Truck,
  Store,
  Menu,
} from "lucide-react";

interface MobileBottomNavProps {
  onOpenDrawer: () => void;
  pendingOrdersCount?: number;
}

export function MobileBottomNav({
  onOpenDrawer,
  pendingOrdersCount = 0,
}: MobileBottomNavProps) {
  const pathname = usePathname();

  const navItems = [
    {
      name: "Dashboard",
      href: "/",
      icon: LayoutDashboard,
      badge: 0,
    },
    {
      name: "Orders",
      href: "/orders",
      icon: ShoppingCart,
      badge: pendingOrdersCount,
    },
    {
      name: "Deliveries",
      href: "/deliveries",
      icon: Truck,
      badge: 0,
    },
    {
      name: "Products",
      href: "/products",
      icon: Store,
      badge: 0,
    },
  ];

  return (
    <nav
      className="fixed bottom-0 inset-x-0 z-40 bg-white/95 backdrop-blur-md border-t border-gray-200 md:hidden pb-[max(0.5rem,env(safe-area-inset-bottom))] pt-1 px-2 shadow-lg"
      aria-label="Mobile Bottom Navigation"
    >
      <div className="flex items-center justify-around">
        {navItems.map((item) => {
          const isActive =
            item.href === "/"
              ? pathname === "/"
              : pathname.startsWith(item.href);
          const Icon = item.icon;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex flex-col items-center justify-center flex-1 py-1.5 px-1 relative transition-transform active:scale-95",
                isActive ? "text-emerald-700 font-bold" : "text-gray-500 font-medium"
              )}
            >
              <div className="relative">
                <Icon className={cn("h-5 w-5 transition-colors", isActive ? "text-emerald-700" : "text-gray-500")} />
                {item.badge > 0 && (
                  <span className="absolute -top-1.5 -right-2 bg-red-600 text-white rounded-full text-[9px] font-bold h-4 min-w-[16px] px-1 flex items-center justify-center border-2 border-white">
                    {item.badge > 99 ? "99+" : item.badge}
                  </span>
                )}
              </div>
              <span className="text-[10px] tracking-tight mt-1 leading-none">
                {item.name}
              </span>
              {isActive && (
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-700 mt-1" />
              )}
            </Link>
          );
        })}

        {/* 5th Tab: More Button to open full drawer */}
        <button
          type="button"
          onClick={onOpenDrawer}
          className="flex flex-col items-center justify-center flex-1 py-1.5 px-1 text-gray-500 hover:text-gray-900 active:scale-95 transition-transform"
          aria-label="More navigation items"
        >
          <Menu className="h-5 w-5 text-gray-500" />
          <span className="text-[10px] font-medium tracking-tight mt-1 leading-none">
            More
          </span>
        </button>
      </div>
    </nav>
  );
}
