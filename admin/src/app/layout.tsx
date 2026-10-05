import type { Metadata, Viewport } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { AdminShell } from "@/components/AdminShell";

const inter = Inter({ subsets: ["latin"] });

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  viewportFit: "cover",
  themeColor: "#064520",
};

export const metadata: Metadata = {
  title: "Govind - Admin Dashboard",
  description: "Admin panel for Govind Fresh and Healthy Food",
  icons: {
    icon: "/brand/govind-logo-circle.png",
    apple: "/brand/govind-logo-circle.png",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${inter.className} bg-gray-50 text-gray-900 antialiased`}>
        <AdminShell>{children}</AdminShell>
      </body>
    </html>
  );
}
