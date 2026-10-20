# Documentación de Codea

## Índice

| Documento | Para qué sirve |
|---|---|
| [`GUIA-CONTINUAR.md`](GUIA-CONTINUAR.md) | Empieza aquí: cómo levantar todo, mapa del repo y trampas |
| [`ESTADO.md`](ESTADO.md) | Qué está hecho y qué falta por área (foto del 20/oct) |
| [`FALTANTES.md`](FALTANTES.md) | Qué falta, por requisito, por rol y para la entrega, y en qué orden hacerlo |
| [`REQUISITOS.md`](REQUISITOS.md) | Los 32 requisitos y su estado |
| [`PLAN-ACCION.md`](PLAN-ACCION.md) | Bloques de trabajo hasta la entrega, con lo hecho marcado |
| [`BITACORA.md`](BITACORA.md) | Qué se hizo cada día, con lo que se probó |
| [`RECORRIDO.md`](RECORRIDO.md) | Lo que se probó en el teléfono y las fallas que salieron |
| [`DATOS-DE-PRUEBA.md`](DATOS-DE-PRUEBA.md) | Las cuentas y los datos del seed |
| [`MARCO-TECNICO.md`](MARCO-TECNICO.md) | Qué tecnologías se permiten (startdroid) y cómo está el proyecto frente a eso |
| [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) | Excepciones al marco que esperan aprobación |
| [`ENTREGA-3.md`](ENTREGA-3.md) | Checklist de la rúbrica final; los entregables están en [`entrega/`](entrega/) |
| [`presentacion/index.html`](presentacion/index.html) | Presentación final (15 min) en HTML, con capturas reales de la app; se abre en el navegador (← → avanzar, N notas, F pantalla completa) |
| [`PLAN.md`](PLAN.md) | Plan del 16 al 22 de octubre |
| [`INCIDENTE-2026-10-19.md`](INCIDENTE-2026-10-19.md) | Cuentas que no se activaban, servidores mezclados y datos borrados en producción: qué pasó y cómo se corrigió |

> La sección de prototipos de abajo es de la primera etapa: el prototipo es solo del turno de la docente. Para el estado real de todas las pantallas ve [`ESTADO.md`](ESTADO.md) y los README de `app/src/main/java/mx/tec/codea/ui/screens/`.

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
| [`api/endpoints.md`](api/endpoints.md) | **Referencia vigente.** Los endpoints que tiene hoy la API (84 rutas al 20 de octubre), sacados del código: rol, qué envían, qué responden y sus códigos de error. |
| [`api/endpoints-api-codea.pdf`](api/endpoints-api-codea.pdf) | Histórico. Endpoints de la primera entrega por rol, con las rutas anteriores; ya no coincide en varios detalles con el código. |
| [Propuesta de arquitectura: API de la guardería](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H) | **Histórico** (propuesta inicial; el login quedó con JWT propio, no con Firebase). Arquitectura completa: componentes, login con Firebase, convenciones, modelo de datos, fotos con S3, monitoreo y seguridad. |

El contrato vivo es el OpenAPI que genera FastAPI: en el servidor compartido, `https://api.codeaapp.site/docs`; con la API local, `http://localhost:8000/docs`.

### Pantallas de la app y su referencia en el prototipo

| Tab | Pantalla en el prototipo | Estado |
| --- | --- | --- |
| Mi día | Reporte con foto · paso 2 («Realizar reporte») | Pase de lista, rutina del día (el momento elegido en violeta, «Confirmar subproceso» avanza) y, al final, el pase de salida y «Finalizar día». Antes de checar muestra «Aún el día no comienza» |
| Mi día → Reporte | Reporte con foto · pasos 3 a 6, ruta A, E1 y E2 | Escrito, dictado o con foto (cámara real). No deja continuar sin texto ni guardar sin niños, y los que faltaron no se pueden marcar |
| Mi sala | Chatear con los papás · paso 1 | Rediseñada: cuadrícula de niños con filtros, avisos de casa, ficha de cada niño (salud, asistencia, quién lo recoge, llamar) y resumen del día guiado |
| Chats | Chatear con los papás · paso 2 | Bandeja por niño con sus tutores; envía y recibe texto y fotos por la API (sin tiempo real: consulta al abrir) |
| Menú | Registrar entrada · paso 1 | Perfil y estado de la entrada, checador, Mi perfil (Mi cuenta: datos y contraseña), avisos de la fundación y cerrar sesión |
| Menú → Checador | Registrar entrada · paso 2 y E2 | Sin ubicación ni mapa: un toque registra la entrada (tarde registra con motivo opcional) y otro la salida; a las 9 pm el servidor cierra solo lo que quedó abierto. El caso E1 («fuera del perímetro») ya no existe |
| Menú → Entrada registrada | Registrar entrada · paso 3 y E3 | Se queda en pantalla hasta que la docente toca «Ir a Mi día»; ya no se cierra sola |

## Estructura del código

Sigue la de las prácticas del curso: `domain/` (reglas en Kotlin puro), `data/` (repositorios que llaman a la API; ya no quedan repositorios en memoria), `ui/state/` (ViewModels), `ui/screens/` (pantallas que solo dibujan), `ui/components/` y `ui/navigation/`. Las reglas del dominio tienen pruebas en `app/src/test/java/mx/tec/codea/domain/` (`./gradlew test`).


## Licencias

- La fuente **Poppins** (`app/src/main/res/font/poppins_*.ttf`) se usa bajo la SIL Open Font License 1.1: [`licencias/OFL-Poppins.txt`](licencias/OFL-Poppins.txt).
