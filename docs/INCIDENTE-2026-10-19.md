# Incidente del 19 de octubre: cuentas que no se activaban, servidores mezclados y datos borrados

Qué pasó la noche del 19/oct (hora de Monterrey), por qué pasó, cómo se corrigió y qué falta para que no se repita.
Resumen: **el código de activación no estaba roto**. Se mezclaron dos servidores (el local de la Mac y el compartido
`codeaapp.site`), un APK quedó apuntando a `localhost` y el seed de prueba borró los datos que había en producción.

## Resumen

| # | Síntoma | Causa | Estado |
|---|---|---|---|
| 1 | La checada de Paola Sánchez no aparecía en el admin | La lista de docentes solo se recargaba con cambios hechos desde el mismo teléfono | ✅ Corregido (`c523cdc`) |
| 2 | Las fotos de los avisos no salían en el emulador | El servidor local tenía código viejo y la base sin migrar | ✅ Corregido (servidor local) |
| 3 | «El código no es válido» al activar cuentas nuevas | Las cuentas se dieron de alta en el servidor local y se activaron en el compartido | ✅ Explicado; hay que volver a dar de alta las cuentas |
| 4 | La tablet no se conectaba | El APK apuntaba a `localhost`, que en la tablet es la propia tablet | ✅ Corregido (APK a la API) |
| 5 | Desaparecieron cuentas, fotos y avisos de producción | `seed_demo.py --borrar-todo` se corrió sobre la base compartida | ⚠️ Sin respaldo: no se pueden recuperar |
| 6 | Aviso «only 0 volumes left» del almacén de fotos | SeaweedFS con tope de 8 volúmenes, los 8 ocupados | ⚠️ Pendiente |

## Los dos servidores

| | Servidor local | Servidor compartido |
|---|---|---|
| Dónde | Docker en la Mac, proyecto `guarderia-api` | Docker en la Mac, proyecto `codea-prod`, con túnel de Cloudflare |
| Dirección | `http://localhost:8000` (el emulador llega con `adb reverse`) | `https://api.codeaapp.site/api/v1/` |
| Base | Postgres local del contenedor `db` | Aiven, base `codea_prod` (Postgres 18) |
| Fotos | SeaweedFS local, puerto 9000 | SeaweedFS de `codea-prod`, `archivos.codeaapp.site` |
| Contraseña de las cuentas de prueba | `codea1234` | La del grupo (no está guardada en ningún archivo) |

La app toma la dirección de `API_BASE_URL` en `local.properties` (o de `-PapiBaseUrl` al compilar). Android Studio usa
`local.properties`, y ahí decía `http://localhost:8000/api/v1/`: todo lo que se instalaba desde Android Studio iba al
servidor local.

## Cronología

| Hora | Qué pasó |
|---|---|
| 21:54 | Se agrega `seed_demo.py` al repo (commit `d36fb17`). Esa noche, antes de las 22:10, se corre con `--borrar-todo` sobre `codea_prod`: vacía las tablas, reinicia los IDs y siembra 22 cuentas `@oruga.test`. La hora exacta no quedó registrada. |
| ~22:00 | Se reporta que la checada de Paola no aparece en el admin. |
| 22:10 en adelante | Varios intentos fallidos de entrar al servidor compartido con cuentas del seed (Paola, Laura, Héctor, Karla Vega): no se tiene la contraseña del grupo. |
| 22:30 | En el emulador (servidor local) se publican avisos con imagen y la imagen no sale. |
| 22:37 | Desde el emulador (servidor **local**) se da de alta a una docente con correo `@tec.mx`. |
| 22:39 y 22:42 | Desde la tablet (servidor **compartido**) se intenta activar esa cuenta: 403 «El código no es válido». |
| 22:45 | Desde el emulador (local) se da de alta a otra docente `@tec.mx`. |
| 22:46 | Desde la tablet (compartido) se intenta activar: 403. |
| 22:54 | Se instala en la tablet un APK desde Android Studio que apunta a `localhost`: la tablet deja de conectarse. |
| ~23:00 | Se despliega en el servidor compartido un endurecimiento de la activación (ver 3). |
| 23:07 | Se genera un código de acceso para la docente de prueba que se compartirá con un developer. |
| 23:14 | Se genera un código de acceso para el admin (al usarlo, el admin queda con una contraseña nueva). |

## 1. La checada no aparecía en el admin

- **Causa:** `DocentesAdminViewModel` solo volvía a pedir la lista cuando llegaba un `DataEvents.changed`, y eso solo pasa
  con cambios hechos desde el mismo teléfono. La docente checa desde el suyo, así que el admin no veía la hora hasta
  reiniciar la app.
- **Corrección** (`c523cdc`): la lista se vuelve a pedir al abrir la pestaña Docentes y se puede jalar hacia abajo para
  recargar. Quien no ha checado dice «Sin checar» (antes «Checó --:--»).
- En el mismo cambio, a pedido del usuario: Centro pasó a ser **Avisos**, las salas pasaron a **Docentes → Salas** y se
  quitaron los sobretítulos de las pantallas (después también la tarjeta de resumen, `c06888f`).

## 2. Las fotos de los avisos no salían en el emulador

- **Causa:** el servidor local llevaba 26 h corriendo con código viejo (`uvicorn --reload` no detectó los cambios del
  montaje en la Mac) y su base seguía en la migración `a96b2c7d5f65`, sin la columna `announcement.media_id`
  (migración `c63f9e5b3d04`). El aviso se guardaba pero la imagen se perdía.
- **Corrección:** `alembic upgrade head` en la base local (hasta `d74a0f6c4e15`) y reinicio del contenedor `api` local.
  Los avisos publicados antes quedaron sin imagen.
- Además, a pedido del usuario (`7cb17ab`): la imagen se ve completa, con su relación de aspecto y un tope de 320 dp
  de alto, también en la vista previa al publicar.

## 3. «El código no es válido»

- **Lo que se revisó y estaba bien:** las 38 pruebas de activación e invitaciones; un recorrido completo contra el
  servidor local (alta, código en minúsculas y sin guion, entrar, código nuevo del admin); el código del contenedor de
  producción era idéntico al del repo; la app mandaba bien el correo y el código; el límite de intentos responde 429,
  no 403.
- **Causa real** (vista en la base compartida con consultas de solo lectura y en la base local): las dos cuentas
  `@tec.mx` existen **solo en el servidor local**. En `codea_prod` no hay ninguna cuenta fuera de las 22 del seed, así
  que no había invitación para ese correo y la API respondió `CODE_INVALID`. El registro de la auditoría de producción
  tiene los tres `ACTIVATION_FAILED` de esos correos.
- **Antes de dar con la causa** se desplegó un endurecimiento que no la resolvía, pero se dejó porque no hace daño:
  - API: `normalize_code` se queda solo con letras y números; `rules.clean_email` quita espacios y caracteres invisibles
    (los que puede traer un texto copiado de WhatsApp) en el alta, el login y la activación. Dos pruebas nuevas en
    `test_auth.py`; 338 pruebas en verde.
  - App: el campo del código guarda solo letras y números y pone el guion solo al escribir (`Invite.cleanCode`,
    `CodeDashTransformation`); teclado en mayúsculas y sin autocorrección. Tres pruebas nuevas en `InviteTest`; 138 en
    verde.
  - **Ojo:** ese cambio se desplegó en producción (solo el contenedor `api`) **antes de hacer commit**. Hay que hacer el
    commit para que el repo y el servidor coincidan.
- **Qué falta:** volver a dar de alta las dos cuentas `@tec.mx` desde un admin que apunte al servidor compartido.

## 4. La tablet no se conectaba

- **Causa:** el APK instalado a las 22:54 se compiló con `API_BASE_URL=http://localhost:8000/api/v1/`. En un
  dispositivo físico `localhost` es el propio dispositivo, y la tablet no tenía `adb reverse`. El log decía
  `Failed to connect to localhost/127.0.0.1:8000`.
- **Corrección:** se le instaló un APK compilado con
  `-PapiBaseUrl=https://api.codeaapp.site/api/v1/ --no-configuration-cache`, y en la Mac se cambió `local.properties` a
  `API_BASE_URL=https://api.codeaapp.site/api/v1/`, así que lo que se instale desde Android Studio ya va a la API.

## 5. El seed borró los datos de producción

- `seed_demo.py --borrar-todo` hace `TRUNCATE ... RESTART IDENTITY CASCADE` de todas las tablas y borra las fotos del
  bucket antes de sembrar.
- Los contadores de Postgres (`pg_stat_user_tables`) muestran lo que había antes: `app_user` tuvo 38 inserciones y
  quedan 22 (**16 cuentas perdidas**: las del primer seed, `app/seed.py`, y las que se dieron de alta desde la app),
  `media` tuvo 6 y quedan 0 (**6 fotos**), `announcement` tuvo 8 y quedan 5 (**3 avisos** publicados desde la app). La
  docente que se editó ese mismo día desde el admin ya no existe.
- **No hay respaldo:** no se encontró ningún `pg_dump` en la Mac ni en el repo, y no hay respaldos documentados. No se
  puede saber qué cuentas eran ni recuperarlas.
- **Estado actual de `codea_prod`:** solo las 22 cuentas del seed (1 admin, 8 docentes con una dada de baja, 13 padres),
  todas activadas con la contraseña del grupo. 10 MB, Postgres 18.6, máximo 20 conexiones.

## 6. Almacén de fotos al tope de volúmenes

- SeaweedFS (`weed server`) arranca con un máximo de 8 volúmenes. Los 8 están ocupados: 7 reservados para una colección
  que no se usa y **uno solo** para el bucket `guarderia`. Cada 15 minutos registra «volume grow request failed: only 0
  volumes left». Las fotos se suben, pero si ese volumen se llena o queda de solo lectura, las subidas empiezan a fallar.
- **Pendiente:** agregar `-volume.max=100 -master.volumeSizeLimitMB=1024` en `guarderia-api/docker/seaweedfs-s3.sh` y
  reiniciar solo el contenedor `storage` de `codea-prod`.

## Lo que se cambió en el servidor compartido

Todo lo demás fueron consultas de solo lectura (transacción `READ ONLY`).

- Se reconstruyó y reinició **solo** el contenedor `api` (`up -d --build --no-deps api`) con el endurecimiento del punto 3.
  La base, el almacén y el túnel no se tocaron.
- Se generaron dos códigos de acceso con la misma función que usa el admin (quedan en la auditoría como
  `ACCESS_CODE_ISSUED`): uno para la docente de prueba que se comparte con un developer y uno para el admin. Los
  códigos no se escriben aquí porque el repo es público.

## Decisiones

- **Desde el 19/oct todos trabajan contra el servidor compartido**, nunca contra `localhost`. El servidor local solo
  sirve para trabajar en el backend y correr `pytest`.
- Para eso se agregó `guarderia-api/.env.dev`: configuración del backend local sin secretos reales (ninguno coincide con
  producción). La base de Aiven, el token del túnel, las llaves del almacén y la llave JWT de producción solo viven en
  `.env.prod` de la Mac servidor.

## Pendientes para que no se repita

1. **Commit y push** del endurecimiento de la activación, `.env.dev` y este documento (producción ya corre ese código).
2. **Respaldos:** un `pg_dump` ahora y uno diario de `codea_prod` (con la imagen `postgres:18`: el `pg_dump` 16 del
   contenedor `db` no puede respaldar Postgres 18), junto con una copia del volumen `codea-prod_media`.
3. **Candado al seed:** que `seed_demo.py --borrar-todo` se niegue fuera de desarrollo salvo que se escriba el nombre de
   la base, y que saque un respaldo antes.
4. **Almacén:** subir el límite de volúmenes de SeaweedFS (punto 6).
5. **Que todo apunte a la API:** cambiar el valor por omisión de `API_BASE_URL` en `app/build.gradle.kts` (hoy
   `10.0.2.2`), apagar el servidor local y quitar de las guías los pasos de `adb reverse`.
6. **Cuentas:** decidir si se quedan las cuentas del seed o se empieza limpio; volver a dar de alta las cuentas reales
   desde un admin que apunte a la API; activar el código del admin y compartir la contraseña nueva solo con quien
   administre.
7. **La Mac servidor** no se debe dormir (si se duerme o se cierra Docker, la API se cae para todos).
