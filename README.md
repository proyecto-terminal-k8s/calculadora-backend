# Calculadora API

API REST de operaciones aritmeticas construida con Java 21 y Spring Boot. La aplicacion guarda cada peticion HTTP en un archivo de texto persistente.

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

## Logs

Un filtro registra metodo, ruta, estado HTTP y duracion en milisegundos por peticion en `logs/api-requests.txt`. El archivo es de texto plano y se rota diariamente a `api-requests.txt.YYYY-MM-DD`; el archivo activo conserva la ruta configurada mediante `REQUEST_LOG_FILE`.

En Kubernetes, monta un volumen persistente en el directorio que contiene el archivo y comparte ese volumen con el CronJob de respaldo. El CronJob envía a S3 el archivo rotado del día anterior y lo elimina solo después de confirmar que la carga terminó correctamente.

- Salud: `GET /actuator/health`
- Comprobacion sencilla para Kubernetes: `GET /health` (responde `{"status":"UP"}`)

## Documentacion OpenAPI

Springdoc genera la especificacion OpenAPI automaticamente a partir de los endpoints y sus anotaciones:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificacion JSON: <http://localhost:8080/v3/api-docs>

La documentacion interactiva describe las cuatro operaciones, sus parametros decimales, el formato de respuesta y el endpoint de salud.

## Ejecutar con Docker Compose

Docker compila el JAR en una etapa de build y monta `./logs` para conservar los logs fuera del contenedor:

```bash
docker compose up --build
```

- API: <http://localhost:8080>
- Logs: `./logs/api-requests.txt`

## Pruebas

```bash
./mvnw test
```

## Despliegue manual en EC2 con Amazon ECR y CodeDeploy

El workflow `Desplegar en EC2 con CodeDeploy` se ejecuta manualmente desde la rama `main` en GitHub Actions. Construye la imagen Docker, la publica en ECR con el SHA del commit como etiqueta, empaqueta `appspec.yml` y los scripts de despliegue, sube el bundle a S3 y espera el resultado de CodeDeploy. Si la nueva imagen no supera la comprobacion de salud, el script intenta restaurar la imagen anterior.

### Configuracion de GitHub

Configura estas variables del repositorio en **Settings > Secrets and variables > Actions > Variables**:

| Variable | Valor |
| --- | --- |
| `AWS_ROLE_ARN` | ARN del rol IAM que GitHub asumira mediante OIDC |
| `AWS_REGION` | Region AWS de ECR, S3 y CodeDeploy |
| `ECR_REPOSITORY` | Nombre de un repositorio ECR existente |
| `S3_BUCKET` | Bucket S3 existente para los bundles de CodeDeploy |
| `CODEDEPLOY_APPLICATION` | Nombre de la aplicacion CodeDeploy para EC2/on-premises |
| `CODEDEPLOY_DEPLOYMENT_GROUP` | Grupo de despliegue asociado a las instancias EC2 |

Configura el proveedor OIDC `token.actions.githubusercontent.com` en AWS con audiencia `sts.amazonaws.com`. Restringe la relacion de confianza del rol a `repo:proyecto-terminal-k8s/calculadora-backend:ref:refs/heads/main`. El rol necesita `ecr:GetAuthorizationToken` sobre `*`; `ecr:DescribeRepositories`, `ecr:BatchCheckLayerAvailability`, `ecr:CompleteLayerUpload`, `ecr:InitiateLayerUpload`, `ecr:PutImage` y `ecr:UploadLayerPart` sobre el repositorio; `s3:PutObject` sobre el prefijo `releases/` del bucket; y `codedeploy:CreateDeployment`, `codedeploy:GetApplication`, `codedeploy:GetDeployment`, `codedeploy:GetDeploymentConfig` y `codedeploy:GetDeploymentGroup` para la aplicacion y el grupo de despliegue. El repositorio ECR y el bucket S3 deben existir antes de ejecutar el workflow, en la region configurada.

### Preparacion de las instancias EC2

- Registra las instancias en el grupo de despliegue y manten activo el agente CodeDeploy.
- Instala Docker, AWS CLI y `curl` en las instancias, y habilita el servicio Docker al iniciar el sistema.
- Asigna un perfil de instancia con `ecr:GetAuthorizationToken` sobre `*` y permisos `ecr:BatchCheckLayerAvailability`, `ecr:BatchGetImage` y `ecr:GetDownloadUrlForLayer` sobre el repositorio, ademas de `s3:GetObject` sobre el prefijo `releases/` del bucket para el agente CodeDeploy.
- Permite el puerto TCP 8080 segun las necesidades de red de la aplicacion.

El despliegue inicia un contenedor llamado `calculadora`, publica el puerto 8080 y valida `http://127.0.0.1:8080/actuator/health`. Reserva ese puerto en las instancias objetivo para esta aplicacion.
