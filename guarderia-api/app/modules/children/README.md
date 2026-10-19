# `children`: infantes

Tabla: `child` (nombre, apellido, nacimiento, alergias, condiciones médicas, intolerancias, medicamentos, otras indicaciones, sala, estado).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /children` | admin | Infantes con edad, tutor, parentesco, correo del tutor, sala y baja |
| `POST /children` | admin | Inscribe; el tutor es opcional (si viene, se crea y se invita). 422 `NAME_REQUIRED`, `AGE_INVALID`, `GUARDIAN_REQUIRED`, `PHONE_INVALID` |
| `GET /children/{id}` | cualquiera con alcance | Expediente. El padre solo ve a sus hijos; la docente, a los de su sala; si no, 404 |
| `PATCH /children/{id}` | admin | Cambia de sala (`classroom_id = null` = sin sala) |
| `PATCH /children/{id}/health` | admin o padre (sus hijos) | La familia escribe alergias, condiciones médicas, intolerancias, medicamentos y otras indicaciones ("Mi hijo"). Solo cambia lo que viene; `""` borra. 422 `TEXT_TOO_LONG` (más de 1000 caracteres). La auditoría guarda qué campos cambiaron, nunca su contenido |
| `POST /children/{id}/deactivate` / `reactivate` | admin | Baja y reactivación |

Edad válida: de 0 a 12 años (`core/rules.py`, igual que `InfanteValidator` en la app).

## Estado

| Hecho | Pendiente |
|---|---|
| Alta, edición, baja, alcance por rol | Varios tutores al inscribir (el modelo `parent_child_rel` ya lo permite) |
| | Más datos del expediente (contactos de emergencia, información médica) si el cliente lo pide |
| | Auditar quién consultó o cambió un expediente (dato sensible de menores) |

Pruebas: `tests/test_admin.py` (`test_enroll_child_*`, `test_child_scope`) y `tests/test_departures_and_health.py` (salud).
