#!/usr/bin/env bash
set -Eeuo pipefail

readonly HEALTH_URL=http://127.0.0.1:8080/actuator/health

for attempt in $(seq 1 30); do
  if curl --fail --silent "$HEALTH_URL" >/dev/null; then
    echo "La comprobacion de salud fue exitosa."
    exit 0
  fi
  sleep 5
done

echo "La aplicacion no respondio en $HEALTH_URL." >&2
exit 1
