# Migraciones (Alembic)

| Versión | Qué crea |
|---|---|
| `63224ea4bc31_usuarios_centro_salas_y_ninos.py` | Usuarios, centro, salas y niños |
| `caf6344f99d5_modelo_de_datos_de_la_primera_entrega.py` | El resto del modelo de la primera entrega |

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

## Pendiente

- Migraciones para lo que viene: `password_hash` y `refresh_token` (auth propia), `audit_log`, pagos, eventos y citas, menú del día, imágenes en avisos.
- Probar `downgrade` de cada versión (rollback), que pide el plan de implantación.
- Estrategia de respaldo y restauración antes de migrar en producción.
