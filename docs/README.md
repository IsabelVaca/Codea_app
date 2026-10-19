# Documentación de Codea

## Índice

| Documento | Para qué sirve |
|---|---|
| [`GUIA-CONTINUAR.md`](GUIA-CONTINUAR.md) | Empieza aquí: cómo levantar todo, mapa del repo y trampas |
| [`ESTADO.md`](ESTADO.md) | Qué está hecho y qué falta por área |
| [`REQUISITOS.md`](REQUISITOS.md) | Los 32 requisitos y su estado |
| [`MARCO-TECNICO.md`](MARCO-TECNICO.md) | Qué tecnologías se permiten (startdroid) y cómo está el proyecto frente a eso |
| [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) | Excepciones al marco que esperan aprobación |
| [`ENTREGA-3.md`](ENTREGA-3.md) | Checklist de la rúbrica final |
| [`PLAN.md`](PLAN.md) | Plan del 16 al 22 de octubre |

> La sección de prototipos de abajo es de la primera etapa. La tabla "Estado" de las pantallas está desactualizada: por ejemplo, Chats ya envía mensajes por la API y el Menú ya no es solo de muestra. Para el estado real ve [`ESTADO.md`](ESTADO.md).

## Prototipos

Maquetas HTML de diseño. Son de referencia visual: las pantallas de la app se arman copiando su diseño y sus textos.

| Archivo | Qué contiene |
| --- | --- |
| [`prototipos/caso-de-uso-turno-de-la-miss.html`](prototipos/caso-de-uso-turno-de-la-miss.html) | Turno completo de la asistente educativa: checador (entrada, fuera de perímetro, retardo), chat con los papás y reporte con foto desde el subproceso activo. |

Los archivos son autocontenidos (funcionan sin internet): ábrelos en el navegador con doble clic. Necesitan JavaScript porque el contenido viene empaquetado dentro del HTML.

## Estilo

[`estilo.md`](estilo.md) resume las convenciones de las prácticas del curso ([startdroid.com](https://startdroid.com/)) que sigue Codea: capas y paquetes, nombres, estado, red, sesión, Hilt, diseño, pruebas y la API.

## Plan de trabajo

[`plan/`](plan/README.md) tiene el plan para llegar al mínimo funcional: pantallas que faltan en la app, cómo conectarla a la API, estado de la API, diseño del login con JWT y tareas en orden.

## API

El código de la API vive en [`guarderia-api/`](../guarderia-api/) (cómo levantarla: [`guarderia-api/README.md`](../guarderia-api/README.md)).

| Archivo | Qué contiene |
| --- | --- |
| [`api/endpoints.md`](api/endpoints.md) | **Referencia vigente.** Los endpoints que tiene hoy la API (83 al 18 de octubre), sacados del código: rol, qué envían, qué responden y sus códigos de error. |
| [`api/endpoints-api-codea.pdf`](api/endpoints-api-codea.pdf) | Histórico. Endpoints de la primera entrega por rol, con las rutas anteriores; ya no coincide en varios detalles con el código. |
| [Propuesta de arquitectura: API de la guardería](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H) | Arquitectura completa: componentes, login con Firebase, convenciones, modelo de datos, fotos con S3, monitoreo y seguridad. |

El contrato vivo es el OpenAPI que genera FastAPI: con la API prendida, ábrelo en `http://localhost:8000/docs`.

### Pantallas de la app y su referencia en el prototipo

| Tab | Pantalla en el prototipo | Estado |
| --- | --- | --- |
| Mi día | Reporte con foto · paso 2 («Realizar reporte») | Interactiva: tocas cualquier momento para elegirlo y reportar sobre él, «Confirmar subproceso» avanza la rutina y cada momento cuenta sus reportes. Antes de checar muestra «Aún el día no comienza» |
| Mi día → Reporte | Reporte con foto · pasos 3 a 6, ruta A, E1 y E2 | Escrito, dictado o con foto (cámara real). No deja continuar sin texto ni guardar sin niños, y los que faltaron no se pueden marcar |
| Mi sala | Chatear con los papás · paso 1 | Asistencia editable (la comparte el reporte) y botón de chat |
| Chats | Chatear con los papás · paso 2 | Conversación de muestra, todavía no envía mensajes |
| Menú | Registrar entrada · paso 1 | Perfil, estado de la entrada, checador, avisos de la fundación, mi perfil y cerrar sesión (reinicia el día del prototipo) |
| Menú → Checador | Registrar entrada · paso 2 y E2 | Sin ubicación ni mapa: un toque registra la entrada (tarde registra con motivo opcional) y otro la salida; a las 9 pm el servidor cierra solo lo que quedó abierto. El caso E1 («fuera del perímetro») ya no existe |
| Menú → Entrada registrada | Registrar entrada · paso 3 y E3 | Se queda en pantalla hasta que la docente toca «Ir a Mi día»; ya no se cierra sola |

## Estructura del código

Sigue la de las prácticas del curso: `domain/` (reglas en Kotlin puro), `data/` (repositorios que llaman a la API; ya no quedan repositorios en memoria), `ui/state/` (ViewModels), `ui/screens/` (pantallas que solo dibujan), `ui/components/` y `ui/navigation/`. Las reglas del dominio tienen pruebas en `app/src/test/java/mx/tec/codea/domain/` (`./gradlew test`).

Sin la key la app compila, pero el mapa sale vacío.

## Licencias

- La fuente **Poppins** (`app/src/main/res/font/poppins_*.ttf`) se usa bajo la SIL Open Font License 1.1: [`licencias/OFL-Poppins.txt`](licencias/OFL-Poppins.txt).
