# `ui/screens/admin/`

Pantallas del administrador. Los nombres mezclan lowerCamelCase (`centroScreen.kt`, `salaDetalleScreen.kt`…) y PascalCase (`PadresScreen.kt`, `RutinasAdminScreen.kt`…): sigue el estilo del archivo vecino y no renombres.
Pestañas: **Centro, Docentes, Infantes, Padres, Rutinas**. Cada una tiene su ViewModel en `ui/state/`.

| Pestaña | Archivos | Qué permite | Estado |
|---|---|---|---|
| Centro | `centroScreen.kt`, `crearSalaScreen.kt`, `salaDetalleScreen.kt` | Ver el centro y las salas, crear sala, asignar asistentes y cupo, borrar sala, publicar un aviso | ✅ |
| Docentes | `docenteScreenAdmin.kt`, `docenteDetalleScreen.kt`, `altaAsistenteScreen.kt` | Listar, dar de alta (la API devuelve un **código de acceso** que se muestra en un diálogo para compartirlo), cambiar de sala, dar de baja y reactivar | ✅ |
| Infantes | `infanteScreenAdmin.kt`, `infanteDetalleScreen.kt`, `inscribirInfanteScreen.kt` | Listar, inscribir (con tutor opcional), cambiar de sala, baja y reactivación | ✅ |
| Padres | `PadresScreen.kt`, `PadreDetalleScreen.kt`, `InscribirPadreScreen.kt` | Listar, inscribir (con código de acceso), quitar o devolver acceso, reenviar invitación (genera un código nuevo y lo muestra) | ✅ |
| Rutinas | `RutinasAdminScreen.kt`, `RutinaDetalleScreen.kt`, `NuevaRutinaScreen.kt`, `AgregarMomentoScreen.kt`, `CatalogoMomentosScreen.kt`, `OrdenRutinaScreen.kt`, `RutinaComponentes.kt` | Plantillas de rutina, agregar y ordenar momentos, catálogo de momentos, asignar plantilla a una sala | ✅ (la última pantalla agregada; sin probar a fondo) |

## Pendiente

- **Avisos con imágenes** (HN5). Publicar (a todo el centro o a una sala) y el historial ya existen en Centro, solo con texto.
- **Configuración del centro** (ubicación, radio del checador y turnos): la API ya tiene `PATCH /center`, pero no hay pantalla; hoy la ubicación se cambia por la API.
- **Calendario de eventos y citas** (CRF-03).
- **Menú del día** (HN3) y **llegada de los padres** (HN2).
- **Imprimir o descargar bitácoras** (HN1).
- **Enviar el código por correo** (tarea abierta): hoy el admin lo ve en pantalla y lo comparte (WhatsApp, en persona); no se manda ningún correo. Tampoco hay botón para generar un código de recuperación de contraseña a alguien que ya entró (`POST /users/{id}/access-code`).
- **Arrastrar para ordenar** (`OrdenRutinaScreen`) depende de la librería `reorderable`, fuera del marco ([decisión #8](../../../../../../../../../../docs/DECISIONES-PENDIENTES.md)).
- **Acceso:** estas pantallas piden datos solo si `ApiConfig.isAdmin`, que `CodeaApp` fija con el rol de la sesión (`GET /me`). El avatar de **Centro** abre Mi cuenta.
