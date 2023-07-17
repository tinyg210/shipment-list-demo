#!/bin/sh

set -e

terraform init

terraform plan

terraform apply --auto-approve

exec "$@"