#!/bin/bash
# Staging End-to-End Core Feature Integration Test
set -e

STAGING_API_URL="https://api.stage.rogic.io"
STAGING_FE_URL="https://stage.rogic.io"

echo "=========================================================="
echo "Starting Automated Staging Integration Tests against $STAGING_API_URL"
echo "=========================================================="

# 0. Healthcheck loop with retries (Spring Boot warmup)
echo "Checking Staging Backend availability (warming up)..."
MAX_ATTEMPTS=15
ATTEMPT=1
HEALTHY=false

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
  STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$STAGING_API_URL/actuator/health")
  if [ "$STATUS_CODE" -eq 200 ]; then
    HEALTH_BODY=$(curl -s "$STAGING_API_URL/actuator/health")
    if echo "$HEALTH_BODY" | grep -q '"UP"'; then
      echo "Staging backend is UP and responsive (HTTP 200)"
      HEALTHY=true
      break
    fi
  fi
  echo "Staging backend status: $STATUS_CODE. Warmup attempt $ATTEMPT/$MAX_ATTEMPTS... Waiting 5s."
  sleep 5
  ATTEMPT=$((ATTEMPT + 1))
done

if [ "$HEALTHY" = false ]; then
  echo "FAIL: Staging Backend failed to become healthy within warm-up time."
  exit 1
fi

# 1. Register Anonymous User
echo "1. Registering anonymous user..."
REG_RESPONSE=$(curl -s -X POST "$STAGING_API_URL/api/users/register")
echo "Register Response: $REG_RESPONSE"

USER_ID=$(echo "$REG_RESPONSE" | jq '.id')
USER_UUID=$(echo "$REG_RESPONSE" | jq -r '.uuid')
USERNAME=$(echo "$REG_RESPONSE" | jq -r '.username')

if [ "$USER_ID" = "null" ] || [ -z "$USER_UUID" ] || [ "$USER_UUID" = "null" ]; then
  echo "FAIL: Failed to register anonymous user"
  exit 1
fi
echo "SUCCESS: User registered with ID: $USER_ID, UUID: $USER_UUID, Username: $USERNAME"

# 2. Fetch Stages
echo "2. Fetching stages list..."
STAGES_RESPONSE=$(curl -s "$STAGING_API_URL/api/stages")
STAGE_COUNT=$(echo "$STAGES_RESPONSE" | jq '. | length')

if [ "$STAGE_COUNT" -eq 0 ] || [ "$STAGES_RESPONSE" = "null" ]; then
  echo "FAIL: No stages found or API error"
  exit 1
fi
echo "SUCCESS: Found $STAGE_COUNT stages"

# Get the first stage ID
FIRST_STAGE_ID=$(echo "$STAGES_RESPONSE" | jq '.[0].id')
FIRST_STAGE_NAME=$(echo "$STAGES_RESPONSE" | jq -r '.[0].name')
echo "Selected target stage: ID $FIRST_STAGE_ID, Name: '$FIRST_STAGE_NAME'"

# 3. Start Stage
echo "3. Starting stage ID $FIRST_STAGE_ID..."
START_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$STAGING_API_URL/api/stages/$FIRST_STAGE_ID/start")
if [ "$START_STATUS" -ne 200 ]; then
  echo "FAIL: Failed to start stage (HTTP $START_STATUS)"
  exit 1
fi
echo "SUCCESS: Stage started successfully"

# 4. Clear Stage
echo "4. Submitting clear record for stage ID $FIRST_STAGE_ID..."
CLEAR_RESPONSE=$(curl -s -X POST "$STAGING_API_URL/api/users/$USER_ID/clear?difficulty=EASY&stageId=$FIRST_STAGE_ID&elapsedTime=15")
echo "Clear Response: $CLEAR_RESPONSE"

NEW_XP=$(echo "$CLEAR_RESPONSE" | jq '.xp')
if [ "$NEW_XP" = "null" ] || [ "$NEW_XP" -eq 0 ]; then
  echo "FAIL: Failed to clear stage or XP not accumulated"
  exit 1
fi
echo "SUCCESS: Stage cleared. Accumulated XP: $NEW_XP"

# 5. Fetch User History
echo "5. Verifying user play history..."
HISTORY_RESPONSE=$(curl -s "$STAGING_API_URL/api/users/$USER_ID/history")
HISTORY_COUNT=$(echo "$HISTORY_RESPONSE" | jq '. | length')
if [ "$HISTORY_COUNT" -eq 0 ] || [ "$HISTORY_RESPONSE" = "null" ]; then
  echo "FAIL: No history record found for user $USER_ID"
  exit 1
fi
echo "SUCCESS: Found $HISTORY_COUNT history records"

# 6. Fetch Rankings
echo "6. Fetching global leaderboard rankings..."
RANKING_RESPONSE=$(curl -s "$STAGING_API_URL/api/users/ranking")
RANKING_COUNT=$(echo "$RANKING_RESPONSE" | jq '. | length')
if [ "$RANKING_COUNT" -eq 0 ] || [ "$RANKING_RESPONSE" = "null" ]; then
  echo "FAIL: Failed to fetch leaderboard rankings"
  exit 1
fi
echo "SUCCESS: Leaderboard fetched successfully. Top user count: $RANKING_COUNT"

# 7. Frontend CDN Check
echo "7. Checking Frontend CDN accessibility..."
FE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$STAGING_FE_URL")
if [ "$FE_STATUS" -ne 200 ]; then
  echo "FAIL: Staging Frontend is not accessible (HTTP $FE_STATUS)"
  exit 1
fi
echo "SUCCESS: Staging Frontend is active (HTTP 200)"

echo "=========================================================="
echo "SUCCESS: All staging E2E core integration tests passed!"
echo "=========================================================="
exit 0
