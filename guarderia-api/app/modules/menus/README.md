# `menus`: menú del día (HN3)

El admin captura lo que se come cada día del centro; las familias lo ven en Hoy y la docente en Mi día. Tabla: `daily_menu` (centro, fecha, `breakfast`, `lunch`, `snack`, quién lo guardó). Hay uno por centro y por día; cada comida es opcional, pero el menú lleva al menos una.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /menus?start=&end=` | cualquiera con cuenta | Los menús capturados entre las dos fechas (ambas incluidas), del más viejo al más nuevo. Sin fechas, el de hoy. Los días sin menú no aparecen. `422 RANGE_INVALID` si el final es anterior al inicio o el rango pasa de 31 días |
| `PUT /menus/{fecha}` | admin | Crea o reemplaza el menú de ese día. Las comidas en blanco se guardan como vacías. `422 MENU_EMPTY` si no trae ninguna (para quitarlo se usa `DELETE`) |
| `DELETE /menus/{fecha}` | admin | Quita el menú de ese día. `404 MENU_NOT_FOUND` |

- `GET /children/{id}/day` (la pantalla Hoy de la familia) trae `menu` del día que se pide, o `null` si no lo capturaron.
- Hoy hay un solo centro: el admin escribe en el suyo y docente y papá leen el único que existe (`default_center`). Con más sedes, el menú tendría que salir de la sala del niño o de la docente.
- Cada alta, cambio o baja queda en la bitácora de auditoría (`MENU_SAVED`, `MENU_DELETED`, con la fecha).

## Estado

| Hecho | Pendiente |
|---|---|
| Capturar, consultar por rango, reemplazar y quitar; menú dentro del día del niño | Menús por sala o por grupo de edad |
| Pantalla del admin (Ajustes → Menú del día), tarjeta en Hoy de la familia y en Mi día | Copiar el menú de una semana a otra |
| | Alergias del niño contra el menú (avisar a la familia) |
| | Verlo en un teléfono |

Pruebas: `tests/test_daily_menu.py` (8: guardar y reemplazar, menú vacío, quién lo ve y quién lo escribe, rango, quitar, menú en el día del niño, auditoría).
