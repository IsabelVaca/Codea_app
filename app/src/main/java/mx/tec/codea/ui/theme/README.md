# `ui/theme/`

Tema de Material 3 de la app.

| Archivo | Qué contiene |
|---|---|
| `Color.kt` | Paleta |
| `DomainColors.kt` | Colores que el dominio no maneja: el de los avatares de iniciales (`AvatarBg` lila y `AvatarFg` morado, uno solo para todos) y `salaColor`, un color estable por sala |
| `Type.kt` | Tipografía; la fuente es **Poppins** (`app/src/main/res/font/`), bajo SIL Open Font License 1.1 (`docs/licencias/OFL-Poppins.txt`) |
| `Tones.kt` | `ColorScheme.muted`: el gris de los textos secundarios, que se lee en claro y en oscuro |
| `Theme.kt` | `CodeaTheme` (sigue el modo oscuro del sistema; sin color dinámico) |

Los diseños salen del prototipo en `docs/prototipos/`. Usa los tokens de este tema en lugar de colores sueltos. Excepción: las pantallas del padre y de la docente, que copian el estilo del admin, toman colores fijos de `CodeaColores` (`ui/components/EstiloTarjetas.kt`) y las del admin los escriben directo, no de este tema.

## Pendiente

- Revisar modo oscuro y accesibilidad (contraste, tamaños de fuente grandes). Lo que usa `CodeaColores` no cambia en modo oscuro.
- Si algún día se quiere bloquear capturas de pantalla (extra del tablero), se hace con `FLAG_SECURE` en la actividad, no aquí.
