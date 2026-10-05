"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { User, LogOut, ShieldCheck, Menu } from "lucide-react";
import { Button } from "./ui/button";
import { createClient } from "@/lib/supabase/client";

interface HeaderProps {
  onOpenDrawer?: () => void;
}

export function Header({ onOpenDrawer }: HeaderProps) {
  const router = useRouter();
  const supabase = createClient();
  const [adminUser, setAdminUser] = useState<{ name: string; email: string } | null>(null);
  const [loggingOut, setLoggingOut] = useState(false);

  useEffect(() => {
    async function loadUser() {
      const { data: { user } } = await supabase.auth.getUser();
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
    }
    loadUser();
  }, [supabase]);

  const handleLogout = async () => {
    setLoggingOut(true);
    await supabase.auth.signOut();
    router.push("/login");
    router.refresh();
  };

  return (
    <header className="flex h-14 md:h-16 items-center justify-between border-b bg-white px-3 md:px-6 sticky top-0 z-30 shadow-[0_1px_2px_rgba(0,0,0,0.03)]">
      {/* Left: Hamburger (Mobile) or Production Admin Pill (Desktop) */}
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={onOpenDrawer}
          className="p-2 -ml-1 text-gray-700 hover:text-gray-900 rounded-lg hover:bg-gray-100 active:bg-gray-200 md:hidden flex items-center justify-center min-w-[40px] min-h-[40px]"
          aria-label="Open navigation drawer"
        >
          <Menu className="h-5 w-5" />
        </button>

        {/* Mobile Brand Logo */}
        <div className="flex items-center gap-2 md:hidden">
          <Image
            src="/brand/govind-logo-circle.png"
            alt="Govind Logo"
            width={28}
            height={28}
            className="rounded-full object-contain"
          />
          <span className="font-bold text-sm text-gray-900 tracking-tight">
            Govind Admin
          </span>
        </div>

        {/* Desktop Production Badge */}
        <span className="hidden md:inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200">
          <ShieldCheck className="h-3.5 w-3.5 text-emerald-700" />
          Production Admin
        </span>
      </div>

      {/* Right: User info & Actions */}
      <div className="flex items-center gap-2 sm:gap-4">
        <div className="text-right hidden sm:block">
          <div className="text-sm font-semibold text-gray-800 leading-tight">
            {adminUser?.name || "Admin User"}
          </div>
          <div className="text-xs text-gray-500">
            {adminUser?.email || "admin@govind.com"}
          </div>
        </div>

        <div className="flex items-center gap-1.5 sm:gap-2">
          <div className="h-8 w-8 sm:h-9 sm:w-9 rounded-full bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold text-xs sm:text-sm">
            <User className="h-4 w-4" />
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={handleLogout}
            disabled={loggingOut}
            className="h-8 sm:h-9 px-2 sm:px-3 flex items-center gap-1.5 text-red-600 hover:text-red-700 hover:bg-red-50 border-gray-200 text-xs font-medium"
            title="Log out of Admin Dashboard"
          >
            <LogOut className="h-3.5 w-3.5 sm:h-4 sm:w-4" />
            <span className="hidden sm:inline">{loggingOut ? "Signing out..." : "Logout"}</span>
          </Button>
        </div>
      </div>
    </header>
  );
}
