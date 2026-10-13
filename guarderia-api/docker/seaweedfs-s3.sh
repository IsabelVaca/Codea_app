#!/bin/sh
# Arranca SeaweedFS con su API compatible con S3, usando las llaves de .env.
# Solo para desarrollo: en producción va S3 de AWS u otro servidor compatible.
set -e
cat > /tmp/s3.json <<JSON
{"identities": [{"name": "api", "credentials": [{"accessKey": "$S3_ACCESS_KEY", "secretKey": "$S3_SECRET_KEY"}], "actions": ["Admin", "Read", "List", "Tagging", "Write"]}]}
JSON
exec weed server -dir=/data -s3 -s3.port=9000 -s3.config=/tmp/s3.json
