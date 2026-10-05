"use client";

import React, { useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import Image from "next/image";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  ShoppingCart,
  Truck,
  Package,
  Tags,
  Users,
  Ticket,
  Megaphone,
  Settings,
  Menu,
  Bell,
  X,
  LogOut,
  ShieldCheck,
  ChevronRight,
  Store,
} from "lucide-react";
import { createClient } from "@/lib/supabase/client";

interface MobileDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  adminUser?: { name: string; email: string } | null;
}

const navSections = [
  {
    title: "Operations",
    items: [
      { name: "Dashboard", href: "/", icon: LayoutDashboard },
      { name: "Orders", href: "/orders", icon: ShoppingCart },
      { name: "Deliveries", href: "/deliveries", icon: Truck },
      { name: "Inventory", href: "/inventory", icon: Package },
    ],
  },
  {
    title: "Catalogue & Menus",
    items: [
      { name: "Products", href: "/products", icon: Store },
      { name: "Fresh Board", href: "/fresh-board", icon: Package },
      { name: "Daily Rate List", href: "/daily-rates", icon: Tags },
      { name: "Punjabi Menu", href: "/punjabi-menu", icon: Menu },
      { name: "Categories", href: "/categories", icon: Tags },
    ],
  },
  {
    title: "Marketing & Growth",
    items: [
      { name: "Customers", href: "/customers", icon: Users },
      { name: "Coupons", href: "/coupons", icon: Ticket },
      { name: "Promotions", href: "/promotions", icon: Megaphone },
      { name: "Notifications", href: "/notifications", icon: Bell },
    ],
  },
  {
    title: "System",
    items: [{ name: "Settings", href: "/settings", icon: Settings }],
  },
];

export function MobileDrawer({ isOpen, onClose, adminUser }: MobileDrawerProps) {
  const pathname = usePathname();
  const router = useRouter();
  const supabase = createClient();
  const [loggingOut, setLoggingOut] = React.useState(false);

  // Close drawer on path change
  useEffect(() => {
    onClose();
  }, [pathname]);

  // Lock body scroll when drawer is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [isOpen]);

  const handleLogout = async () => {
    setLoggingOut(true);
    await supabase.auth.signOut();
    onClose();
    router.push("/login");
    router.refresh();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 md:hidden flex">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/60 backdrop-blur-sm transition-opacity animate-in fade-in duration-200"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Drawer Content */}
      <div className="relative flex flex-col w-[85%] max-w-xs bg-white h-full shadow-2xl z-10 animate-in slide-in-from-left duration-250 ease-out">
        {/* Header */}
        <div className="p-4 border-b bg-gradient-to-r from-emerald-900 to-emerald-800 text-white flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Image
              src="/brand/govind-logo-circle.png"
              alt="Govind Logo"
              width={36}
              height={36}
              className="rounded-full bg-white p-0.5 object-contain shadow-sm"
            />
            <div>
              <h2 className="text-sm font-bold leading-tight">Govind Admin</h2>
              <p className="text-[11px] text-emerald-200">Fresh & Healthy Food</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-white/10 active:bg-white/20 text-white transition-colors"
            aria-label="Close navigation"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* User Info Card */}
        <div className="p-3.5 bg-emerald-50/70 border-b border-emerald-100 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="h-8 w-8 rounded-full bg-emerald-700 text-white flex items-center justify-center font-bold text-xs shadow-sm">
              {adminUser?.name ? adminUser.name.slice(0, 1).toUpperCase() : "A"}
            </div>
            <div className="truncate max-w-[150px]">
              <div className="text-xs font-bold text-gray-900 truncate">
                {adminUser?.name || "Admin"}
              </div>
              <div className="text-[10px] text-gray-500 truncate">
                {adminUser?.email || "admin@govind.com"}
              </div>
            </div>
          </div>
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">
            <ShieldCheck className="h-3 w-3 text-emerald-700" />
            Live
          </span>
        </div>

        {/* Navigation Sections */}
        <div className="flex-1 overflow-y-auto px-3 py-3 space-y-4 overscroll-contain">
          {navSections.map((section, sIdx) => (
            <div key={sIdx} className="space-y-1">
              <div className="px-2 text-[10px] font-bold tracking-wider uppercase text-gray-400">
                {section.title}
              </div>
              <div className="space-y-0.5">
                {section.items.map((item, iIdx) => {
                  const isActive = pathname === item.href;
                  const Icon = item.icon;
                  return (
                    <Link
                      key={iIdx}
                      href={item.href}
                      onClick={onClose}
                      className={cn(
                        "flex items-center justify-between rounded-lg px-3 py-2.5 text-xs font-semibold transition-all active:scale-[0.98]",
                        isActive
                          ? "bg-emerald-700 text-white shadow-sm"
                          : "text-gray-700 hover:bg-gray-100 active:bg-gray-200"
                      )}
                    >
                      <div className="flex items-center gap-3">
                        <Icon className={cn("h-4 w-4", isActive ? "text-white" : "text-gray-500")} />
                        <span>{item.name}</span>
                      </div>
                      <ChevronRight
                        className={cn(
                          "h-3.5 w-3.5",
                          isActive ? "text-emerald-200" : "text-gray-300"
                        )}
                      />
                    </Link>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        {/* Footer with Logout */}
        <div className="p-3 border-t bg-gray-50 flex items-center justify-between">
          <button
            onClick={handleLogout}
            disabled={loggingOut}
            className="w-full flex items-center justify-center gap-2 py-2.5 px-3 rounded-lg text-xs font-bold text-red-600 bg-red-50 hover:bg-red-100 active:bg-red-200 transition-colors"
          >
            <LogOut className="h-4 w-4" />
            <span>{loggingOut ? "Signing out..." : "Sign Out of Admin"}</span>
          </button>
        </div>
      </div>
    </div>
  );
}
