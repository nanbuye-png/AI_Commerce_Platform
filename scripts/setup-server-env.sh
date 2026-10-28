#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
if [[ -e .env ]]; then
  echo 'A .env file already exists; refusing to overwrite it.' >&2
  exit 1
fi

postgres_password=$(openssl rand -hex 24)
internal_token=$(openssl rand -hex 32)
customer_secret=$(openssl rand -hex 32)
merchant_secret=$(openssl rand -hex 32)
admin_secret=$(openssl rand -hex 32)
admin_password=$(openssl rand -hex 20)

umask 077
{
  printf 'POSTGRES_PASSWORD=%s\n' "$postgres_password"
  printf 'AI_INTERNAL_API_TOKEN=%s\n' "$internal_token"
  printf 'JWT_CUSTOMER_WEB_SECRET=%s\n' "$customer_secret"
  printf 'JWT_MERCHANT_WEB_SECRET=%s\n' "$merchant_secret"
  printf 'JWT_ADMIN_WEB_SECRET=%s\n' "$admin_secret"
  printf 'ADMIN_USERNAME=admin\n'
  printf 'ADMIN_PASSWORD=%s\n' "$admin_password"
  printf 'ADMIN_EMAIL=admin@example.invalid\n'
  printf 'AI_LLM_PROVIDER=mock\n'
} > .env
chmod 600 .env

echo 'Created .env with unique credentials; admin username: admin.'
echo "On the server, use grep '^ADMIN_PASSWORD=' .env to view the password privately."
