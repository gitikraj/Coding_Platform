"use client";

import { Problem } from "@/lib/types";
import clsx from "clsx";

type Props = {
  problems: Problem[];
  selectedId: number | null;
  onSelect: (problem: Problem) => void;
};

export function ProblemTabs({ problems, selectedId, onSelect }: Props) {
  return (
    <div className="flex flex-1 flex-col overflow-hidden rounded-xl border border-slate-800 bg-slate-900/60">
      <div className="flex gap-2 border-b border-slate-800 px-4 py-3">
        {problems.map((problem) => (
          <button
            key={problem.id}
            onClick={() => onSelect(problem)}
            className={clsx(
              "rounded-lg px-3 py-1.5 text-sm font-semibold transition",
              selectedId === problem.id
                ? "bg-cyan-500 text-slate-900"
                : "bg-slate-900 text-slate-200 hover:bg-slate-800"
            )}
          >
            {problem.title}
          </button>
        ))}
      </div>
      <div className="flex-1 overflow-y-auto px-6 py-5 text-sm leading-relaxed text-slate-100">
        {selectedId == null ? (
          <p>Select a problem to view description.</p>
        ) : (
          <ProblemDescription problem={problems.find((p) => p.id === selectedId)!} />
        )}
      </div>
    </div>
  );
}

function ProblemDescription({ problem }: { problem: Problem }) {
  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-semibold text-slate-50">{problem.title}</h2>
        <p className="text-xs text-slate-400">
          Time limit: {problem.timeLimitMs} ms | Memory limit: {problem.memoryLimitMb} MB | Points:{" "}
          {problem.pointValue}
        </p>
      </div>
      <article className="whitespace-pre-wrap text-sm text-slate-100">{problem.statement}</article>
      <div className="space-y-3 rounded-lg bg-slate-950/60 p-4">
        <h3 className="text-sm font-semibold text-slate-200">Sample Test Cases</h3>
        <div className="grid gap-3">
          {problem.samples.length === 0 ? (
            <p className="text-xs text-slate-400">No samples available.</p>
          ) : (
            problem.samples.map((sample, index) => (
              <div key={index} className="grid gap-1 rounded border border-slate-800 p-3">
                <span className="text-xs font-semibold uppercase tracking-wide text-slate-400">
                  Input
                </span>
                <pre className="overflow-auto rounded bg-slate-950/80 p-2 text-xs text-slate-200">
                  {sample.input}
                </pre>
                <span className="text-xs font-semibold uppercase tracking-wide text-slate-400">
                  Output
                </span>
                <pre className="overflow-auto rounded bg-slate-950/80 p-2 text-xs text-slate-200">
                  {sample.output}
                </pre>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
