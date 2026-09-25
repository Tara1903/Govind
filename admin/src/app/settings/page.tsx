"use client";

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export default function SettingsPage() {
  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Settings</h2>
        <p className="text-gray-500 mt-2">Manage app configuration and delivery settings.</p>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Delivery Settings</CardTitle>
          <CardDescription>Configure basic delivery rules and charges.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-2">
            <label className="text-sm font-medium">Minimum Order Amount (₹)</label>
            <input type="number" className="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" defaultValue={0} />
          </div>
          <div className="grid gap-2">
            <label className="text-sm font-medium">Base Delivery Charge (₹)</label>
            <input type="number" className="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" defaultValue={40} />
          </div>
          <div className="grid gap-2">
            <label className="text-sm font-medium">Free Delivery Threshold (₹)</label>
            <input type="number" className="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" defaultValue={500} />
          </div>
          <Button className="mt-4">Save Changes</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Serviceable Pincodes</CardTitle>
          <CardDescription>Manage the areas where delivery is active.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-2">
            <label className="text-sm font-medium">Pincodes (Comma separated)</label>
            <textarea className="flex min-h-[80px] w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent" defaultValue="110001, 110002, 110003" />
          </div>
          <Button>Update Pincodes</Button>
        </CardContent>
      </Card>
    </div>
  );
}
