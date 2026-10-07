# Calculadora API

API REST de operaciones aritmeticas construida con Java 21 y Spring Boot. La aplicacion registra las peticiones en la salida estandar, expone metricas para Prometheus y proporciona una configuracion local de Grafana.

## Ejecutar localmente

```bash
./mvnw spring-boot:run
```

Todos los endpoints reciben `numero1` y `numero2` como parametros de consulta y devuelven JSON con la operacion, los operandos y el resultado:

| Operacion | Ejemplo |
| --- | --- |
| Sumar | `GET /api/v1/calculadora/sumar?numero1=2&numero2=3` |
| Restar | `GET /api/v1/calculadora/restar?numero1=5&numero2=2` |
| Multiplicar | `GET /api/v1/calculadora/multiplicar?numero1=4&numero2=3` |
| Dividir | `GET /api/v1/calculadora/dividir?numero1=10&numero2=4` |

Ejemplo de respuesta:

```json
{
  "operacion": "sumar",
  "numero1": 2,
  "numero2": 3,
  "resultado": 5
}
```

La division usa precision decimal `DECIMAL128`. Dividir entre cero devuelve HTTP 400 con un error Problem Details. Los parametros faltantes o no numericos tambien son rechazados por Spring MVC con HTTP 400.

## Logs y metricas

Un filtro registra metodo, ruta, estado HTTP y duracion en milisegundos por peticion. Cada operacion tambien registra sus operandos y resultado. Los logs se escriben en consola (stdout/stderr), de modo que Docker y Kubernetes pueden recolectarlos sin que la aplicacion dependa de un backend de almacenamiento. La exportacion/retencion o envio a S3 queda para una practica posterior.

- Salud: `GET /actuator/health`
- Comprobacion sencilla para Kubernetes: `GET /health` (responde `{"status":"UP"}`)
- Prometheus: `GET /actuator/prometheus`

## Documentacion OpenAPI

Springdoc genera la especificacion OpenAPI automaticamente a partir de los endpoints y sus anotaciones:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificacion JSON: <http://localhost:8080/v3/api-docs>

La documentacion interactiva describe las cuatro operaciones, sus parametros decimales, el formato de respuesta y el endpoint de salud.

## Prometheus y Grafana con Docker Compose

Levanta la API y el stack de observabilidad (Docker compila el JAR en una etapa de build):

```bash
docker compose up --build
```

- API: <http://localhost:8080>
- Prometheus: <http://localhost:9090>
- Grafana: <http://localhost:3000> (usuario y contrasena iniciales: `admin` / `admin`)

Prometheus consulta la API cada 15 segundos. Grafana aprovisiona la fuente de datos Prometheus y un dashboard con tasa de peticiones, latencia p95 y respuestas agrupadas por codigo HTTP. Los datos de Prometheus y Grafana se guardan en volumenes Docker.

## Pruebas

```bash
./mvnw test
```
