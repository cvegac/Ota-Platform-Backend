# Infraestructura como código — Plataforma OTA (AWS / CloudFormation)

Implementa el **recuadro superior de la Figura 8.1** del documento de tesis:
VPC con subred privada, Load Balancer, Auto Scaling Group de FrontEnd y de BackEnd,
RDS PostgreSQL con alias, y bucket de artefactos para los pipelines.

Región objetivo: **us-east-2** (misma que `ota-binaries-bucket`, AWS IoT Core y las Lambdas).

---

## 1. Qué corre dónde (estado analizado)

| Componente | Local (dev) | AWS (esta IaC) |
|---|---|---|
| Base de datos | Postgres en `postgres_setup/docker-compose.yml` | **RDS PostgreSQL** `ota-platform`, subred privada, `db.t4g.micro` |
| BackEnd | `mvn spring-boot:run` en `:8080` | ASG `ota-backend-asg` en subred privada, detrás del ALB |
| FrontEnd | `ng serve` en `:4200` → API en `localhost:8080` | ASG `ota-frontend-asg` (NGINX), detrás del ALB |
| Acceso | localhost | ALB `internet-facing`: `:80` web + `/api,/auth,...` → backend; `:8080` API directa |
| Firmware / S3 | — | `ota-binaries-bucket` (ya existe, solo se referencia en el IAM role) |
| IoT / Lambda / Step Functions | — | Ya desplegados a mano — **fuera del alcance de este stack** |

### Enrutamiento del ALB

- `http://<alb>/` → FrontEnd (NGINX / Angular)
- `http://<alb>/api/*`, `/auth/*`, `/platform/*`, `/iot/*`, `/s3/*`, `/lambda/*`, `/actuator/*` → BackEnd
- `http://<alb>:8080/*` → BackEnd puro (para vender como API)
- `environment.ts` de producción usa `API_URL = ''` → mismo host, sin CORS.

---

## 2. Prerrequisitos

- AWS CLI v2 configurada, permisos de admin para crear el stack.
- `jq` (lo usa el script de deploy).
- El bucket `ota-binaries-bucket` y los recursos de IoT/Lambda ya existen en us-east-2.

## 3. Desplegar el stack

```bash
export AWS_REGION=us-east-2
export STACK_NAME=ota-platform
./infra/scripts/deploy-stack.sh
```

Parámetros en `infra/cloudformation/parameters.example.json`. Opcionales:

- `KeyPairName` → vacío = acceso solo por **SSM Session Manager** (recomendado).
- `SSLCertificateArn` → ARN de un cert ACM para habilitar HTTPS (443).

Al terminar, el script imprime los **Outputs**: `WebUrl`, `ApiUrl`, `ArtifactsBucketName`,
`BackendAsgName`, `FrontendAsgName`, `DBAlias`, `DBEndpoint`, `DBSecretArn`.

## 4. Cargar el esquema de la base

El backend corre con `hibernate.ddl-auto=update`, así que crea las tablas solo.
Solo hay que crear los **schemas** una vez:

```bash
# desde una instancia del backend (SSM) o vía túnel al RDS
psql "host=db.ota.internal dbname=app_db user=<user> password=<pass>" \
  -f infra/db/schema.sql
```

`user`/`pass` salen del secret gestionado por RDS (`DBSecretArn` en los outputs).

## 5. Configurar los pipelines (GitHub Actions)

Secrets a cargar en **ambos** repos (`embedded` y `platform`):

| Secret | Valor |
|---|---|
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | usuario IAM *deployer* (permisos: `s3:PutObject` en el bucket de artefactos + `autoscaling:StartInstanceRefresh` / `DescribeInstanceRefreshes`) |
| `AWS_REGION` | `us-east-2` |
| `ARTIFACTS_BUCKET` | output `ArtifactsBucketName` |
| `BACKEND_ASG_NAME` | output `BackendAsgName` (solo repo `embedded`) |
| `FRONTEND_ASG_NAME` | output `FrontendAsgName` (solo repo `platform`) |

Flujo de cada pipeline (fiel a las figuras 10.14 y 10.16, adaptado a ASG):

```
push → build (Maven / ng build) → subir artefacto a S3 → start-instance-refresh → esperar Successful
```

Las instancias del ASG bajan el artefacto en su **UserData** al arrancar.

## 6. Estado del deploy (2026-08-27)

Stack `ota-platform` en us-east-2 — **CREATE/UPDATE_COMPLETE, todo healthy**:

| Check | Resultado |
|---|---|
| `http://<alb>/` (web) | 200 |
| `http://<alb>/actuator/health` (path routing) | 200 |
| `http://<alb>:8080/actuator/health` (API directa) | 200 `{"status":"UP"}` |
| `POST /auth/log-in` (admin/U2t4n6810) | 200 + JWT (RDS + seed + BCrypt + JWT OK) |
| SPA fallback (`/cualquier/ruta`) | 200 |
| RDS `ota-platform` | available, **PubliclyAccessible: false** |
| Stack `Degree` (IoT/Lambda/StepFunctions) | intacto, sin cambios |

Bugs resueltos durante el deploy:
- Backend crasheaba: faltaban schemas en la RDS → `src/main/resources/schema.sql` (`CREATE SCHEMA IF NOT EXISTS`).
- Health check daba 500: el catch-all `@ExceptionHandler(Exception.class)` convertía el 404 de
  `/actuator/health` en 500 → se agregó `spring-boot-starter-actuator` + handler 404 para
  `NoResourceFoundException`.

## 7. Deuda técnica / pendientes (documentado, no silenciado)

1. **Credenciales AWS filtradas**: `application.properties` tenía `access-key`/`secret-key`
   y la JWT key hardcodeadas y commiteadas. Ya se removieron del código, pero **hay que
   rotar la key `AKIA2UC3B3LZGUZGACPK` en IAM** — está en el historial de git y se usó para
   este deploy.
2. **Región del bucket**: `S3Service`/`LambdaService` hardcodean `ota-binaries-bucket`.
   Pasarlo a `@Value("${ota.binaries.bucket}")`.
3. Un solo NAT Gateway (sin HA entre AZ) para bajar costo. Para prod real: uno por AZ.
4. El catch-all de `GlobalExceptionHandler` todavía convierte `MethodNotSupported` (405) y
   `MethodArgumentNotValid` (400) en 500. Conviene mapearlos explícitamente.
5. Instancias `t3.small` (~1.9 GB). El backend arranca justo; si se agrega carga, subir a `t3.medium`.
6. Falta cargar los pipelines de GitHub Actions con los secrets (sección 5) para que el deploy
   sea por push y no por `deploy-all.sh` a mano.
