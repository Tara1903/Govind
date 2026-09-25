"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  ShoppingCart,
  Package,
  Tags,
  Users,
  Ticket,
  Megaphone,
  Settings,
  Menu,
} from "lucide-react";
import { Button } from "./ui/button";

const navItems = [
  { name: "Dashboard", href: "/", icon: LayoutDashboard },
  { name: "Orders", href: "/orders", icon: ShoppingCart },
  { name: "Products", href: "/products", icon: Package },
  { name: "Fresh Board", href: "/fresh-board", icon: Package },
  { name: "Daily Rate List", href: "/daily-rates", icon: Tags },
  { name: "Punjabi Menu", href: "/punjabi-menu", icon: Menu },
  { name: "Categories", href: "/categories", icon: Tags },
  { name: "Inventory", href: "/inventory", icon: Package },
  { name: "Customers", href: "/customers", icon: Users },
  { name: "Coupons", href: "/coupons", icon: Ticket },
  { name: "Promotions", href: "/promotions", icon: Megaphone },
  { name: "Settings", href: "/settings", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <div className="flex h-full flex-col border-r bg-white w-64 hidden md:flex">
      <div className="p-6 border-b">
        <h1 className="text-xl font-bold text-gray-900 flex items-center gap-2">
          <span className="bg-green-600 text-white p-1.5 rounded-md">
            G
          </span>
          Govind Admin
        </h1>
      </div>
      <div className="flex-1 overflow-y-auto py-4">
        <nav className="grid gap-1 px-4">
          {navItems.map((item, index) => {
            const isActive = pathname === item.href;
            const Icon = item.icon;
            return (
              <Link key={index} href={item.href}>
                <span
                  className={cn(
                    "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-all hover:text-gray-900",
                    isActive
                      ? "bg-gray-100 text-gray-900"
                      : "text-gray-500 hover:bg-gray-50"
                  )}
                >
                  <Icon className="h-4 w-4" />
                  {item.name}
                </span>
              </Link>
            );
          })}
        </nav>
      </div>
    </div>
  );
}
