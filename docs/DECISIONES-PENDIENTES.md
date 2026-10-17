# Decisiones pendientes y excepciones al marco

Todo lo que se salga de [`MARCO-TECNICO.md`](MARCO-TECNICO.md) se registra aquí. **No se implementa hasta que el responsable apruebe.**
Estado a 2026-10-16: nada está aprobado todavía.

| # | Tema | Por qué importa | Alternativa dentro del marco | Recomendación | Estado |
|---|---|---|---|---|---|
| 1 | Login con Firebase Auth | Firebase está excluido del marco. | JWT propio en FastAPI (contraseña con hash, access de 5 min y refresh), guardado con DataStore y Keystore. **Ya diseñado por el equipo en [`plan/04-api-login-jwt.md`](plan/04-api-login-jwt.md)** (Argon2id, PyJWT, refresh rotado). | Seguir ese diseño. Además cierra RF-01, RNF01 y RNF03. | ⏳ Falta la aprobación formal; el diseño ya existe |
| 2 | Notificaciones con Firebase | Excluido de forma explícita. | WorkManager + notificaciones locales; [`estilo.md`](estilo.md) ya dice "FCM descartado". | Usar la alternativa. | ⏳ Pendiente |
| 3 | Google Maps Compose (checador) | Usa Play Services. | Mapa dibujado, como el del prototipo (la rama `checador-menu` ya lo hizo). | Aprobar excepción o usar la rama `checador-menu`. | ⏳ Pendiente |
| 4 | Huella (`androidx.biometric`, RF-13) | Librería fuera de la lista. | Ninguna equivalente. | Aprobar excepción o mover RF-13 a "fuera de alcance". | ⏳ Pendiente |
| 5 | Redis | Fuera de la lista. | Postgres para lo que se necesite. | No usar. | ⏳ Pendiente |
| 6 | Grafana (monitoreo) | Fuera de la lista. | Endpoints `/health/live` y `/health/ready` ya existentes + logs. | Aprobar solo si se quiere evidencia de disponibilidad (RNF04). | ⏳ Pendiente |
| 7 | Cloudflare tunnel (HTTPS público) | Fuera de la lista. | Desplegar con Docker en un servidor propio con certificado. | Aprobar si no hay otra forma rápida de tener HTTPS. | ⏳ Pendiente |
| 8 | Librería `reorderable` | Fuera de la lista. | Botones subir/bajar o arrastre propio. | Reemplazar o aprobar. | ⏳ Pendiente |
| 9 | SeaweedFS como almacén | El sitio nombra RustFS (compatible con S3). | RustFS. | Confirmar que SeaweedFS cuenta como "compatible con S3". | ⏳ Pendiente |
| 10 | Dictado con `RecognizerIntent` | API del sistema, pero usa el reconocedor de Google. | Sin dictado. | Confirmar que está permitido. | ⏳ Pendiente |

## Cómo se registra una decisión

Cambia el estado a `✅ Aprobada (fecha, quién)` o `❌ Rechazada` y deja una línea de por qué. Lo aprobado debe aparecer en la sección de desviaciones de la entrega final ([`ENTREGA-3.md`](ENTREGA-3.md), parte 5).
