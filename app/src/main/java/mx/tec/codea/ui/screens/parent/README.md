# `ui/screens/parent/`

Pantallas del padre. Pestañas: **Hoy, Calendario, Fotos, Documentos**. Todas leen de `ParentViewModel` (`ui/state/`), que carga de la API. Los archivos `*SampleData.kt` conservan los **modelos de interfaz** (`TimelineEvent`, `DaySummary`, `CalendarDay`, `RequestedDocument`, `AuthorizedPerson`…) y, además, objetos con datos de ejemplo de la etapa sin backend.

| Carpeta | Pantalla | Estado |
|---|---|---|
| `today/` | Hoy: resumen del día (ánimo, comida, cambios, siesta, nota) y línea de tiempo; elegir otro día; mandar una nota a la asistente | ✅ |
| `calendar/` | Calendario del mes con la asistencia de cada día | 🟡 Solo asistencia; faltan eventos y citas (CRF-03) |
| `photos/` | Fotos por categoría y botón "Pedir que se borre" (usa Coil) | 🟡 Sin paginación en pantalla; el backend ya pagina |
| `documents/` | Documentos pedidos (vigente o faltante), subir o reemplazar, y personas autorizadas a recoger (`AddAuthorizedDialog`) | ✅ |

## Pendiente

- **Chat del padre** con la docente (tarea abierta y prioritaria en Teams). La API de conversaciones ya acepta al padre.
- **Varios hijos** (HN4): `ParentViewModel` usa el primer hijo. Falta un selector de hijo y que cada pestaña cambie con él.
- **Avisos**: ya se leen en la campana de Hoy (`parent/notifications/`); falta marcar leído en el servidor.
- **Menú del día** (HN3).
- **Paginación de fotos** (tarea abierta; también ayuda a RNF05, fotos en menos de 4 s).
- **Resumen semanal** (RF-12).
- **Mandar fotos** desde el padre (tarea abierta).
- **Limpieza:** quitar los objetos `ParentXxxSampleData` que ya no se usan, y dejar los modelos en archivos con nombre propio (`ParentModels.kt`).
- **Sin conexión:** que Hoy y Calendario muestren lo último cargado.
