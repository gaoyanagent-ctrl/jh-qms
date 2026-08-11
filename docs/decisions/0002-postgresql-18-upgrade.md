# ADR-0002: Local PostgreSQL 16 → 18 Upgrade and Host Port 5123

## Status

Accepted

## Context

The IAF backend declares PostgreSQL as the primary database
(`AGENTS.md`, `docs/architecture/02_技术选型报告.md`) but the
repository's local development defaults were pinned to `postgres:16`
in `docker-compose.yml` and the `TASK-0101` migration-check example.
No ADR had been recorded for the choice.

Starting 2026-07-03, the local machine already hosts two unrelated
PostgreSQL containers:

- `multica-postgres-1` (pgvector/pgvector:pg17) on host `5432`.
- `iaos-integration-postgres` (postgres:16-alpine) on host `5433`.

`HANDOFF-0101_next_agent_execution_plan.md` Section 8 explicitly forbids
stopping or modifying unrelated project containers, so the IAF
PostgreSQL service cannot bind host `5432` or `5433`.

The TASK-0101 handoff plan also assumes a fresh, clean PostgreSQL
container is available for migration checks, so the IAF service must
be reachable for both local development and CI migration runs.

## Decision

1. The IAF local development stack upgrades from `postgres:16` to
   `postgres:18` (full Debian-based image, not `-alpine`) to align
   with current PostgreSQL LTS-aligned releases and to make use of
   the newer JSON/JSONB and logical-replication features that the
   platform code is expected to need in later phases.
2. The IAF PostgreSQL container binds host port `5123` (not `5432`)
   to avoid colliding with `multica-postgres-1`. The default is
   captured in `docker-compose.yml`, `.env.example`, and
   `backend/iaf-app/src/main/resources/application.yml`. The
   container continues to listen on the standard PostgreSQL port
   `5432` internally.
3. The `TASK-0101` plan example is updated to `postgres:18` so the
   migration-check command matches the actual stack.
4. `CLAUDE.md` and this ADR are the source of truth for the new
   local default.

## Consequences

### Positive

- No conflict with existing project containers; `docker compose up`
  works out of the box for IAF developers on this host.
- PostgreSQL 18 is the current major version and ships with the
  features expected by later platform phases (improved JSONB
  indexing, stronger logical replication, performance fixes).
- Single source of truth (`docker-compose.yml`) keeps dev, CI, and
  plan examples aligned.

### Negative / Trade-offs

- Local developers who already pointed tooling at `localhost:5432`
  must update to `localhost:5123` (or override `IAF_DATASOURCE_URL`).
- The full `postgres:18` image is larger than `postgres:18-alpine`;
  we accept the size cost for the broader compatibility surface
  during early bootstrap.
- The host port `5123` is an arbitrary choice that the team may
  revisit; any future change should be made in a new ADR, not by
  editing this one.

### Compliance Notes

- AGENTS.md ADR rule requires an ADR for "tech stack changes";
  this document satisfies that rule.
- AGENTS.md rule "Do not change existing Flyway migrations" is
  unaffected. `V0001__init_platform_auth_schema.sql` was authored
  without PostgreSQL-version-specific features and remains valid
  on PostgreSQL 18.
- AGENTS.md rule "PostgreSQL first, with MySQL compatibility
  reserved" is preserved; this ADR only sets the major version.

## How To Apply

- New IAF developers: copy `.env.example` to `.env`, run
  `docker compose up -d postgres redis minio`, then connect
  applications to `localhost:5123`.
- Migration checks: use a temporary `postgres:18` container
  without host port mapping, as described in
  `docs/operations/HANDOFF-0101_next_agent_execution_plan.md`
  Section 8 (updated to reflect this version).
- Production / staging: out of scope for this ADR; managed
  PostgreSQL services should pin the same major version
  independently.
