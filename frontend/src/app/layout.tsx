import "@/app/globals.css";
import type { Metadata } from "next";
import { ReactNode } from "react";
import { Toaster } from "@/components/ui/toaster";

export const metadata: Metadata = {
  title: "Shodh-a-Code Contest",
  description: "Real-time contest platform prototype"
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-slate-950 text-slate-100 antialiased">
        {children}
        <Toaster />
      </body>
    </html>
  );
}
