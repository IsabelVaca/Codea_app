# `ui/screens/admin/`

Pantallas del administrador. Los nombres mezclan lowerCamelCase (`centroScreen.kt`, `salaDetalleScreen.kt`…) y PascalCase (`PadresScreen.kt`, `RutinasAdminScreen.kt`…): sigue el estilo del archivo vecino y no renombres.
Pestañas: **Centro, Docentes, Familias, Ajustes**. Cada una tiene su ViewModel en `ui/state/`.

| Pestaña | Archivos | Qué permite | Estado |
|---|---|---|---|
| Centro | `centroScreen.kt`, `crearSalaScreen.kt`, `salaDetalleScreen.kt` | Ver el centro y las salas, crear sala, asignar asistentes y cupo, borrar sala, publicar un aviso | ✅ |
| Docentes | `docenteScreenAdmin.kt`, `docenteDetalleScreen.kt`, `altaAsistenteScreen.kt` | Listar, dar de alta (la API devuelve un **código de acceso** que se muestra en un diálogo para compartirlo), cambiar de sala, dar de baja y reactivar | ✅ |
| Familias | `FamiliasScreen.kt` (lista con selector Infantes / Padres y tutores), `infanteScreenAdmin.kt` (`InfanteCard`), `PadresScreen.kt` (`PadreFila`), `infanteDetalleScreen.kt`, `inscribirInfanteScreen.kt`, `PadreDetalleScreen.kt`, `InscribirPadreScreen.kt` | Infantes: listar, inscribir (con tutor opcional), cambiar de sala, baja y reactivación. Padres: listar, inscribir (con código de acceso), quitar o devolver acceso, reenviar invitación (genera un código nuevo y lo muestra). Todo cuelga de `AdminFamiliasGraph` | ✅ |
| Ajustes | `AjustesScreen.kt` (menú), `HorarioScreen.kt` (horario del centro) y las pantallas de rutinas: `RutinasAdminScreen.kt`, `RutinaDetalleScreen.kt`, `NuevaRutinaScreen.kt`, `AgregarMomentoScreen.kt`, `CatalogoMomentosScreen.kt`, `OrdenRutinaScreen.kt`, `RutinaComponentes.kt` | Todo lo que el admin configura: **Horario** (hora de entrada, de salida, de cierre automático y días de trabajo de todo el centro), **Rutinas** (plantillas, agregar y ordenar momentos, catálogo, asignar plantilla a una sala), **Mi cuenta**. Para sumar una opción, agrégala en `AjustesScreen` y su ruta en `AjustesAdminGraph` | ✅ sin probar en el teléfono |

## Pendiente

- **Avisos con imágenes** (HN5). Publicar (a todo el centro o a una sala) y el historial ya existen en Centro, solo con texto.
- **Turnos por docente:** el checador usa el horario único del centro; los turnos (Matutino, Vespertino) siguen en el alta de la docente pero ya no cambian el retardo. Falta decidir si se quitan del alta.
- **Calendario de eventos y citas** (CRF-03).
- **Menú del día** (HN3) y **llegada de los padres** (HN2).
- **Imprimir o descargar bitácoras** (HN1).
- **Enviar el código por correo** (tarea abierta): hoy el admin lo ve en pantalla y lo comparte (WhatsApp, en persona); no se manda ningún correo. Tampoco hay botón para generar un código de recuperación de contraseña a alguien que ya entró (`POST /users/{id}/access-code`).
- **Arrastrar para ordenar** (`OrdenRutinaScreen`) depende de la librería `reorderable`, fuera del marco ([decisión #8](../../../../../../../../../../docs/DECISIONES-PENDIENTES.md)).
- **Acceso:** estas pantallas piden datos solo si `ApiConfig.isAdmin`, que `CodeaApp` fija con el rol de la sesión (`GET /me`). Mi cuenta (perfil, contraseña y cerrar sesión) se abre solo desde **Ajustes**.
