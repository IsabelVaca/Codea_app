# Documentación de Codea

## Prototipos

Maquetas HTML de diseño. Son de referencia visual: las pantallas de la app se arman copiando su diseño y sus textos.

| Archivo | Qué contiene |
| --- | --- |
| [`prototipos/caso-de-uso-turno-de-la-miss.html`](prototipos/caso-de-uso-turno-de-la-miss.html) | Turno completo de la asistente educativa: checador (entrada, fuera de perímetro, retardo), chat con los papás y reporte con foto desde el subproceso activo. |

Los archivos son autocontenidos (funcionan sin internet): ábrelos en el navegador con doble clic. Necesitan JavaScript porque el contenido viene empaquetado dentro del HTML.

### Pantallas de la app y su referencia en el prototipo

| Tab | Pantalla en el prototipo | Estado |
| --- | --- | --- |
| Mi día | Reporte con foto · paso 2 («Realizar reporte») | Interactiva: tocas cualquier momento para elegirlo y reportar sobre él, «Confirmar subproceso» avanza la rutina y cada momento cuenta sus reportes. Antes de checar muestra «Aún el día no comienza» |
| Mi día → Reporte | Reporte con foto · pasos 3 a 6, ruta A, E1 y E2 | Escrito, dictado o con foto (cámara real). No deja continuar sin texto ni guardar sin niños, y los que faltaron no se pueden marcar |
| Mi sala | Chatear con los papás · paso 1 | Asistencia editable (la comparte el reporte) y botón de chat |
| Chats | Chatear con los papás · paso 2 | Conversación de muestra, todavía no envía mensajes |
| Menú | Registrar entrada · paso 1 | Perfil, estado de la entrada, checador, opciones («Muy pronto») y cerrar sesión (reinicia el día del prototipo) |
| Menú → Checador | Registrar entrada · paso 2, E1 y E2 | Chips para simular «a tiempo», «tarde» y «lejos». Fuera del perímetro no registra; tarde registra con motivo opcional |
| Menú → Entrada registrada | Registrar entrada · paso 3 y E3 | Te lleva sola a Mi día en 2 segundos |

## Estructura del código

Sigue la de las prácticas del curso: `domain/` (reglas en Kotlin puro), `data/` (repositorios en memoria), `ui/state/` (ViewModels), `ui/screens/` (pantallas que solo dibujan), `ui/components/` y `ui/navigation/`. Las reglas del dominio tienen pruebas en `app/src/test/java/mx/tec/codea/domain/` (`./gradlew test`).

## Licencias

- La fuente **Poppins** (`app/src/main/res/font/poppins_*.ttf`) se usa bajo la SIL Open Font License 1.1: [`licencias/OFL-Poppins.txt`](licencias/OFL-Poppins.txt).
