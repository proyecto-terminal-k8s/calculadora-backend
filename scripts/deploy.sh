#!/usr/bin/env bash
set -Eeuo pipefail

readonly APP_NAME=calculadora
readonly APP_DIRECTORY=/opt/calculadora
readonly HEALTH_URL=http://127.0.0.1:8080/actuator/health

for command in aws curl docker; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "Falta el comando requerido en la instancia: $command" >&2
    exit 1
  fi
done

if [[ ! -s "$APP_DIRECTORY/image-uri.txt" || ! -s "$APP_DIRECTORY/aws-region.txt" ]]; then
  echo "No se encontro la URI de la imagen o la region AWS del bundle." >&2
  exit 1
fi

image_uri="$(<"$APP_DIRECTORY/image-uri.txt")"
aws_region="$(<"$APP_DIRECTORY/aws-region.txt")"
if [[ -z "$image_uri" || -z "$aws_region" ]]; then
  echo "La URI de la imagen y la region AWS no pueden estar vacias." >&2
  exit 1
fi

registry="${image_uri%%/*}"
if [[ "$registry" == "$image_uri" ]]; then
  echo "La URI de imagen ECR no es valida: $image_uri" >&2
  exit 1
fi

aws ecr get-login-password --region "$aws_region" \
  | docker login --username AWS --password-stdin "$registry"
docker pull "$image_uri"

previous_image=""
if docker inspect "$APP_NAME" >/dev/null 2>&1; then
  previous_image="$(docker inspect --format '{{.Config.Image}}' "$APP_NAME")"
  docker rm --force "$APP_NAME"
fi

wait_for_health() {
  local attempt
  for attempt in $(seq 1 60); do
    if curl --fail --silent "$HEALTH_URL" >/dev/null; then
      return 0
    fi
    sleep 5
  done
  return 1
}

restore_previous_image() {
  if [[ -z "$previous_image" ]]; then
    echo "No habia una version anterior que restaurar." >&2
    return 0
  fi

  echo "Restaurando la imagen anterior: $previous_image" >&2
  if ! docker run --detach \
    --name "$APP_NAME" \
    --restart unless-stopped \
    --publish 8080:8080 \
    "$previous_image"; then
    echo "No se pudo iniciar la imagen anterior." >&2
    return 1
  fi

  if ! wait_for_health; then
    echo "La imagen anterior tampoco respondio en $HEALTH_URL." >&2
    return 1
  fi

  echo "La imagen anterior se restauro correctamente."
}

if ! docker run --detach \
  --name "$APP_NAME" \
  --restart unless-stopped \
  --publish 8080:8080 \
  "$image_uri"; then
  echo "No se pudo iniciar la nueva imagen." >&2
  restore_previous_image || echo "No se pudo completar la restauracion." >&2
  exit 1
fi

if wait_for_health; then
  echo "La aplicacion quedo saludable con la imagen $image_uri."
  exit 0
fi

echo "La nueva imagen no paso la comprobacion de salud." >&2
docker rm --force "$APP_NAME"
restore_previous_image || echo "No se pudo completar la restauracion." >&2

exit 1
