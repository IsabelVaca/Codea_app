# `reports`: reportes y resumen del día

Tablas: `report` (momento, tipo `INCIDENT`/`ACHIEVEMENT`/`HEALTH`/`BEHAVIOR`, método `WRITTEN`/`DICTATION`/`PHOTO`, texto, `media_id`, `created_by`), `report_child` (un reporte para varios niños), `daily_summary` (ánimo, comida, cambios, siesta, nota; único por niño y día).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `POST /reports` | docente | Crea un reporte de un momento. 422 `DESCRIPTION_TOO_SHORT`, `CHILDREN_REQUIRED`; 409 `MOMENT_NOT_STARTED`, `CHILD_ABSENT` |
| `GET /reports` | admin o docente | Reportes de un momento (el contador de Mi día) |
| `POST /children/{id}/daily-entries` | admin o docente | Guarda el resumen del día de un niño |
| `GET /children/{id}/day` | quien tenga alcance | Pantalla Hoy: resumen y línea de tiempo |

Mismas reglas que `ReportValidator` en la app.

## Estado

| Hecho | Pendiente |
|---|---|
| Reportes a varios niños, con foto, línea de tiempo, resumen del día | **Resumen semanal** (RF-12) |
| | **Exportar o imprimir reportes** (HN1) |
| | **Consulta con IA** (CRF-04): el dictado ocurre en la app con el reconocedor del sistema; el backend no hace nada con IA |
| | **Auditoría** (RF-14): solo hay `created_by` y `recorded_by` |
| | Editar o anular un reporte (hoy solo se crea) |

Pruebas: `tests/test_teacher_day.py` (`test_report_rules`), `tests/test_media_documents.py` (`test_photo_report_reaches_the_family`), `tests/test_parent_views.py` (`test_today_screen`).
