# API: login con correo, contraseña y JWT

> **Estado (2026-10-17): implementado** en la rama `feature/login-jwt` (API y app), sin subir. Diferencias con este diseño: la contraseña lleva de **9** a 128 caracteres (RNF01 dice "mayores a 8"); `POST /me/password` devuelve `200 TokenOut` y no 204, para que quien la cambia siga dentro; un código nuevo deja sin vigencia a los anteriores; y quien está de baja recibe `403 USER_INACTIVE` aunque su token ya no coincida. La referencia vigente es [`guarderia-api/app/modules/auth/README.md`](../../guarderia-api/app/modules/auth/README.md).

El login propio reemplaza a Firebase Auth. La regla de la propuesta se queda: **el token dice quién eres y Postgres dice qué puedes hacer**. El JWT solo lleva el id de la persona; el rol, el alcance y si está activa se leen de la base en cada request, como ya hace `get_current_user`. Así, dar de baja a alguien corta su acceso en el siguiente request.

La base es el [anexo de identidad del curso](https://startdroid.com/practicas/anexo-auth.html) (FastAPI + PostgreSQL), que la app de la práctica 6 ya sabe usar: mismas librerías, mismos tiempos y la misma rotación del refresh token. Lo que cambia para Codea es el primer acceso (código de invitación en lugar de registro abierto) y que el rol se lee de Postgres en lugar de ir en el token.

Este documento cubre lo necesario para que el login funcione bien. El endurecimiento (límites de intentos, almacenamiento cifrado, auditoría, etc.) está al final, en [Para la fase de seguridad](#para-la-fase-de-seguridad).

## Decisiones

| Tema | Decisión | Por qué |
| --- | --- | --- |
| Contraseñas | Argon2id con `pwdlib[argon2]` (`PasswordHash.recommended()`), como el anexo; no `passlib` | La sal va dentro del hash; `verify_and_update` permite subir el costo después sin pedir contraseñas nuevas |
| Access token | JWT `HS256` con `PyJWT` (no `python-jose`), algoritmo fijado al verificar (`algorithms=["HS256"]`), dura **5 minutos** | Igual que el anexo y la práctica 6. Un solo servicio firma y verifica, así que basta una llave simétrica |
| `JWT_SECRET` | Sin valor por omisión: la API no arranca sin él. Se genera con `secrets.token_urlsafe(48)` | Igual que el anexo; nunca en git |
| Refresh token | Texto aleatorio de 32 bytes (`secrets.token_urlsafe(32)`), **no** JWT; en la base solo su SHA-256; dura **7 días** y **rota** en cada uso | Igual que el anexo: se puede revocar uno por uno. Codea agrega la detección de reuso que el anexo deja como pendiente |
| Revocación inmediata | `app_user.token_version`; el JWT lleva `tv` y debe coincidir | Baja, cambio o restablecimiento de contraseña suben la versión e invalidan todos los access tokens vivos |
| Primer acceso | Código de invitación (8 caracteres) + correo + contraseña nueva | Nadie entra solo por saber un correo; el admin controla quién tiene acceso |
| Olvidé mi contraseña (mínimo) | El admin genera un código nuevo y la persona lo usa en Activar cuenta | No hace falta servidor de correo para la primera entrega |
| Olvidé mi contraseña (después) | `POST /auth/password/forgot` manda el código por correo | Mismo código y misma tabla; solo cambia cómo llega |
| Modo `dev:<correo>` | Se quita | Con JWT propio, las pruebas generan tokens reales y Swagger usa el login de verdad |
| Firebase | Se quita `firebase-admin` y su inicio en `lifespan` | Ya no hace falta: el curso tampoco usa FCM para notificaciones (práctica 8) |

## Flujos

```mermaid
sequenceDiagram
    participant Admin
    participant App
    participant API
    participant DB as Postgres

    Admin->>API: POST /teachers (correo, sala…)
    API->>DB: app_user + invitation(code_hash, expira en 7 días)
    API-->>Admin: 201 + access_code {code: K7QX-M4PZ}
    Note over Admin,App: El admin comparte el código (WhatsApp, en persona)

    App->>API: POST /auth/activate {email, code, password}
    API->>DB: guarda password_hash, marca el código como usado
    API-->>App: access_token (5 min) + refresh_token (7 días) + me

    App->>API: GET /classrooms/… con Bearer access_token
    API-->>App: 401 TOKEN_EXPIRED (pasaron 5 min)
    App->>API: POST /auth/refresh {refresh_token}
    API->>DB: revoca el viejo, crea uno nuevo (misma familia)
    API-->>App: tokens nuevos; la app repite el request
```

- **Login normal:** `POST /auth/login` con correo y contraseña → mismos tokens que Activar.
- **Cerrar sesión:** `POST /auth/logout` con el refresh token → se revoca; el access token muere solo en 5 minutos como máximo.
- **Baja:** al dar de baja a alguien, `token_version += 1` y se revocan sus refresh tokens. Su siguiente request responde `403 USER_INACTIVE` o `401 TOKEN_INVALID`.
- **Reuso:** si llega un refresh token que ya se había rotado, se revoca toda su familia (alguien lo copió) y hay que volver a entrar.

## Endpoints

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| POST | `/auth/login` | Sin sesión | `{email, password}` | 200 `TokenOut` | `401 INVALID_CREDENTIALS` (el mismo para correo desconocido y contraseña mal); `403 ACCOUNT_NOT_ACTIVATED`, `USER_INACTIVE` |
| POST | `/auth/activate` | Sin sesión | `{email, code, password}` | 200 `TokenOut` | `403 CODE_INVALID`, `CODE_EXPIRED`, `USER_INACTIVE`; `422 PASSWORD_TOO_SHORT` |
| POST | `/auth/refresh` | Sin sesión | `{refresh_token}` | 200 `TokenOut` (sin `me`) | `401 REFRESH_INVALID` |
| POST | `/auth/logout` | Sin sesión | `{refresh_token}` | 204 (también si ya no servía) | — |
| POST | `/me/password` | Todos | `{current_password, new_password}` | 204; cierra las demás sesiones | `403 PASSWORD_INCORRECT`; `422 PASSWORD_TOO_SHORT` |
| DELETE | `/me/sessions` | Todos | — | 200 `{revoked}`: cierra todas sus sesiones (como `DELETE /auth/sesiones` del anexo) | — |
| POST | `/users/{id}/access-code` | `ADMIN` | — | 201 `{code, purpose, expires_at}`: `INVITATION` si nunca ha entrado, `PASSWORD_RESET` si ya entró | `404 USER_NOT_FOUND`; `409 USER_INACTIVE` |
| POST | `/auth/password/forgot` | Sin sesión | `{email}` | 202 siempre (no revela si el correo existe) | — · **solo cuando haya correo** |

Además:

- `POST /teachers`, `POST /parents` y `POST /children` agregan a su respuesta `access_code: {code, expires_at}` (una sola vez; después solo se puede generar uno nuevo).
- `POST /parents/{id}/resend-invitation` pasa a llamar a `/users/{id}/access-code` por dentro, o se retira cuando la app use el nuevo.
- `POST /auth/accept-invitation` se borra.
- `has_account` (en docentes, padres y tutores) pasa de `firebase_uid is not None` a `password_hash is not None`.

`TokenOut`:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9…",
  "token_type": "bearer",
  "expires_in": 300,
  "refresh_token": "Xb3q9…",
  "refresh_expires_in": 604800,
  "me": { "id": "…", "roles": ["TEACHER"], "teacher": { … } }
}
```

Claims del access token: `sub` (uuid de `app_user`), `type: "access"`, `tv` (token_version), `iat`, `exp` y `jti`. Nada de roles ni datos personales: a diferencia del anexo (que manda `rol`), en Codea una persona puede tener varios roles y la baja debe cortar el acceso al instante, así que el rol se lee de Postgres en cada request.

Los nombres de los campos van en `snake_case` como el resto de la API (el anexo usa `accessToken`); en la app basta con `@SerialName`.

Errores del token en cualquier endpoint: `401 UNAUTHENTICATED` (no hay header), `401 TOKEN_EXPIRED` (la app refresca y reintenta) y `401 TOKEN_INVALID` (firma, tipo o `tv` no coinciden: la app cierra sesión).

## Cambios en la base

Una migración de Alembic:

| Tabla | Cambio |
| --- | --- |
| `app_user` | `+ password_hash text null`, `+ password_changed_at timestamptz null`, `+ last_login_at timestamptz null`, `+ token_version int not null default 0`, `− firebase_uid` |
| `invitation` | `+ code_hash text not null` (SHA-256 del código), `+ purpose varchar(20)` (`INVITATION`, `PASSWORD_RESET`); `accepted_at` sirve como "usado". Índice en `(email, accepted_at)` |
| `refresh_token` (nueva) | `id`, `user_id` → `app_user`, `token_hash` (único), `family_id` uuid, `expires_at`, `revoked_at`, `replaced_by_id`, `last_used_at`, `user_agent`, `created_at` |

El código se muestra como `XXXX-XXXX` con un alfabeto sin caracteres confusos (sin `0/O`, `1/I/L`) y se compara sin guion ni mayúsculas. Al usar uno, se marcan como usados todos los pendientes de esa persona.

## Cambios en el código

| Archivo | Qué cambia |
| --- | --- |
| `requirements.txt` | `+ PyJWT`, `+ pwdlib[argon2]`; `− firebase-admin` |
| `app/core/config.py` | `+ jwt_secret` (obligatorio, sin valor por omisión), `+ access_ttl_s = 300`, `+ refresh_ttl_s = 7 días`, `+ seed_password`; `− auth_mode`, `− firebase_project_id`, `− firebase_credentials` |
| `app/core/tokens.py` (nuevo) | `create_access_token(user)`, `decode_access_token(token)`, `new_refresh_token()`, `hash_token(raw)` y `new_access_code()` |
| `app/core/passwords.py` (nuevo) | `hash_password` y `verify_password` con `pwdlib`; corren en `run_in_threadpool` porque Argon2 tarda a propósito |
| `app/core/security.py` | `get_identity` e `Identity` se van; `get_current_user` decodifica el JWT, busca por `id`, revisa `status` y `tv`. `revoke_sessions(firebase_uid)` → `revoke_all_sessions(db, user)` |
| `app/modules/auth/` | Modelo `RefreshToken`; esquemas `LoginIn`, `ActivateIn`, `RefreshIn`, `ChangePasswordIn`, `TokenOut`, `AccessCodeOut`; servicio y router con los endpoints de arriba |
| `teachers`, `parents`, `children` | Respuesta de alta con `access_code`; baja llama `revoke_all_sessions`; `has_account` con `password_hash` |
| `app/main.py` | Sin inicio de Firebase en `lifespan`; router de `auth` con los endpoints nuevos |
| `app/seed.py` | Pone `SEED_PASSWORD` (en `.env.example`: `codea1234`) a los usuarios de ejemplo e imprime correo y contraseña; deja de usar `firebase_uid = "dev:…"` |
| `.env.example` y `README.md` | Variables nuevas; la sección "Entrar" explica el login y el botón Authorize de Swagger (pegar el `access_token` de `/auth/login`) |
| `tests/conftest.py` | `as_user(email)` genera un access token real para ese usuario en lugar de `dev:<correo>`; los usuarios del `world` llevan un hash de contraseña calculado una sola vez |

## Pruebas nuevas

- Login correcto; contraseña mal y correo desconocido dan el mismo `401 INVALID_CREDENTIALS`; cuenta sin activar; cuenta dada de baja.
- Activar con código correcto, vencido, equivocado y ya usado; contraseña corta.
- Refresh rota el token; el viejo ya no sirve; reusar el viejo revoca la familia.
- Logout revoca el refresh token.
- Access token vencido → `401 TOKEN_EXPIRED`; refresh token usado como access → `401 TOKEN_INVALID`.
- Dar de baja invalida el access token en el siguiente request (por `tv`) y los refresh tokens.
- Cambiar la contraseña cierra las demás sesiones; contraseña actual equivocada.
- `access-code` para alguien que ya entró genera `PASSWORD_RESET` y sirve para poner una contraseña nueva.
- Las 30 pruebas actuales siguen pasando con el nuevo `as_user`.

## Reglas mínimas de contraseña

De 8 a 128 caracteres y distinta del correo. El tope de 128 evita que alguien mande textos enormes para gastar CPU en Argon2. Reglas más fuertes van en la fase de seguridad.

## Para la fase de seguridad

Queda fuera de este plan y se revisa después:

- Límite de intentos por IP y por correo en login, activar y refresh (Redis o tabla), y bloqueo temporal.
- Respuesta con el mismo tiempo para correo existente y no existente (verificar contra un hash falso).
- Rotación de la llave del JWT (`kid` en el header) y llave en Docker secrets.
- Política de contraseñas más fuerte y revisión contra contraseñas filtradas.
- Auditoría de logins, códigos generados y bajas.
- Lista de sesiones abiertas y "cerrar las demás sesiones" desde la app.
- Tarea que borre refresh tokens y códigos vencidos.
- Segundo factor para el `ADMIN`.
- HTTPS obligatorio y HSTS (Cloudflare).
