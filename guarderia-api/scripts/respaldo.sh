#!/bin/sh
# Respaldo del servidor compartido (proyecto codea-prod): la base de Aiven y las fotos del almacén.
# Se corre en la Mac que sirve codeaapp.site, desde cualquier carpeta:
#
#   guarderia-api/scripts/respaldo.sh
#
# Deja dos archivos en ~/respaldos-codea (o RESPALDOS_DIR):
#   codea_prod-diario-AAAAMMDD-HHMM.dump  la base (pg_dump en formato custom; se restaura con pg_restore)
#   fotos-diario-AAAAMMDD-HHMM.tar.gz     el volumen del almacén (codea-prod_media)
# y borra los "-diario-" de más de RESPALDOS_DIAS días (14 por omisión). Los respaldos hechos a mano no se tocan.
# La llave de la base sale de .env.prod y no se escribe en ningún lado. Cómo restaurar: README.md, "Respaldos".
set -eu

API_DIR=$(cd "$(dirname "$0")/.." && pwd)
DESTINO="${RESPALDOS_DIR:-$HOME/respaldos-codea}"
DIAS="${RESPALDOS_DIAS:-14}"
FECHA=$(date +%Y%m%d-%H%M)
BASE="$DESTINO/codea_prod-diario-$FECHA.dump"
FOTOS="$DESTINO/fotos-diario-$FECHA.tar.gz"
ALMACEN=codea-prod-storage-1
# la misma versión mayor que Aiven (18): un pg_dump más viejo no puede respaldar una base más nueva.
IMAGEN=postgres:18

umask 077
mkdir -p "$DESTINO"
chmod 700 "$DESTINO"
log() { echo "$(date '+%Y-%m-%d %H:%M:%S') $*" | tee -a "$DESTINO/respaldo.log"; }

# 1. la base. DATABASE_URL viene para SQLAlchemy (postgresql+asyncpg://...?ssl=require); pg_dump la quiere en el
# formato de libpq (postgresql://...?sslmode=require). Se pasa al contenedor por variable de entorno, no como
# argumento, para que no aparezca en la lista de procesos.
CODEA_DB_URL=$(grep '^DATABASE_URL=' "$API_DIR/.env.prod" | cut -d= -f2- | sed -e 's/+asyncpg//' -e 's/ssl=/sslmode=/')
if [ -z "$CODEA_DB_URL" ]; then
  log "ERROR: no hay DATABASE_URL en .env.prod"
  exit 1
fi
export CODEA_DB_URL
docker run --rm -e CODEA_DB_URL "$IMAGEN" sh -c 'exec pg_dump -Fc --no-owner --no-privileges -d "$CODEA_DB_URL"' > "$BASE.tmp"
# se comprueba que el archivo se pueda leer y traiga tablas antes de darlo por bueno.
TABLAS=$(docker run --rm -i "$IMAGEN" pg_restore --list < "$BASE.tmp" | grep -c ' TABLE DATA ' || true)
if [ "$TABLAS" -lt 1 ]; then
  rm -f "$BASE.tmp"
  log "ERROR: el respaldo de la base salió vacío"
  exit 1
fi
mv "$BASE.tmp" "$BASE"
log "base: $(basename "$BASE") ($(du -h "$BASE" | cut -f1), $TABLAS tablas con datos)"

# 2. las fotos. Se pausa el almacén unos segundos para copiar el volumen sin escrituras a medias;
# si algo falla, el trap lo vuelve a encender.
reanudar() { docker unpause "$ALMACEN" >/dev/null 2>&1 || true; }
trap reanudar EXIT
docker pause "$ALMACEN" >/dev/null
docker run --rm -v codea-prod_media:/data:ro "$IMAGEN" tar czf - -C /data . > "$FOTOS.tmp"
reanudar
trap - EXIT
mv "$FOTOS.tmp" "$FOTOS"
log "fotos: $(basename "$FOTOS") ($(du -h "$FOTOS" | cut -f1))"

# 3. se borran los respaldos diarios viejos.
BORRADOS=$(find "$DESTINO" -maxdepth 1 \( -name 'codea_prod-diario-*.dump' -o -name 'fotos-diario-*.tar.gz' \) -mtime +"$DIAS" -print -delete | wc -l | tr -d ' ')
log "listo; se borraron $BORRADOS respaldos de más de $DIAS días"
