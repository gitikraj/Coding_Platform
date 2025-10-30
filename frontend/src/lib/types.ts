export type TestCase = {
  input: string;
  output: string;
};

export type Problem = {
  id: number;
  title: string;
  slug: string;
  statement: string;
  timeLimitMs: number;
  memoryLimitMb: number;
  pointValue: number;
  samples: TestCase[];
};

export type Contest = {
  id: string;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  problems: Problem[];
};

export type LeaderboardEntry = {
  username: string;
  solved: number;
  totalPoints: number;
  lastUpdated: number;
};

export type SubmissionStatus = {
  submissionId: string;
  status: string;
  verdict?: string;
  runtimeMillis?: number;
  memoryKb?: number;
  stdout?: string;
  stderr?: string;
  submittedAt: string;
  updatedAt: string;
};
