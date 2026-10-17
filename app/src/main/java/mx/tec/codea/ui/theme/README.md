# `ui/theme/`

Tema de Material 3 de la app.

| Archivo | Qué contiene |
|---|---|
| `Color.kt` | Paleta |
| `DomainColors.kt` | Colores con significado del dominio (por ejemplo, estados de asistencia y de momentos de la rutina) |
| `Type.kt` | Tipografía; la fuente es **Poppins** (`app/src/main/res/font/`), bajo SIL Open Font License 1.1 (`docs/licencias/OFL-Poppins.txt`) |
| `Theme.kt` | `CodeaTheme` |

Los diseños salen del prototipo en `docs/prototipos/`. Usa los tokens de este tema en lugar de colores sueltos.

## Pendiente

- Revisar modo oscuro y accesibilidad (contraste, tamaños de fuente grandes).
- Si algún día se quiere bloquear capturas de pantalla (extra del tablero), se hace con `FLAG_SECURE` en la actividad, no aquí.
