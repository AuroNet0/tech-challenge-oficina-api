#!/bin/sh
set -eu

JAVA_OPTS="${JAVA_OPTS:-}"
NEW_RELIC_ENABLED_NORMALIZED="$(printf '%s' "${NEW_RELIC_ENABLED:-false}" | tr '[:upper:]' '[:lower:]')"

if [ "$NEW_RELIC_ENABLED_NORMALIZED" = "true" ]; then
  if [ -n "${NEW_RELIC_LICENSE_KEY:-}" ]; then
    NEW_RELIC_APP_NAME="${NEW_RELIC_APP_NAME:-tech-challenge-oficina-api}"
    JAVA_OPTS="$JAVA_OPTS -javaagent:/app/newrelic/newrelic-agent.jar"
    JAVA_OPTS="$JAVA_OPTS -Dnewrelic.config.license_key=${NEW_RELIC_LICENSE_KEY}"
    JAVA_OPTS="$JAVA_OPTS -Dnewrelic.config.app_name=${NEW_RELIC_APP_NAME}"
    JAVA_OPTS="$JAVA_OPTS -Dnewrelic.config.agent_enabled=true"
  else
    echo "NEW_RELIC_ENABLED=true but NEW_RELIC_LICENSE_KEY is not set; starting without New Relic agent." >&2
  fi
fi

exec java $JAVA_OPTS -jar app.jar "$@"
