#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
MODE="${1:-all}"

if [[ ! -f "$FRONTEND_DIR/package.json" ]]; then
  echo "Frontend checks skipped: frontend/package.json does not exist yet."
  exit 0
fi

cd "$FRONTEND_DIR"

resolve_frontend_tool() {
  local tool_name="$1"
  local local_bin="$FRONTEND_DIR/node_modules/.bin/$tool_name"
  if [[ -x "$local_bin" ]]; then
    echo "$local_bin"
    return
  fi
  command -v "$tool_name" || true
}

TSC="$(resolve_frontend_tool tsc)"
VITEST="$(resolve_frontend_tool vitest)"
VITE="$(resolve_frontend_tool vite)"

run_static_guardrails() {
  echo "== Frontend static guardrails =="

  if ! command -v rg >/dev/null 2>&1; then
    echo "Frontend guardrail failed: ripgrep (rg) is required for static checks." >&2
    exit 1
  fi

  if rg -n "(axios|\\bfetch\\(|new XMLHttpRequest)" "$FRONTEND_DIR/apps" "$FRONTEND_DIR/packages" \
    --glob '!packages/api-client/**' \
    --glob '!**/*.test.*' \
    --glob '!**/test/**'; then
    echo "Frontend guardrail failed: HTTP calls must go through packages/api-client." >&2
    exit 1
  fi

  if rg -n "(TOKEN_KEY|accessToken.*localStorage|localStorage.*accessToken|iaf_access_token)" "$FRONTEND_DIR/apps" \
    --glob '!**/*.test.*' \
    --glob '!**/test/**'; then
    echo "Frontend guardrail failed: apps must not read or write auth tokens directly." >&2
    exit 1
  fi
}

run_static_guardrails

if [[ -z "$TSC" || -z "$VITEST" || -z "$VITE" ]]; then
  echo "Frontend checks skipped: frontend dependencies are not installed."
  exit 0
fi

typecheck_projects=(
  "packages/domain-types"
  "packages/api-client"
  "packages/auth"
  "packages/permissions"
  "packages/i18n"
  "packages/theme"
  "packages/ui-core"
  "packages/ui-business"
  "packages/mock-data"
  "packages/table-engine"
  "packages/form-engine"
  "apps/pc-admin"
)

run_typecheck() {
  echo "== Frontend typecheck =="
  for project in "${typecheck_projects[@]}"; do
    "$TSC" -p "$FRONTEND_DIR/$project/tsconfig.json" --noEmit
  done
}

run_tests() {
  echo "== Frontend tests =="
  (cd "$FRONTEND_DIR" && "$VITEST" run --config "$FRONTEND_DIR/vitest.config.ts")
}

run_build() {
  echo "== Frontend build =="
  (cd "$FRONTEND_DIR/apps/pc-admin" && "$VITE" build)
}

case "$MODE" in
  typecheck | lint)
    run_typecheck
    ;;
  test)
    run_tests
    ;;
  build)
    run_build
    ;;
  all)
    run_typecheck
    run_tests
    run_build
    ;;
  *)
    echo "Unknown frontend check mode: $MODE" >&2
    exit 2
    ;;
esac
