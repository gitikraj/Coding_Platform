"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useCallback, useEffect, useMemo, useRef, useState } from "react";

import { LeaderboardPanel } from "@/components/LeaderboardPanel";
import { ProblemTabs } from "@/components/ProblemTabs";
import { SubmissionStatusCard } from "@/components/SubmissionStatusCard";
import { CodeEditor } from "@/components/CodeEditor";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card";
import { toast } from "@/hooks/use-toast";
import { fetchContest, fetchLeaderboard, fetchSubmissionStatus, submitSolution } from "@/lib/api";
import { Contest, LeaderboardEntry, Problem, SubmissionStatus } from "@/lib/types";

type Props = {
  params: {
    contestId: string;
  };
};

const POLL_INTERVAL = 2500;
const LEADERBOARD_INTERVAL = 20000;

export default function ContestPage({ params }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const contestId = params.contestId;

  const [contest, setContest] = useState<Contest | null>(null);
  const [selectedProblem, setSelectedProblem] = useState<Problem | null>(null);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [language, setLanguage] = useState("java");
  const [codeByProblem, setCodeByProblem] = useState<Record<string, string>>({});
  const [status, setStatus] = useState<SubmissionStatus | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [username, setUsername] = useState("");
  const [initialized, setInitialized] = useState(false);
  const [notFound, setNotFound] = useState(false);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const leaderboardRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    const paramUser = searchParams.get("user");
    const storedUser = typeof window !== "undefined" ? window.localStorage.getItem("shodh-user") : null;
    const finalUser = paramUser ?? storedUser ?? "";
    if (paramUser && typeof window !== "undefined") {
      window.localStorage.setItem("shodh-user", paramUser);
    }
    setUsername(finalUser);
    setInitialized(true);
    if (!finalUser) {
      router.replace("/");
    }
  }, [searchParams, router]);

  const loadContest = useCallback(async () => {
    try {
      const data = await fetchContest(contestId);
      setError(null);
      setNotFound(false);
      setContest(data);
      if (data.problems.length > 0) {
        setSelectedProblem((current) => current ?? data.problems[0]);
      }
    } catch (err) {
      const message = (err as Error).message;
      setError(message);
      if (message?.toLowerCase().includes("not found")) {
        setContest(null);
        setNotFound(true);
      } else {
        setNotFound(false);
      }
    }
  }, [contestId]);

  const loadLeaderboard = useCallback(async () => {
    try {
      const rows = await fetchLeaderboard(contestId);
      setLeaderboard(rows);
    } catch (err) {
      // ignore transient errors
      console.warn("Failed to load leaderboard", err);
    }
  }, [contestId]);

  useEffect(() => {
    if (!initialized || !username) {
      return;
    }
    loadContest();
    loadLeaderboard();
    leaderboardRef.current = setInterval(loadLeaderboard, LEADERBOARD_INTERVAL);

    return () => {
      if (pollRef.current) {
        clearInterval(pollRef.current);
      }
      if (leaderboardRef.current) {
        clearInterval(leaderboardRef.current);
      }
    };
  }, [contestId, initialized, username, loadContest, loadLeaderboard]);

  const stopPolling = () => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  };

  const startPolling = (submissionId: string) => {
    stopPolling();
    pollRef.current = setInterval(async () => {
      try {
        const info = await fetchSubmissionStatus(submissionId);
        setStatus(info);
        if (!["PENDING", "RUNNING"].includes(info.status)) {
          stopPolling();
          loadLeaderboard();
        }
      } catch (err) {
        console.error("Failed to fetch submission status", err);
      }
    }, POLL_INTERVAL);
  };

  const currentCode = useMemo(() => {
    if (!selectedProblem) {
      return "";
    }
    const key = `${selectedProblem.id}:${language}`;
    return codeByProblem[key] ?? "";
  }, [selectedProblem, language, codeByProblem]);

  const handleCodeChange = useCallback(
    (value: string) => {
      if (!selectedProblem) {
        return;
      }
      const key = `${selectedProblem.id}:${language}`;
      setCodeByProblem((prev) => {
        const existing = prev[key];
        if (existing === value) {
          return prev;
        }
        return {
          ...prev,
          [key]: value
        };
      });
    },
    [selectedProblem, language]
  );

  const isSubmitDisabled = useMemo(() => {
    return !selectedProblem || !currentCode.trim() || isSubmitting;
  }, [selectedProblem, currentCode, isSubmitting]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedProblem) {
      return;
    }
    setError(null);
    setIsSubmitting(true);
    setStatus(undefined);
    try {
      const payload = {
        contestId,
        problemId: selectedProblem.id,
        username,
        language,
        sourceCode: currentCode
      };
      const response = await submitSolution(payload);
      toast({
        title: "Submission queued",
        description: "Hang tight while the judge evaluates your code.",
      });
      setStatus({
        submissionId: response.submissionId,
        status: response.status,
        verdict: undefined,
        runtimeMillis: undefined,
        memoryKb: undefined,
        stdout: undefined,
        stderr: undefined,
        submittedAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });
      startPolling(response.submissionId);
    } catch (err) {
      const message = (err as Error).message;
      setError(message);
      toast({
        title: "Submission failed",
        description: message,
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!contest) {
    if (notFound) {
      return (
        <main className="flex min-h-screen flex-col items-center justify-center px-6 py-12">
          <Card className="w-full max-w-lg border-slate-800/70 bg-slate-900/70 text-center shadow-xl shadow-slate-950/40">
            <CardHeader className="space-y-3">
              <Badge variant="outline" className="mx-auto w-fit border border-rose-500/40 text-rose-200">
                Contest not found
              </Badge>
              <CardTitle className="text-2xl font-semibold text-white">Contest unavailable</CardTitle>
              <CardDescription className="text-slate-300">
                We couldn&apos;t locate a contest with ID{" "}
                <span className="font-semibold text-white">{contestId}</span>.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-slate-400">
                Double-check the contest code or head back to the lobby to join an active event.
              </p>
            </CardContent>
            <CardFooter className="justify-center">
              <Button type="button" variant="hero" onClick={() => router.replace("/")}>
                Back to join page
              </Button>
            </CardFooter>
          </Card>
        </main>
      );
    }
    return (
      <main className="flex min-h-screen flex-col items-center justify-center">
        {error ? (
          <div className="rounded-lg border border-rose-600 bg-rose-900/40 px-6 py-4 text-rose-100">
            <p className="font-semibold">Unable to load contest.</p>
            <p className="text-sm">{error}</p>
          </div>
        ) : (
            <p className="text-sm text-slate-200">Loading contest...</p>
        )}
      </main>
    );
  }

  return (
    <main className="relative min-h-screen overflow-hidden px-4 py-8 sm:px-8 lg:px-16">
      <div className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(circle_at_top,_rgba(56,189,248,0.12),transparent_45%),radial-gradient(circle_at_bottom,_rgba(168,85,247,0.1),transparent_55%)]" />
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <Card className="relative overflow-hidden border-cyan-500/20">
          <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-cyan-500/15 via-transparent to-purple-500/15 blur-3xl" />
          <CardHeader className="relative z-10 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div className="space-y-3">
              <Badge variant="default" className="w-fit border border-cyan-400/40 bg-cyan-500/15 text-cyan-100">
                Live Contest - #{contest.id}
              </Badge>
              <CardTitle className="text-3xl font-bold text-white drop-shadow">{contest.title}</CardTitle>
              <CardDescription className="max-w-3xl text-slate-300">
                {contest.description}
              </CardDescription>
            </div>
            <div className="flex items-center gap-3 rounded-xl border border-slate-800 bg-slate-950/70 px-5 py-3 text-sm text-slate-200 shadow-inner">
              <div className="flex flex-col text-right">
                <span className="text-xs uppercase tracking-wide text-slate-400">Signed in as</span>
                <span className="text-base font-semibold text-white">{username}</span>
              </div>
              <Badge variant="outline" className="border-cyan-500/40 text-cyan-200">
                {contest.problems.length} problems
              </Badge>
            </div>
          </CardHeader>
        </Card>

        {error && (
          <Card className="border-rose-500/40 bg-rose-950/40 text-rose-100">
            <CardContent className="pt-6 text-sm">{error}</CardContent>
          </Card>
        )}

        <section className="grid gap-8 lg:grid-cols-3">
          <div className="flex flex-col gap-8 lg:col-span-2">
            <ProblemTabs
              problems={contest.problems}
              selectedId={selectedProblem?.id ?? null}
              onSelect={setSelectedProblem}
            />
            <form onSubmit={handleSubmit} className="group">
              <Card className="border-slate-800/70 bg-slate-900/70">
                <CardHeader className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div className="space-y-2">
                    <CardTitle className="text-xl text-white">Code Editor</CardTitle>
                    <CardDescription>Write, run, and submit your solution in real time.</CardDescription>
                  </div>
                  <label className="flex items-center gap-3 rounded-lg border border-slate-800 bg-slate-950/80 px-3 py-2 text-xs font-medium text-slate-300 md:text-sm">
                    <span className="text-slate-200">Language</span>
                    <select
                      className="rounded border border-slate-700 bg-slate-900 px-2 py-1 text-xs text-slate-100 focus:border-cyan-400 focus:outline-none md:text-sm"
                      value={language}
                      onChange={(event) => {
                        setLanguage(event.target.value);
                      }}
                    >
                      <option value="java">Java</option>
                      <option value="cpp">C++17</option>
                    </select>
                  </label>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="min-h-[420px] overflow-hidden rounded-xl border border-slate-800 bg-slate-950/80 shadow-inner">
                    <CodeEditor language={language} value={currentCode} onChange={handleCodeChange} />
                  </div>
                </CardContent>
                <CardFooter className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <span className="text-xs text-slate-400">
                    Submissions update every {Math.floor(POLL_INTERVAL / 1000)} seconds.
                  </span>
                  <Button
                    type="submit"
                    disabled={isSubmitDisabled}
                    variant="hero"
                    className="min-w-[160px] shadow-[0_0_30px_rgba(34,211,238,0.25)]"
                  >
                    {isSubmitting ? "Submitting..." : "Submit Solution"}
                  </Button>
                </CardFooter>
              </Card>
            </form>
            <SubmissionStatusCard status={status} isPending={isSubmitting || !!pollRef.current} />
          </div>
          <div className="flex flex-col gap-6">
            <LeaderboardPanel entries={leaderboard} currentUser={username} />
          </div>
        </section>
      </div>
    </main>
  );
}


