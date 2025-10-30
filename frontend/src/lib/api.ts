import { Contest, LeaderboardEntry, SubmissionStatus } from "@/lib/types";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8081";

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `Request failed with status ${response.status}`);
  }
  return (await response.json()) as T;
}

export async function fetchContest(contestId: string): Promise<Contest> {
  const response = await fetch(`${API_BASE}/api/contests/${contestId}`, {
    cache: "no-store"
  });
  return handleResponse<Contest>(response);
}

export async function submitSolution(payload: {
  contestId: string;
  problemId: number;
  username: string;
  language: string;
  sourceCode: string;
}): Promise<{ submissionId: string; status: string }> {
  const response = await fetch(`${API_BASE}/api/submissions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  return handleResponse(response);
}

export async function fetchSubmissionStatus(submissionId: string): Promise<SubmissionStatus> {
  const response = await fetch(`${API_BASE}/api/submissions/${submissionId}`, {
    cache: "no-store"
  });
  return handleResponse(response);
}

export async function fetchLeaderboard(contestId: string): Promise<LeaderboardEntry[]> {
  const response = await fetch(`${API_BASE}/api/contests/${contestId}/leaderboard`, {
    cache: "no-store"
  });
  return handleResponse(response);
}
