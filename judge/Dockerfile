FROM eclipse-temurin:17-jdk AS base

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        bash \
        coreutils \
        g++ && \
    rm -rf /var/lib/apt/lists/*

# Create non-root execution user (idempotent)
RUN if ! id -u runner >/dev/null 2>&1; then useradd -ms /bin/bash runner; fi

WORKDIR /workspace
USER runner
