"use client";

import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";

export default function JoinPage() {
  const router = useRouter();
  const [contestId, setContestId] = useState("shodh-101");
  const [username, setUsername] = useState("");

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!contestId || !username) {
      return;
    }
    const trimmedContest = contestId.trim();
    const trimmedUser = username.trim();
    if (!trimmedContest || !trimmedUser) {
      return;
    }
    localStorage.setItem("shodh-user", trimmedUser);
    router.push(`/contest/${encodeURIComponent(trimmedContest)}?user=${encodeURIComponent(trimmedUser)}`);
  };

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-12 sm:px-6">
      <div className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(circle_at_top,_rgba(56,189,248,0.12),transparent_45%),radial-gradient(circle_at_bottom,_rgba(76,29,149,0.12),transparent_55%)]" />
      <Card className="w-full max-w-xl border-slate-800/70 bg-slate-900/70 shadow-2xl shadow-slate-950/40 backdrop-blur">
        <CardHeader className="space-y-4 text-center">
          <Badge className="mx-auto w-fit border border-cyan-400/40 bg-cyan-500/15 text-cyan-100">
            Join the Arena
          </Badge>
          <CardTitle className="text-3xl font-bold text-white">Shodh-a-Code Contest</CardTitle>
          <CardDescription className="text-slate-300">
            Enter the contest ID and your handle to jump into the live leaderboard experience.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2 text-left">
              <label className="text-sm font-medium text-slate-200" htmlFor="contest">
                Contest ID
              </label>
              <input
                id="contest"
                className="w-full rounded-lg border border-slate-700 bg-slate-950/80 px-4 py-2 text-slate-100 outline-none ring-offset-slate-950 transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-500/40 focus:ring-offset-2"
                value={contestId}
                onChange={(event) => setContestId(event.target.value)}
                placeholder="Enter contest code"
              />
            </div>
            <div className="space-y-2 text-left">
              <label className="text-sm font-medium text-slate-200" htmlFor="username">
                Username
              </label>
              <input
                id="username"
                className="w-full rounded-lg border border-slate-700 bg-slate-950/80 px-4 py-2 text-slate-100 outline-none ring-offset-slate-950 transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-500/40 focus:ring-offset-2"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                placeholder="Pick a handle"
              />
            </div>
            <Button type="submit" className="w-full justify-center" variant="hero">
              Enter Contest
            </Button>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}


