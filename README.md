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

## Frontend Workflow

1. Join page collects a contest ID and username, then routes to `/contest/{contestId}`.
2. Contest page:
   - Fetches contest details once and renders problems via tabbed panels.
   - Provides minimal Java and C++ templates within a lightweight editor (now taller for better readability).
   - Submits code through the API and polls submission status every 2.5s until terminal.
   - Shows real-time toast feedback for submission state.
   - Polls the leaderboard every 20s to simulate a live event.
   - Gracefully handles unknown contest IDs with a dedicated “Contest unavailable” state.

## Judge Design

- A lightweight queue (`LinkedBlockingQueue`) buffers submissions for asynchronous processing.
- `SubmissionProcessingWorker` marks jobs as running, invokes the judge, and persists the verdict.
- `DockerJudgeService` writes user code + test inputs to a shared workspace, executes them inside the `shodhaicode/judge:latest` container, captures stdout/stderr, compares against expected output, and enforces timeouts. It currently supports Java and C++17 (more languages can be added by extending the strategy table).
- The default judge image carries the JDK runtime and GNU coreutils (`timeout`). The backend container installs the Docker CLI and accesses the host daemon via the mounted socket.

### Security & Isolation Notes

- Each evaluation happens in a fresh container with memory (`512m`) and CPU (`1`) limits enforced.
- Workspaces are stored under a dedicated directory (`JUDGE_WORKSPACE_ROOT`) and cleaned after each run.
- Network access is disabled for judge containers (`--network=none`).

## Design Choices & Rationale

- **Spring Boot + H2**: Fast bootstrap, schema auto-generation, and easy seed data for a self-contained prototype.
- **ProcessBuilder over orchestration frameworks**: Keeps control explicit and transparent; demonstrates core integration logic without external dependencies.
- **Next.js App Router**: Simplifies routing and data fetching while enabling a clean separation between join flow and contest UI. Tailwind keeps styling nimble.
- **Polling Strategy**: A lightweight alternative to websockets for this prototype; easier to operate and reason about while still delivering near-real-time feedback.
- **Docker-first judge**: Mirrors how production judges typically operate and showcases system integration skills.

## Testing & Validation

- Backend ships with a `@SpringBootTest` smoke test (`ContestBackendApplicationTests`).
- Due to the offline environment, Maven/Node dependencies were not downloaded here—run builds locally to verify (`mvn test`, `npm run lint`).
- When Docker is unavailable (local dev, CI), set `JUDGE_USE_DOCKER=false` to let the judge execute on the host JVM instead (includes stdin piping fallback for parity).

## Next Steps (Suggested)

1. Persist contest state in Postgres or MySQL and add Flyway migrations.
2. Persist leaderboards and submission history in Redis for faster updates.
3. Extend judge to support multiple languages with per-language templates and compile/run commands.
4. Harden security (resource cgroups, code sanitisation) and add observability dashboards.
5. Add websocket updates for submissions + leaderboard to eliminate polling.
