"use client";

import { useEffect, useState } from "react";

type Props = {
  language: string;
  value: string;
  onChange: (value: string) => void;
};

const templates: Record<string, string> = {
  java: `import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // TODO: implement solution
    }
}
`,
  cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    // TODO: implement solution
    return 0;
}
`
};

export function CodeEditor({ language, value, onChange }: Props) {
  const [internalValue, setInternalValue] = useState(value);

  useEffect(() => {
    setInternalValue(value);
  }, [value]);

  useEffect(() => {
    if (value.trim().length === 0 && templates[language]) {
      setInternalValue(templates[language]);
      onChange(templates[language]);
    }
  }, [language, value, onChange]);

  const handleChange = (content: string) => {
    setInternalValue(content);
    onChange(content);
  };

  return (
    <textarea
      className="h-full min-h-[420px] w-full resize-none rounded-xl border border-slate-800 bg-slate-950/90 p-4 font-mono text-sm text-slate-100 outline-none focus:border-cyan-400 focus:ring-2 focus:ring-cyan-500/40"
      value={internalValue}
      onChange={(event) => handleChange(event.target.value)}
      spellCheck={false}
    />
  );
}
