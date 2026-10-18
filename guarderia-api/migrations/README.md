# Migraciones (Alembic)

| Versión | Qué crea |
|---|---|
| `63224ea4bc31_usuarios_centro_salas_y_ninos.py` | Usuarios, centro, salas y niños |
| `caf6344f99d5_modelo_de_datos_de_la_primera_entrega.py` | El resto del modelo de la primera entrega |
| `98306224d8b5_login_con_jwt.py` | Login propio: `password_hash`, `token_version` y fechas en `app_user` (se quita `firebase_uid`), `code_hash` y `purpose` en `invitation`, tabla `refresh_token` |

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

- Migraciones para lo que viene: eventos y citas, menú del día, imágenes en avisos.
- Probar `downgrade` de cada versión (rollback), que pide el plan de implantación.
- Estrategia de respaldo y restauración antes de migrar en producción.
