"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { User, LogOut, ShieldCheck } from "lucide-react";
import { Button } from "./ui/button";
import { createClient } from "@/lib/supabase/client";

export function Header() {
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
    <header className="flex h-16 items-center justify-between border-b bg-white px-6">
      <div className="flex items-center gap-2">
        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-50 text-green-700 border border-green-200">
          <ShieldCheck className="h-3.5 w-3.5" />
          Production Admin
        </span>
      </div>
      <div className="flex items-center gap-4">
        <div className="text-right hidden sm:block">
          <div className="text-sm font-semibold text-gray-800">
            {adminUser?.name || "Admin User"}
          </div>
          <div className="text-xs text-gray-500">
            {adminUser?.email || "admin@govind.com"}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div className="h-9 w-9 rounded-full bg-green-100 text-green-700 flex items-center justify-center font-bold text-sm">
            <User className="h-4 w-4" />
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={handleLogout}
            disabled={loggingOut}
            className="flex items-center gap-1.5 text-red-600 hover:text-red-700 hover:bg-red-50 border-gray-200"
            title="Log out of Admin Dashboard"
          >
            <LogOut className="h-4 w-4" />
            <span className="hidden sm:inline">{loggingOut ? "Signing out..." : "Logout"}</span>
          </Button>
        </div>
      </div>
    </header>
  );
}
