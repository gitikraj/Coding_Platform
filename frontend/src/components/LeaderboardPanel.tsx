"use client";

import { LeaderboardEntry } from "@/lib/types";

type Props = {
  entries: LeaderboardEntry[];
  currentUser?: string;
};

const formatRelative = (timestamp: number) => {
  if (!timestamp) {
    return "-";
  }
  const diff = Date.now() - timestamp;
  const seconds = Math.floor(diff / 1000);
  if (seconds < 60) {
    return `${seconds}s ago`;
  }
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) {
    return `${minutes}m ago`;
  }
  const hours = Math.floor(minutes / 60);
  return `${hours}h ago`;
};

export function LeaderboardPanel({ entries, currentUser }: Props) {
  return (
    <div className="h-full rounded-2xl border border-slate-800/70 bg-slate-900/70 shadow-xl shadow-slate-950/40 backdrop-blur">
      <div className="flex items-center justify-between border-b border-slate-800/70 px-5 py-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-200">Leaderboard</h2>
        <span className="rounded-full border border-slate-700 bg-slate-950/70 px-3 py-1 text-[10px] font-semibold uppercase tracking-wider text-slate-400">Auto-refreshing</span>
      </div>
      <div className="max-h-[460px] overflow-y-auto">
        <table className="min-w-full divide-y divide-slate-800/60 text-sm">
          <thead className="bg-slate-900/80 text-xs uppercase tracking-wide text-slate-400">
            <tr>
              <th className="px-4 py-2 text-left">Rank</th>
              <th className="px-4 py-2 text-left">User</th>
              <th className="px-4 py-2 text-center">Solved</th>
              <th className="px-4 py-2 text-center">Points</th>
              <th className="px-4 py-2 text-right">Last Update</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {entries.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-8 text-center text-xs text-slate-400">
                  No submissions yet. Be the first to score!
                </td>
              </tr>
            ) : (
              entries.map((entry, index) => {
                const isCurrent = currentUser && entry.username.toLowerCase() === currentUser.toLowerCase();
                const rank = index + 1;
                const rowGlow =
                  rank === 1
                    ? "bg-gradient-to-r from-amber-400/15 via-transparent to-transparent"
                    : rank === 2
                      ? "bg-gradient-to-r from-slate-200/10 via-transparent to-transparent"
                      : rank === 3
                        ? "bg-gradient-to-r from-orange-400/10 via-transparent to-transparent"
                        : "";
                const highlightClasses = [rowGlow, isCurrent ? "bg-cyan-500/10" : ""].filter(Boolean).join(" ");
                return (
                  <tr
                    key={entry.username}
                    className={highlightClasses}
                  >
                    <td className="px-5 py-3 text-left font-semibold text-slate-100">#{rank}</td>
                    <td className="px-5 py-3 text-left text-slate-200">{entry.username}</td>
                    <td className="px-5 py-3 text-center text-slate-100">{entry.solved}</td>
                    <td className="px-5 py-3 text-center text-cyan-200">{entry.totalPoints}</td>
                    <td className="px-5 py-3 text-right text-xs text-slate-400">
                      {formatRelative(entry.lastUpdated)}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}


