"use client";

import { SubmissionStatus } from "@/lib/types";
import clsx from "clsx";

type Props = {
  status?: SubmissionStatus;
  isPending: boolean;
};

const statusColorMap: Record<string, string> = {
  PENDING: "bg-slate-800 text-slate-100",
  RUNNING: "bg-amber-500/90 text-slate-950",
  ACCEPTED: "bg-emerald-500 text-emerald-950",
  WRONG_ANSWER: "bg-rose-500 text-rose-950",
  COMPILE_ERROR: "bg-rose-500 text-rose-950",
  RUNTIME_ERROR: "bg-rose-500 text-rose-950",
  INTERNAL_ERROR: "bg-rose-500 text-rose-950"
};

export function SubmissionStatusCard({ status, isPending }: Props) {
  const state = status?.status ?? (isPending ? "PENDING" : "IDLE");
  const pillClasses = statusColorMap[state] ?? "bg-slate-800 text-slate-100";

  return (
    <div className="space-y-3 rounded-xl border border-slate-800 bg-slate-900/60 p-4 text-sm text-slate-200">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold">Latest Submission</h3>
        <span className={clsx("rounded-full px-3 py-1 text-xs font-semibold", pillClasses)}>
          {state.replace("_", " ")}
        </span>
      </div>
      {status?.verdict && <p className="text-sm text-slate-200">{status.verdict}</p>}
      {status?.stdout && (
        <div className="space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wide text-slate-400">Stdout</span>
          <pre className="overflow-auto rounded bg-slate-950/80 p-2 text-xs">{status.stdout}</pre>
        </div>
      )}
      {status?.stderr && status.stderr.trim().length > 0 && (
        <div className="space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wide text-slate-400">Stderr</span>
          <pre className="overflow-auto rounded bg-rose-950/40 p-2 text-xs text-rose-200">
            {status.stderr}
          </pre>
        </div>
      )}
      <div className="grid grid-cols-2 gap-3 text-xs text-slate-400">
        <Detail label="Runtime" value={status?.runtimeMillis ? `${status.runtimeMillis} ms` : "-"} />
        <Detail label="Memory" value={status?.memoryKb ? `${status.memoryKb} KB` : "-"} />
        <Detail label="Submitted" value={status?.submittedAt ? formatTime(status.submittedAt) : "-"} />
        <Detail label="Updated" value={status?.updatedAt ? formatTime(status.updatedAt) : "-"} />
      </div>
    </div>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col">
      <span className="text-xs uppercase tracking-wide">{label}</span>
      <span className="font-semibold text-slate-200">{value}</span>
    </div>
  );
}

function formatTime(timestamp: string) {
  const date = new Date(timestamp);
  return `${date.toLocaleTimeString()}`;
}
