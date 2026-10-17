# `ui/screens/admin/`

Pantallas del administrador. Los nombres mezclan lowerCamelCase (`centroScreen.kt`, `salaDetalleScreen.kt`…) y PascalCase (`PadresScreen.kt`, `RutinasAdminScreen.kt`…): sigue el estilo del archivo vecino y no renombres.
Pestañas: **Centro, Docentes, Infantes, Padres, Rutinas**. Cada una tiene su ViewModel en `ui/state/`.

| Pestaña | Archivos | Qué permite | Estado |
|---|---|---|---|
| Centro | `centroScreen.kt`, `crearSalaScreen.kt`, `salaDetalleScreen.kt` | Ver el centro y las salas, crear sala, asignar asistentes y cupo, borrar sala, publicar un aviso | ✅ |
| Docentes | `docenteScreenAdmin.kt`, `docenteDetalleScreen.kt`, `altaAsistenteScreen.kt` | Listar, dar de alta (con invitación), cambiar de sala, dar de baja y reactivar | ✅ |
| Infantes | `infanteScreenAdmin.kt`, `infanteDetalleScreen.kt`, `inscribirInfanteScreen.kt` | Listar, inscribir (con tutor opcional), cambiar de sala, baja y reactivación | ✅ |
| Padres | `PadresScreen.kt`, `PadreDetalleScreen.kt`, `InscribirPadreScreen.kt` | Listar, inscribir, quitar o devolver acceso, reenviar invitación | ✅ |
| Rutinas | `RutinasAdminScreen.kt`, `RutinaDetalleScreen.kt`, `NuevaRutinaScreen.kt`, `AgregarMomentoScreen.kt`, `CatalogoMomentosScreen.kt`, `OrdenRutinaScreen.kt`, `RutinaComponentes.kt` | Plantillas de rutina, agregar y ordenar momentos, catálogo de momentos, asignar plantilla a una sala | ✅ (la última pantalla agregada; sin probar a fondo) |

## Pendiente

- **Avisos con imágenes** (HN5). Publicar (a todo el centro o a una sala) y el historial ya existen en Centro, solo con texto.
- **Solicitudes de borrado de fotos** (RF-10): la API las recibe de las familias pero no hay endpoint ni pantalla para que el admin las apruebe o rechace.
- **Pagos por infante** (CRF-01).
- **Calendario de eventos y citas** (CRF-03).
- **Menú del día** (HN3) y **llegada de los padres** (HN2).
- **Imprimir o descargar bitácoras** (HN1).
- **Enviar el correo de invitación** (tarea abierta): hoy "reenviar" solo renueva la vigencia en el backend y no manda nada.
- **Arrastrar para ordenar** (`OrdenRutinaScreen`) depende de la librería `reorderable`, fuera del marco ([decisión #8](../../../../../../../../../../docs/DECISIONES-PENDIENTES.md)).
- **Acceso:** estas pantallas piden datos solo si `ApiConfig.isAdmin`; con login real, el admin sale del rol del `GET /me`.
