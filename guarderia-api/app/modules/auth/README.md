# `auth`: sesión, códigos de acceso y perfil

Tablas: `app_user` (cuenta; `password_hash`, `token_version`), `invitation` (el código de acceso, solo su hash; `purpose` = `INVITATION` o `PASSWORD_RESET`), `refresh_token` (solo su hash; `family_id` para detectar reuso).
Piezas compartidas: `core/tokens.py` (JWT, refresh, códigos), `core/passwords.py` (Argon2id), `core/security.py` (`get_current_user`).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `POST /auth/login` | sin sesión | Correo y contraseña → `TokenOut`. `401 INVALID_CREDENTIALS` (igual para correo desconocido y contraseña mal); `403 ACCOUNT_NOT_ACTIVATED`, `USER_INACTIVE` |
| `POST /auth/activate` | sin sesión | Primer acceso: correo + código + contraseña nueva → `TokenOut`. `403 CODE_INVALID`, `CODE_EXPIRED`, `USER_INACTIVE`; `422 PASSWORD_TOO_SHORT`, `PASSWORD_TOO_LONG`, `PASSWORD_IS_EMAIL` |
| `POST /auth/refresh` | sin sesión | Cambia un refresh token por un par nuevo (sin `me`); el viejo queda revocado. `401 REFRESH_INVALID` |
| `POST /auth/logout` | sin sesión | Revoca el refresh token; 204 aunque ya no sirviera |
| `GET /me` | cualquiera con cuenta | Rol, sala o hijos; si es docente, también turno y centro para el checador |
| `POST /me/password` | cualquiera con cuenta | Cambia la contraseña, cierra todas las sesiones y devuelve tokens nuevos. `403 PASSWORD_INCORRECT` |
| `DELETE /me/sessions` | cualquiera con cuenta | Cierra todas mis sesiones |
| `POST /users/{id}/access-code` | admin | Genera un código (`INVITATION` o `PASSWORD_RESET`). `404 USER_NOT_FOUND`, `409 USER_INACTIVE` |
| `POST /invitations` | admin | Invita a una persona con un rol; devuelve su `access_code` una sola vez |
| `GET /invitations` | admin | Códigos con estado `PENDING`/`ACCEPTED`/`EXPIRED`; filtros `?status=` y `?role=` |

## Cómo funciona

1. El admin da de alta a alguien y la respuesta trae `access_code` (`XXXX-XXXX`, 7 días, solo se ve una vez).
2. La persona manda correo + código + contraseña a `/auth/activate` y queda dentro con `access_token` (5 min) y `refresh_token` (7 días).
3. Con el access token vencido la API responde `401 TOKEN_EXPIRED`; la app llama `/auth/refresh` y repite la petición. Cada refresh rota el token; si llega uno ya rotado se cierra toda su familia.
4. El JWT lleva `sub`, `type`, `tv` (`token_version`), `iat`, `exp` y `jti`: **nada de rol ni datos personales**. El rol se lee de Postgres en cada petición.
5. Baja, cambio de contraseña y `PASSWORD_RESET` suben `token_version` y revocan los refresh tokens: los access tokens vivos dejan de servir al instante.

## Estado

| Hecho | Pendiente (fase de seguridad) |
|---|---|
| Login, activar con código, refresh con rotación y detección de reuso, logout, cambio de contraseña, cerrar sesiones, códigos nuevos y de recuperación, baja que corta el acceso | **Límite de intentos y bloqueo temporal** en login, activar y refresh |
| 31 pruebas en `tests/test_auth.py` | **Correo real** con el código (hoy el admin lo comparte) |
| | Rotación de la llave del JWT (`kid`), tarea que borre refresh tokens y códigos vencidos |
| | Auditar logins, códigos generados y bajas; lista de sesiones abiertas; segundo factor para el admin |

Diseño y decisiones: [`docs/plan/04-api-login-jwt.md`](../../../../docs/plan/04-api-login-jwt.md). Diferencias con ese plan: `POST /me/password` devuelve `200 TokenOut` (y no 204) para que quien cambia su contraseña siga dentro; el código se acepta con o sin guion y en cualquier mayúscula.
