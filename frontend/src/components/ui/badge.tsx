// @ts-nocheck
"use client";

import type { HTMLAttributes } from "react";
import React from "react";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-cyan-400/70 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950",
  {
    variants: {
      variant: {
        default:
          "border border-cyan-400/40 bg-cyan-500/15 text-cyan-200 hover:bg-cyan-500/25",
        secondary:
          "border border-purple-400/40 bg-purple-500/15 text-purple-100 hover:bg-purple-500/25",
        destructive:
          "border border-rose-500/30 bg-rose-500/15 text-rose-100 hover:bg-rose-500/30",
        outline: "border border-slate-700 text-slate-200",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

type BadgeProps = HTMLAttributes<HTMLDivElement> &
  VariantProps<typeof badgeVariants>;

function Badge({ className, variant = "default", ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  );
}

export { Badge, badgeVariants };
