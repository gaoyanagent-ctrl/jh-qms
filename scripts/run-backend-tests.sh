#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"

if [[ -x "$BACKEND_DIR/mvnw" ]]; then
  cd "$BACKEND_DIR"
  ./mvnw test
elif [[ -f "$BACKEND_DIR/pom.xml" ]]; then
  cd "$BACKEND_DIR"
  mvn test
else
  echo "Backend tests skipped: backend/pom.xml does not exist yet."
fi
