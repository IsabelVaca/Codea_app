# `auth`: sesión, códigos de acceso y perfil

Tablas: `app_user` (cuenta; `password_hash`, `token_version`), `invitation` (el código de acceso, solo su hash; `purpose` = `INVITATION` o `PASSWORD_RESET`), `refresh_token` (solo su hash; `family_id` para detectar reuso).
Piezas compartidas: `core/tokens.py` (JWT, refresh, códigos), `core/passwords.py` (Argon2id), `core/security.py` (`get_current_user`).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `POST /auth/login` | sin sesión | Correo y contraseña → `TokenOut`. `401 INVALID_CREDENTIALS` (igual para correo desconocido y contraseña mal); `403 ACCOUNT_NOT_ACTIVATED`, `USER_INACTIVE`; `429 TOO_MANY_ATTEMPTS` |
| `POST /auth/activate` | sin sesión | Primer acceso: correo + código + contraseña nueva → `TokenOut`. `403 CODE_INVALID`, `CODE_EXPIRED`, `USER_INACTIVE`; `422 PASSWORD_TOO_SHORT`, `PASSWORD_TOO_LONG`, `PASSWORD_IS_EMAIL`; `429 TOO_MANY_ATTEMPTS` |
| `POST /auth/refresh` | sin sesión | Cambia un refresh token por un par nuevo (sin `me`); el viejo queda revocado. `401 REFRESH_INVALID` |
| `POST /auth/logout` | sin sesión | Revoca el refresh token; 204 aunque ya no sirviera |
| `GET /me` | cualquiera con cuenta | Rol, sala o hijos; si es docente, también turno y centro para el checador |
| `POST /me/password` | cualquiera con cuenta | Cambia la contraseña, cierra todas las sesiones y devuelve tokens nuevos. `403 PASSWORD_INCORRECT` |
| `DELETE /me/sessions` | cualquiera con cuenta | Cierra todas mis sesiones |
| `POST /users/{id}/access-code` | admin | Genera un código (`INVITATION` o `PASSWORD_RESET`). `404 USER_NOT_FOUND`, `409 USER_INACTIVE` |
| `POST /invitations` | admin | Invita a una persona con un rol; devuelve su `access_code` una sola vez |
| `GET /invitations` | admin | Códigos con estado `PENDING`/`ACCEPTED`/`EXPIRED`; filtros `?status=` y `?role=` |
| `GET /invitar?email=&code=` | pública, fuera de `/api/v1` | Página del enlace de invitación: botón «Abrir en la app» (`codea://invitar?…`) y el correo y el código escritos. **Solo lee, nunca gasta el código.** `400` si el correo o el código no tienen la forma esperada (no se refleja nada) |
| `GET /.well-known/assetlinks.json` | pública | Le dice a Android que `mx.tec.codea` puede abrir los enlaces de este servidor (`ANDROID_CERT_SHA256`); vacía si no está configurada |

## Cómo funciona

1. El admin da de alta a alguien y la respuesta trae `access_code` (`XXXX-XXXX`, 7 días, solo se ve una vez) y, con `PUBLIC_BASE_URL`, su `link`. La app del admin arma el mensaje de WhatsApp con el enlace y, por si no abre la app, el correo y el código escritos.
2. La persona manda correo + código + contraseña a `/auth/activate` y queda dentro con `access_token` (5 min) y `refresh_token` (7 días).
3. Con el access token vencido la API responde `401 TOKEN_EXPIRED`; la app llama `/auth/refresh` y repite la petición. Cada refresh rota el token; si llega uno ya rotado se cierra toda su familia.
4. El JWT lleva `sub`, `type`, `tv` (`token_version`), `iat`, `exp` y `jti`: **nada de rol ni datos personales**. El rol se lee de Postgres en cada petición.
5. Baja, cambio de contraseña y `PASSWORD_RESET` suben `token_version` y revocan los refresh tokens: los access tokens vivos dejan de servir al instante.

## Estado

| Hecho | Pendiente (fase de seguridad) |
|---|---|
| Login, activar con código, refresh con rotación y detección de reuso, logout, cambio de contraseña, cerrar sesiones, códigos nuevos y de recuperación, baja que corta el acceso | Límite en `refresh` (los refresh tokens tienen entropía alta, el riesgo es bajo) |
| **Límite de intentos** (ver abajo) y **auditoría** de logins, códigos y bajas (`modules/audit`) | Lista de sesiones abiertas; segundo factor para el admin |
| 33 pruebas en `tests/test_auth.py`, 23 en `tests/test_security.py` y 7 en `tests/test_invitations.py` (enlace y `assetlinks`) | Sin correos: la invitación la manda el admin por WhatsApp (decisión del equipo) |
| | Rotación de la llave del JWT (`kid`), tarea que borre refresh tokens y códigos vencidos |

Diseño y decisiones: [`docs/plan/04-api-login-jwt.md`](../../../../docs/plan/04-api-login-jwt.md). Diferencias con ese plan: `POST /me/password` devuelve `200 TokenOut` (y no 204) para que quien cambia su contraseña siga dentro; el código se acepta con o sin guion y en cualquier mayúscula: `normalize_code` solo compara letras y números, así que tampoco estorban los espacios ni los caracteres invisibles que trae un texto copiado de WhatsApp. El correo pasa por `rules.clean_email` (sin espacios ni invisibles, en minúsculas) en el alta, el login y la activación.

## Límite de intentos (`throttle.py`)

Se cuentan los fallos de `/auth/login` y de `/auth/activate` (el código es corto y se podría adivinar) en una ventana móvil:

| Regla | Valor por omisión | Variable |
|---|---|---|
| Fallos por correo antes de bloquearlo | 5 | `AUTH_MAX_FAILURES` |
| Fallos por IP antes de bloquearla (frena el barrido de muchos correos) | 30 | `AUTH_MAX_FAILURES_IP` |
| Ventana, y duración del bloqueo | 900 s (15 min) | `AUTH_WINDOW_S` |

- Con el correo bloqueado responde `429 TOO_MANY_ATTEMPTS` con `retry_after_s` (y el encabezado `Retry-After`) **sin revisar la contraseña**, también si era la buena.
- Los fallos se guardan por correo aunque la cuenta no exista, así el bloqueo no delata qué correos están registrados.
- Entrar bien borra los fallos de ese correo. Se guarda en Postgres (`auth_attempt`), por eso vale con varios procesos de la API.
- Riesgo conocido: alguien puede bloquear la cuenta de otra persona 15 minutos fallando a propósito. Se acepta a cambio de frenar la fuerza bruta; la ventana es corta.
- Detrás de un proxy, `request.client` debe traer la IP real (`uvicorn --proxy-headers` y un proxy de confianza); si no, el límite por IP vería a todos como la misma. En producción ya está: `docker-compose.prod.yml` arranca uvicorn con `--proxy-headers` y solo confía en la red interna del túnel.
