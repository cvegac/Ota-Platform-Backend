#!/usr/bin/env bash
# ===========================================================================
# Deploy completo de la plataforma OTA en AWS (us-east-2).
#   - Crea/actualiza el stack CloudFormation `ota-platform`
#   - Compila backend (jar) y frontend (dist.zip)
#   - Sube los artefactos al bucket de artefactos
#   - Dispara instance refresh de los dos ASG
#   - Verifica health de targets, ALB, RDS
#
# NO toca el stack `Degree` (Lambdas, Step Functions, IoT, ota-binaries-bucket).
#
# Uso:
#   export AWS_ACCESS_KEY_ID=...
#   export AWS_SECRET_ACCESS_KEY=...
#   export AWS_DEFAULT_REGION=us-east-2
#   bash infra/scripts/deploy-all.sh
#
# Requisitos: aws CLI, JDK 21 (JAVA_HOME), node 22, jq, zip
# ===========================================================================
set -euo pipefail

STACK_NAME="${STACK_NAME:-ota-platform}"
REGION="${AWS_DEFAULT_REGION:-us-east-2}"
# pwd -W => ruta estilo Windows (D:/...), que es la que entiende el aws CLI de Windows.
REPO_ROOT="$(cd "$(dirname "$0")/../.." && { pwd -W 2>/dev/null || pwd; })"
PLATFORM_REPO="${PLATFORM_REPO:-$(cd "$REPO_ROOT/../platform" && { pwd -W 2>/dev/null || pwd; })}"
TEMPLATE="$REPO_ROOT/infra/cloudformation/platform.yaml"

# aws CLI: usa el binario si esta en PATH, si no cae a `python -m awscli` (instalado via pip).
# 'command' es clave: sin el, la funcion aws() se llamaria a si misma en loop (segfault).
if command -v aws >/dev/null; then _AWS=(command aws); else _AWS=(python -m awscli); fi
aws() { "${_AWS[@]}" --region "$REGION" "$@"; }
say() { printf '\n\033[1;36m>> %s\033[0m\n' "$*"; }

# --------------------------------------------------------------------------
say "1/6  Validando identidad y template"
aws sts get-caller-identity
aws cloudformation validate-template --template-body "file://$TEMPLATE" >/dev/null
echo "template OK"

# --------------------------------------------------------------------------
say "2/6  Desplegando stack $STACK_NAME (RDS + NAT + ALB, ~15-20 min)"
aws cloudformation deploy \
  --stack-name "$STACK_NAME" \
  --template-file "$TEMPLATE" \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-fail-on-empty-changeset \
  --parameter-overrides EnvironmentName=ota

get_out() { aws cloudformation describe-stacks --stack-name "$STACK_NAME" \
  --query "Stacks[0].Outputs[?OutputKey=='$1'].OutputValue" --output text; }

ARTIFACTS_BUCKET="$(get_out ArtifactsBucketName)"
BACKEND_ASG="$(get_out BackendAsgName)"
FRONTEND_ASG="$(get_out FrontendAsgName)"
ALB_DNS="$(get_out AlbDnsName)"
DB_ENDPOINT="$(get_out DBEndpoint)"
echo "ArtifactsBucket=$ARTIFACTS_BUCKET"
echo "BackendASG=$BACKEND_ASG  FrontendASG=$FRONTEND_ASG"
echo "ALB=$ALB_DNS  DB=$DB_ENDPOINT"

# --------------------------------------------------------------------------
say "3/6  Compilando artefactos"
cd "$REPO_ROOT"

# --- localizar un JDK 21 ---
jdk_ok() { [ -n "${1:-}" ] && [ -x "$1/bin/java" ] && "$1/bin/java" -version 2>&1 | grep -qE '"2[1-9]'; }
if ! jdk_ok "${JAVA_HOME:-}"; then
  JAVA_HOME=""
  for d in "/c/Program Files/Amazon Corretto"/jdk2[1-9]* \
           "/c/Program Files/Java"/jdk-2[1-9]* \
           "/c/Program Files/Eclipse Adoptium"/jdk-2[1-9]* ; do
    if jdk_ok "$d"; then JAVA_HOME="$d"; break; fi
  done
fi
if ! jdk_ok "${JAVA_HOME:-}"; then
  echo "ERROR: no encuentro un JDK 21. Instala 'choco install -y corretto21jdk'"
  echo "       o exporta JAVA_HOME apuntando a un JDK 21 y volve a correr."
  exit 1
fi
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
echo "JAVA_HOME=$JAVA_HOME"
java -version

./mvnw -B clean package -DskipTests
JAR="$(ls target/*.jar | grep -v original | head -n1)"
echo "backend jar: $JAR"

cd "$PLATFORM_REPO"
npm ci
npx ng build --configuration production
rm -f dist.zip
if command -v zip >/dev/null; then
  ( cd dist && zip -qr ../dist.zip . )
else
  powershell -NoProfile -Command "Compress-Archive -Path '$PLATFORM_REPO/dist/*' -DestinationPath '$PLATFORM_REPO/dist.zip' -Force"
fi
echo "frontend zip: $PLATFORM_REPO/dist.zip"

# --------------------------------------------------------------------------
say "4/6  Subiendo artefactos a s3://$ARTIFACTS_BUCKET"
aws s3 cp "$REPO_ROOT/$JAR" "s3://$ARTIFACTS_BUCKET/backend/app.jar"
aws s3 cp "$PLATFORM_REPO/dist.zip" "s3://$ARTIFACTS_BUCKET/frontend/dist.zip"

# --------------------------------------------------------------------------
say "5/6  Instance refresh de los ASG"
for ASG in "$BACKEND_ASG" "$FRONTEND_ASG"; do
  # cancelar cualquier refresh en curso (p.ej. uno anterior que quedo esperando health)
  cur="$(aws autoscaling describe-instance-refreshes --auto-scaling-group-name "$ASG" \
        --query 'InstanceRefreshes[0].Status' --output text 2>/dev/null || true)"
  if [ "$cur" = "InProgress" ] || [ "$cur" = "Pending" ]; then
    echo "  cancelando refresh previo de $ASG ($cur)"
    aws autoscaling cancel-instance-refresh --auto-scaling-group-name "$ASG" >/dev/null || true
    sleep 20
  fi
  aws autoscaling start-instance-refresh --auto-scaling-group-name "$ASG" \
    --preferences '{"MinHealthyPercentage":0,"InstanceWarmup":240}'
done

wait_refresh() {
  local asg="$1"
  for i in $(seq 1 40); do
    local s
    s="$(aws autoscaling describe-instance-refreshes --auto-scaling-group-name "$asg" \
        --query 'InstanceRefreshes[0].Status' --output text)"
    echo "  $asg: $s"
    case "$s" in Successful) return 0;; Failed|Cancelled) return 1;; esac
    sleep 30
  done
  return 1
}
wait_refresh "$BACKEND_ASG"
wait_refresh "$FRONTEND_ASG"

# --------------------------------------------------------------------------
say "6/6  Verificacion"
BTG="$(aws elbv2 describe-target-groups --names ota-backend-tg  --query 'TargetGroups[0].TargetGroupArn' --output text)"
FTG="$(aws elbv2 describe-target-groups --names ota-frontend-tg --query 'TargetGroups[0].TargetGroupArn' --output text)"
echo "backend targets:";  aws elbv2 describe-target-health --target-group-arn "$BTG" --query 'TargetHealthDescriptions[].TargetHealth.State' --output text
echo "frontend targets:"; aws elbv2 describe-target-health --target-group-arn "$FTG" --query 'TargetHealthDescriptions[].TargetHealth.State' --output text
aws rds describe-db-instances --db-instance-identifier ota-platform --query 'DBInstances[0].DBInstanceStatus' --output text

echo
echo "Web:  http://$ALB_DNS/"
echo "API:  http://$ALB_DNS:8080/"
curl -sS -o /dev/null -w "  web  -> HTTP %{http_code}\n" "http://$ALB_DNS/"           || true
curl -sS -o /dev/null -w "  api  -> HTTP %{http_code}\n" "http://$ALB_DNS:8080/auth/log-in" || true
echo
echo "Listo. Para bajar todo: aws cloudformation delete-stack --stack-name $STACK_NAME"
