# Migraciones (Alembic)

13 versiones en cadena; la última (head) es `d74a0f6c4e15`, aplicada en desarrollo y en producción (Aiven, `codea_prod`). En orden:

| Versión | Qué crea |
|---|---|
| `63224ea4bc31_usuarios_centro_salas_y_ninos.py` | Usuarios, centro, salas y niños |
| `caf6344f99d5_modelo_de_datos_de_la_primera_entrega.py` | El resto del modelo de la primera entrega |
| `98306224d8b5_login_con_jwt.py` | Login propio: `password_hash`, `token_version` y fechas en `app_user` (se quita `firebase_uid`), `code_hash` y `purpose` en `invitation`, tabla `refresh_token` |
| `b41c7d2e9a10_auditoria_e_intentos_de_acceso.py` | Tablas `audit_log` y `auth_attempt` (límite de intentos) |
| `c52d8e3f0b21_revision_de_borrado_de_fotos.py` | Quién y cuándo decidió una solicitud de borrado de foto (`media_deletion_request`; sin uso desde el 19/oct) |
| `d63e9f4a1c32_salida_de_la_docente.py` | Salida de la docente en `staff_check_in` (`checked_out_at` y su ubicación) |
| `e74f0a5b3d43_checador_sin_ubicacion.py` | Quita las columnas de ubicación del checador y agrega `auto_closed` |
| `f85a1b6c4e54_horario_del_centro.py` | Horario en `daycare_center`: `entry_time`, `exit_time`, `auto_close_time`, `work_days` |
| `a96b2c7d5f65_foto_en_el_chat.py` | `message.media_id` |
| `a07c3d8e6f76_salud_del_nino_y_pase_de_salida.py` | Datos de salud en `child` y tabla `child_departure` |
| `b18e4d7a2c93_hora_de_entrada_del_nino.py` | `attendance.arrived_at` |
| `c63f9e5b3d04_imagen_en_los_avisos.py` | `announcement.media_id` |
| `d74a0f6c4e15_menu_del_dia.py` | Tabla `daily_menu` |

```bash
docker compose exec api alembic upgrade head
docker compose exec api alembic revision --autogenerate -m "descripcion"   # revísala antes de aplicar
docker compose exec api alembic downgrade -1                               # rollback de una versión
```

`migrations/env.py` lee `Base.metadata` (por `app/core/registry.py`). Las restricciones tienen nombres fijos (`naming_convention` en `core/db.py`) para poder borrarlas.

## Reglas

- Todo cambio al modelo lleva su migración. No edites una ya aplicada: crea otra.
- Revisa el autogenerado: a veces propone borrar y recrear.
- Para el plan de implantación: documenta aquí el orden `upgrade head` → arrancar API → seed (solo en una base vacía; `python -m app.seed` no hace nada si ya hay datos).

## Antes de aplicar `98306224d8b5` en una base con datos (por ejemplo la de Aiven)

- **Quita `app_user.firebase_uid`** y ya no se puede volver a poner la información (el `downgrade` recrea la columna vacía). Haz un respaldo.
- Nadie tiene contraseña después de migrar: las personas existentes quedan sin acceso hasta que el admin les genere un código (`POST /users/{id}/access-code`) o se vuelva a correr el seed en una base limpia.
- Las invitaciones pendientes se dan por vencidas (antes se aceptaban con Google y ya no hay código que usar).
- Probada en una base local con datos: sube, baja y vuelve a subir sin errores.

## Pendiente

- Migraciones para lo que viene: eventos y citas. (Menú del día e imágenes en avisos ya están.)
- Probar `downgrade` de cada versión (rollback), que pide el plan de implantación.
- ~~Estrategia de respaldo y restauración antes de migrar en producción~~: hay respaldo diario de la base de producción y la restauración se probó el 20/oct (ver [`../README.md`](../README.md#respaldos-desde-el-20oct)). Antes de migrar, corre un respaldo a mano (`scripts/respaldo.sh`).
