#!/bin/sh
# Arranca SeaweedFS con su API compatible con S3, usando las llaves de .env.
# Lo usan el compose de desarrollo y el servidor compartido (codea-prod, que guarda aquí las fotos de verdad).
#
# -volume.max=100: cuántos volúmenes puede tener. Con el valor por omisión (8) se llenaba: 7 los apartó para una
#   colección que no se usa y al bucket le tocó uno solo, y cada 15 minutos salía "only 0 volumes left".
# -master.volumeSizeLimitMB=1024: cada volumen llega hasta 1 GB (por omisión 30 GB) antes de abrir otro.
set -e
cat > /tmp/s3.json <<JSON
{"identities": [{"name": "api", "credentials": [{"accessKey": "$S3_ACCESS_KEY", "secretKey": "$S3_SECRET_KEY"}], "actions": ["Admin", "Read", "List", "Tagging", "Write"]}]}
JSON
exec weed server -dir=/data -volume.max=100 -master.volumeSizeLimitMB=1024 \
  -s3 -s3.port=9000 -s3.config=/tmp/s3.json
