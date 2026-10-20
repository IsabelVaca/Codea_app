# `ui/screens/parent/`

Pantallas del padre. Pestañas: **Hoy, Calendario, Fotos, Chat, Mi hijo**. Todas leen de `ParentViewModel` (`ui/state/`), que carga de la API. Los archivos `*SampleData.kt` conservan los **modelos de interfaz** (`TimelineEvent`, `DaySummary`, `CalendarDay`, `RequestedDocument`…) y, además, objetos con datos de ejemplo de la etapa sin backend.

**Estilo (desde el 20/oct):** el mismo del admin. Las piezas comunes están en `ParentStyle.kt`: `PadreColores` (lila `EDE9FE` con morado `6D28D9`, gris para lo secundario, violeta `5B3FE0` para el botón principal), `TarjetaPadre` (tarjeta blanca con sombra), `DatoPadre` (etiqueta gris y valor en negritas) y `PastillaPadre`. Sin etiquetas de sección ni sobretítulos, y lo que no tiene dato no se muestra (por ejemplo, las métricas de Hoy si la maestra no llenó el resumen, o «Ánimo» en el resumen semanal si no hay registro).

| Carpeta | Pantalla | Estado |
|---|---|---|
| `today/` | Hoy: resumen del día (ánimo, comida, cambios, siesta, nota) y línea de tiempo; elegir otro día; mandar una nota a la asistente; botón de Mi cuenta junto a la campana | ✅ |
| `calendar/` | Calendario del mes con la asistencia de cada día | 🟡 Solo asistencia; faltan eventos y citas (CRF-03) |
| `photos/` | Fotos por categoría (chips con el número de cada una, solo las que tienen fotos) y visor con «Descargar» y «Borrar» con confirmación (usa Coil) | 🟡 Sin paginación en pantalla; el backend ya pagina |
| `child/` | **Mi hijo** (`MyChildScreen`): los datos del niño, la salud que la familia escribe y edita (alergias, condiciones médicas, intolerancias, medicamentos y otras indicaciones; la maestra los ve en la ficha del niño). Ya no se suben documentos desde aquí (se quitó el 18/oct; el backend conserva `GET` y `PUT /children/{id}/documents`). Las personas autorizadas para recoger **ya no se manejan aquí**: las administra el admin en Ajustes | 🟡 sin probar en el teléfono |

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
