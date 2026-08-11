#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${IAF_BASE_URL:-http://localhost:8080}"
TENANT_CODE="${IAF_TENANT_CODE:-default}"
USERNAME="${IAF_USERNAME:-admin}"
PASSWORD="${IAF_PASSWORD:-admin123}"
EXPECTED_TENANT_ID="${IAF_EXPECTED_TENANT_ID:-}"
TARGET_TENANT_ID="${IAF_SMOKE_TARGET_TENANT_ID:-}"

require_tool() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required tool: $1" >&2
    exit 2
  fi
}

require_tool curl
require_tool jq

TMP_DIR="$(mktemp -d)"
TOKEN=""
ORIGINAL_PREFERENCES_FILE="$TMP_DIR/original-preferences.json"
PREFERENCES_CAPTURED=false
PREFERENCES_RESTORED=false

cleanup() {
  local restore_status

  if [[ "$PREFERENCES_CAPTURED" == "true" && "$PREFERENCES_RESTORED" == "false" && -n "$TOKEN" ]]; then
    restore_status="$(request PUT /api/platform/preferences/me "@$ORIGINAL_PREFERENCES_FILE" "$TOKEN" 2>/dev/null || true)"
    if [[ "$restore_status" == "200" ]] && jq -e '.success == true' "$TMP_DIR/response.json" >/dev/null 2>&1; then
      echo "PASS preference restore"
    else
      echo "WARN preference restore failed; manual preference repair may be required" >&2
    fi
  fi

  rm -rf "$TMP_DIR"
}

trap cleanup EXIT

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local token="${4:-}"
  local output="$TMP_DIR/response.json"
  local status

  if [[ "$body" == @* && -n "$token" ]]; then
    status="$(curl -sS -o "$output" -w "%{http_code}" -X "$method" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token" \
      -d "${body}" \
      "$BASE_URL$path")"
  elif [[ "$body" == @* ]]; then
    status="$(curl -sS -o "$output" -w "%{http_code}" -X "$method" \
      -H "Content-Type: application/json" \
      -d "${body}" \
      "$BASE_URL$path")"
  elif [[ -n "$body" && -n "$token" ]]; then
    status="$(curl -sS -o "$output" -w "%{http_code}" -X "$method" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token" \
      -d "$body" \
      "$BASE_URL$path")"
  elif [[ -n "$body" ]]; then
    status="$(curl -sS -o "$output" -w "%{http_code}" -X "$method" \
      -H "Content-Type: application/json" \
      -d "$body" \
      "$BASE_URL$path")"
  elif [[ -n "$token" ]]; then
    status="$(curl -sS -o "$output" -w "%{http_code}" -X "$method" \
      -H "Authorization: Bearer $token" \
      "$BASE_URL$path")"
  else
    status="$(curl -sS -o "$output" -w "%{http_code}" -X "$method" "$BASE_URL$path")"
  fi

  printf '%s\n' "$status"
}

assert_success_body() {
  local label="$1"
  local status="$2"
  local response="$TMP_DIR/response.json"

  if [[ "$status" != "200" ]]; then
    echo "FAIL $label: expected HTTP 200, got $status" >&2
    cat "$response" >&2 || true
    exit 1
  fi

  if ! jq -e '.success == true' "$response" >/dev/null; then
    echo "FAIL $label: response success is not true" >&2
    cat "$response" >&2 || true
    exit 1
  fi

  echo "PASS $label"
}

assert_denied() {
  local label="$1"
  local status="$2"

  if [[ "$status" != "401" && "$status" != "403" ]]; then
    echo "FAIL $label: expected HTTP 401 or 403, got $status" >&2
    cat "$TMP_DIR/response.json" >&2 || true
    exit 1
  fi

  echo "PASS $label"
}

echo "== Platform Foundation smoke test =="
echo "Base URL: $BASE_URL"
echo "Tenant: $TENANT_CODE"
echo "User: $USERNAME"

status="$(request GET /api/health)"
assert_success_body "health" "$status"

login_body="$(jq -n \
  --arg tenantCode "$TENANT_CODE" \
  --arg username "$USERNAME" \
  --arg password "$PASSWORD" \
  '{tenantCode:$tenantCode, username:$username, password:$password}')"
status="$(request POST /api/platform/auth/login "$login_body")"
assert_success_body "login" "$status"
TOKEN="$(jq -r '.data.accessToken // empty' "$TMP_DIR/response.json")"
if [[ -z "$TOKEN" ]]; then
  echo "FAIL login: accessToken missing" >&2
  cat "$TMP_DIR/response.json" >&2
  exit 1
fi

if [[ -n "$EXPECTED_TENANT_ID" ]]; then
  actual_tenant_id="$(jq -r '.data.tenantId // empty' "$TMP_DIR/response.json")"
  if [[ "$actual_tenant_id" != "$EXPECTED_TENANT_ID" ]]; then
    echo "FAIL login: expected tenantId $EXPECTED_TENANT_ID, got $actual_tenant_id" >&2
    exit 1
  fi
  echo "PASS login tenant id"
fi

status="$(request GET /api/platform/auth/me "" "$TOKEN")"
assert_success_body "current user" "$status"
jq -e '.data.userId and .data.tenantId and (.data.permissions | type == "array")' "$TMP_DIR/response.json" >/dev/null

status="$(request GET /api/platform/auth/menus "" "$TOKEN")"
assert_success_body "current menus" "$status"

status="$(request GET '/api/platform/users?pageNo=1&pageSize=10' "" "$TOKEN")"
assert_success_body "users" "$status"

status="$(request GET /api/platform/orgs/tree "" "$TOKEN")"
assert_success_body "organizations" "$status"

status="$(request GET '/api/platform/roles?pageNo=1&pageSize=10' "" "$TOKEN")"
assert_success_body "roles" "$status"

status="$(request GET /api/platform/menus/tree "" "$TOKEN")"
assert_success_body "menu tree" "$status"

status="$(request GET /api/platform/permissions "" "$TOKEN")"
assert_success_body "permissions" "$status"

status="$(request GET /api/platform/theme/current "" "$TOKEN")"
assert_success_body "theme" "$status"

status="$(request GET /api/platform/brand/current "" "$TOKEN")"
assert_success_body "brand" "$status"

status="$(request GET '/api/platform/i18n/resources?locale=zh-CN' "" "$TOKEN")"
assert_success_body "i18n zh-CN" "$status"

status="$(request GET /api/platform/preferences/me "" "$TOKEN")"
assert_success_body "preference read before update" "$status"
jq '{settings:(.data.settings // {})}' "$TMP_DIR/response.json" > "$ORIGINAL_PREFERENCES_FILE"
PREFERENCES_CAPTURED=true

preference_body="$(jq --arg checkedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  '.settings.platformFoundationSmokeTest = {lastCheckedAt:$checkedAt}' \
  "$ORIGINAL_PREFERENCES_FILE")"
status="$(request PUT /api/platform/preferences/me "$preference_body" "$TOKEN")"
assert_success_body "preference merged update" "$status"

status="$(request PUT /api/platform/preferences/me "@$ORIGINAL_PREFERENCES_FILE" "$TOKEN")"
assert_success_body "preference restore" "$status"
PREFERENCES_RESTORED=true

status="$(request GET '/api/platform/users?pageNo=1&pageSize=1')"
assert_denied "protected API denies missing token" "$status"

if [[ -n "$TARGET_TENANT_ID" ]]; then
  status="$(request GET "/api/platform/outbox-events?tenantId=$TARGET_TENANT_ID&pageNo=1&pageSize=10" "" "$TOKEN")"
  assert_success_body "outbox events" "$status"
else
  echo "SKIP outbox events: set IAF_SMOKE_TARGET_TENANT_ID to enable"
fi

echo "== Platform Foundation smoke test completed =="
