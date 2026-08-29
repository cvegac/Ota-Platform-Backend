#!/usr/bin/env bash
# Despliega / actualiza el stack de infraestructura de la plataforma OTA.
#
#   AWS_REGION=us-east-2 ./infra/scripts/deploy-stack.sh
#
set -euo pipefail

STACK_NAME="${STACK_NAME:-ota-platform}"
AWS_REGION="${AWS_REGION:-us-east-2}"
TEMPLATE="$(dirname "$0")/../cloudformation/platform.yaml"
PARAMS="$(dirname "$0")/../cloudformation/parameters.example.json"

echo ">> Validando template..."
aws cloudformation validate-template \
  --region "$AWS_REGION" \
  --template-body "file://$TEMPLATE" >/dev/null

echo ">> Desplegando stack '$STACK_NAME' en $AWS_REGION..."
aws cloudformation deploy \
  --region "$AWS_REGION" \
  --stack-name "$STACK_NAME" \
  --template-file "$TEMPLATE" \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides "$(jq -r '.[] | "\(.ParameterKey)=\(.ParameterValue)"' "$PARAMS" | tr '\n' ' ')"

echo ">> Outputs:"
aws cloudformation describe-stacks \
  --region "$AWS_REGION" \
  --stack-name "$STACK_NAME" \
  --query 'Stacks[0].Outputs' \
  --output table
