"use client";

import { User } from "lucide-react";
import { Button } from "./ui/button";

export function Header() {
  return (
    <header className="flex h-16 items-center justify-between border-b bg-white px-6">
      <div className="flex items-center gap-4">
        {/* Mobile menu could go here */}
      </div>
      <div className="flex items-center gap-4">
        <div className="text-sm font-medium text-gray-700 hidden sm:block">
          Admin User
        </div>
        <Button variant="ghost" size="icon" className="rounded-full bg-gray-100">
          <User className="h-5 w-5 text-gray-600" />
        </Button>
      </div>
    </header>
  );
}
