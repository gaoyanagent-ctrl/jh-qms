#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_DIR="$ROOT_DIR/deploy/production"
LOCK_FILE="${JH_QMS_DEPLOY_LOCK_FILE:-/tmp/jh-qms-production-deploy.lock}"
ALLOW_UNMERGED="${JH_QMS_ALLOW_UNMERGED_DEPLOY:-false}"
ENV_FILE="${JH_QMS_PRODUCTION_ENV_FILE:-$COMPOSE_DIR/.env}"

if [[ ! -f "$ENV_FILE" && -f /ai_agent/jh-qms/deploy/production/.env ]]; then
  ENV_FILE=/ai_agent/jh-qms/deploy/production/.env
fi
COMPOSE_ENV_ARGS=()
if [[ -f "$ENV_FILE" ]]; then
  COMPOSE_ENV_ARGS=(--env-file "$ENV_FILE")
elif [[ -z "${JH_QMS_DB_PASSWORD:-}" || -z "${JH_QMS_MINIO_SECRET_KEY:-}" ]]; then
  echo "Production secrets are unavailable. Set JH_QMS_PRODUCTION_ENV_FILE or export the required JH_QMS_* variables." >&2
  exit 1
fi

exec 9>"$LOCK_FILE"
if ! flock -n 9; then
  echo "Production frontend deployment is already running in another worktree." >&2
  exit 1
fi

cd "$ROOT_DIR"
if [[ -n "$(git status --porcelain)" ]]; then
  echo "Refusing production deployment from a dirty worktree." >&2
  exit 1
fi

REVISION="$(git rev-parse HEAD)"
SHORT_REVISION="$(git rev-parse --short=12 HEAD)"
git fetch origin main --quiet
if [[ "$ALLOW_UNMERGED" != "true" ]] && ! git merge-base --is-ancestor "$REVISION" origin/main; then
  echo "Refusing unmerged revision $SHORT_REVISION; merge through PR first." >&2
  echo "For an explicitly approved test-only exception set JH_QMS_ALLOW_UNMERGED_DEPLOY=true." >&2
  exit 1
fi

echo "== Menu navigation regression tests =="
docker build \
  --target build \
  --build-arg SOURCE_REVISION="$REVISION" \
  -f deploy/production/frontend.Dockerfile \
  -t "jh-qms-frontend-build:$SHORT_REVISION" .
docker run --rm "jh-qms-frontend-build:$SHORT_REVISION" \
  pnpm exec vitest run \
  apps/pc-admin/src/layouts/MainLayout.test.tsx \
  apps/pc-admin/src/modules/platform/menus/menuTree.test.ts \
  --config vitest.config.ts

IMAGE="jh-qms-frontend:$SHORT_REVISION"
echo "== Build immutable frontend image $IMAGE =="
docker build \
  --build-arg SOURCE_REVISION="$REVISION" \
  -f deploy/production/frontend.Dockerfile \
  -t "$IMAGE" .

IMAGE_REVISION="$(docker image inspect "$IMAGE" --format '{{index .Config.Labels "org.opencontainers.image.revision"}}')"
if [[ "$IMAGE_REVISION" != "$REVISION" ]]; then
  echo "Image revision mismatch: expected $REVISION, got $IMAGE_REVISION" >&2
  exit 1
fi

echo "== Replace frontend container =="
cd "$COMPOSE_DIR"
JH_QMS_FRONTEND_IMAGE="$IMAGE" docker compose "${COMPOSE_ENV_ARGS[@]}" -f compose.yml \
  up -d --no-deps --no-build --force-recreate frontend

for attempt in 1 2 3 4 5; do
  if curl -fsS http://127.0.0.1:15174/ >/dev/null; then
    break
  fi
  if [[ "$attempt" == 5 ]]; then
    echo "Frontend did not become ready." >&2
    exit 1
  fi
  sleep 1
done

RUNNING_REVISION="$(docker inspect jh-qms-frontend-1 --format '{{index .Config.Labels "org.opencontainers.image.revision"}}')"
if [[ "$RUNNING_REVISION" != "$REVISION" ]]; then
  echo "Running container revision mismatch: expected $REVISION, got $RUNNING_REVISION" >&2
  exit 1
fi
curl -fsS http://127.0.0.1:18082/api/health >/dev/null
echo "Published frontend revision $REVISION ($IMAGE)."
