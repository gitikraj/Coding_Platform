# Shodh-a-Code Contest Platform

An end-to-end prototype of a real-time coding contest experience. The system pairs a Spring Boot backend (with a Docker-powered judge) and a Next.js frontend to deliver contest details, live submissions, and a polling leaderboard.

## Repository Structure

```
.
├── backend/   # Spring Boot REST API and judge orchestrator
├── frontend/  # Next.js + Tailwind UI for contest and leaderboard
├── judge/     # Minimal runtime image used to execute participant code
└── docker-compose.yml
```

## Quick Start (Docker Compose)

> Requires Docker Desktop (or compatible engine) because the judge runs user code inside containers invoked from the backend.

1. Build and launch all services:
   ```bash
   docker compose up --build
   ```
   - Backend API lives at http://localhost:8081
   - Frontend app lives at http://localhost:3000
2. The compose stack mounts `/var/run/docker.sock` so the backend can spawn containers. The judge image is kept running (`sleep` entrypoint) so the runtime is always available for new submissions.

## Manual Setup

### Backend

> Prerequisites: JDK 17+, Maven 3.9+, Docker CLI

```bash
cd backend
mvn spring-boot:run
```

Environment variables you can override:

| Variable | Default | Purpose |
| --- | --- | --- |
| `JUDGE_IMAGE` | `shodhaicode/judge:latest` | Docker image used for execution |
| `JUDGE_USE_DOCKER` | `true` | Toggle to fall back to local JVM execution (for dev without Docker) |
| `JUDGE_WORKSPACE_ROOT` | `${java.io.tmpdir}/judge-workspaces` | Shared path mounted into judge containers |

### Frontend

> Prerequisites: Node.js 20+

```bash
cd frontend
npm install
npm run dev
```

Configure the backend origin as needed via `NEXT_PUBLIC_API_BASE_URL` (defaults to `http://localhost:8081`).

If you pull new UI dependencies, run:
```bash
cd frontend
npm install
```

## API Summary

| Method & Path | Description |
| --- | --- |
| `GET /api/contests/{contestId}` | Contest metadata with ordered problems and sample cases. |
| `GET /api/contests/{contestId}/leaderboard` | Aggregated leaderboard (solved count, score, last update). |
| `POST /api/submissions` | Accepts `{ contestId, problemId, username, language, sourceCode }`. Persists, enqueues, and returns a `submissionId`. |
| `GET /api/submissions/{submissionId}` | Latest status for a submission (`PENDING`, `RUNNING`, `ACCEPTED`, etc.) with stdout/stderr and metrics. |

Entities are persisted in an in-memory H2 database, seeded with a sample contest (`shodh-101`) and three starter problems.
