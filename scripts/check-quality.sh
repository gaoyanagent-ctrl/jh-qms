#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "== IAF quality gate =="

node "$ROOT_DIR/scripts/check-platform-foundation-templates.js"
"$ROOT_DIR/scripts/run-backend-tests.sh"
"$ROOT_DIR/scripts/run-frontend-checks.sh"

echo "== Quality gate completed =="
