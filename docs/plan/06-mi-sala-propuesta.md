# Propuesta: rediseño de "Mi sala" (docente)

> **Estado (18 de octubre de 2026): implementado**, pendiente de revisar en el teléfono. Ver [Decisiones tomadas](#decisiones-tomadas) y la entrada de la [bitácora](../BITACORA.md). El resto del documento es la propuesta tal como se revisó.

Propuesta del 18 de octubre de 2026. Este documento se escribió para revisarlo y cambiarlo antes de escribir código.

Sigue las convenciones de [`../estilo.md`](../estilo.md) y complementa a [`01-app-pantallas.md`](01-app-pantallas.md).

## 1. El problema

Hoy "Mi sala" (`MyRoomScreen.kt`) es una lista de tarjetas iguales. Cada una repite lo mismo: nombre, dos botones **Presente / Falta**, un enlace "Resumen del día ›" y un botón Chat. Arriba hay dos cuadros con números ("presentes" y "chats sin leer").

Lo que está mal desde el punto de vista de la docente:

| Hoy | Por qué estorba |
| --- | --- |
| **Presente / Falta en cada tarjeta** | El pase de lista ya se hace en *Mi día* como primer paso. Aquí es un botón que se toca sin querer y que ocupa la mitad de la tarjeta. |
| **Los dos cuadros con números** | Son datos que la docente ya sabe contar con los ojos (cuántos niños hay, si hay un punto rojo en un chat). No le dicen qué hacer. |
| **Los avisos de los papás son una etiqueta** | "Nota de la familia hoy" no dice qué nota es. Para leerla hay que abrir *Resumen del día*, que es otra pantalla pensada para *capturar*, no para *leer*. |
| **Todas las tarjetas pesan igual** | El niño que no trae ningún aviso se ve igual que el que trae una alergia, una nota de la mamá y un chat sin leer. |
| **Tres destinos escondidos** | Resumen, chat y asistencia están en el mismo renglón, con textos chicos. Una persona con prisa y con un niño en brazos no acierta. |

## 2. Qué debe resolver la pantalla

*Mi sala* responde tres preguntas, en este orden:

1. **¿Qué tengo que atender ahora?** Avisos de casa, chats sin leer.
2. **¿Cómo está cada niño?** Quién está, quién tiene algo especial (alergia, aviso), a quién le falta su resumen.
3. **¿Qué quiero hacer con este niño?** Leer su aviso, escribirle a la familia, capturar su resumen, corregir su asistencia.

Principios de diseño (para una persona que usa la app de pie, con prisa y con las manos ocupadas):

- **Una sola columna que se lee de arriba abajo.** Sin pestañas dentro de la pantalla, sin menús escondidos.
- **Lo importante arriba, con su texto completo.** Si un papá avisó algo, se lee ahí mismo; no hay que abrir nada.
- **Una acción principal por fila.** Tocar el renglón del niño abre su ficha. Lo demás son atajos claros.
- **Objetivos de toque grandes** (mínimo 48 dp; renglones de unos 72 dp).
- **Nada se entiende solo por el color.** Cada estado lleva icono y texto además del color (también para el modo oscuro).
- **Sin cuadros de números.** El resumen del grupo es una línea de texto, no una tarjeta.
- **Textos en español corriente**, de `strings.xml` (nada de texto en el código, como pide el estilo del proyecto).

## 3. Mapa de pantallas

```text
Mi sala (tab)
 ├── Ficha del niño ──────────────┬── Chat con la familia   (ya existe: tab Chats)
 │                                ├── Resumen del día       (ya existe: ResumenDelDiaScreen)
 │                                └── Corregir asistencia   (diálogo, no pantalla)
 └── Avisos de casa               (bandeja de las notas de los papás de hoy)
```

Son **2 pantallas nuevas** (Ficha del niño y Avisos de casa), **1 diálogo nuevo** (Corregir asistencia) y **2 que se reutilizan tal cual** (Chat y Resumen del día).

## 4. Pantalla 1 · Mi sala

```text
┌──────────────────────────────────────────┐
│ Estancia La Oruga                        │
│ Mi sala · Maternal                       │
│ Lunes 19 de octubre · 11 de 12 niños hoy │   ← texto, no tarjeta
├──────────────────────────────────────────┤
│ ⓘ 2 cambios sin enviar · se mandan solos │   ← solo si hay pendientes
│                                          │
│ DE CASA HOY                    Ver todos ›│
│ ┌──────────────────────────────────────┐ │
│ │ (SM)  Sofía Márquez           7:40   │ │
│ │ "Hoy casi no desayunó y tiene un     │ │
│ │  poco de tos. Si le sube, avísenme." │ │
│ │                                      │ │
│ │ [ Responder ]        [ Ya la vi ✓ ]  │ │
│ └──────────────────────────────────────┘ │
│ ┌──────────────────────────────────────┐ │
│ │ (MR)  Mateo Ruiz              8:05   │ │
│ │ "Lo recoge su abuelita Rosa."        │ │
│ │ [ Responder ]        [ Ya lo vi ✓ ]  │ │
│ └──────────────────────────────────────┘ │
│                                          │
│ [ Todos 12 ] [ Con aviso 2 ] [ Chat 3 ]  │   ← filtros (chips)
│ [ Sin resumen 5 ]                        │
│                                          │
│ LOS NIÑOS                                │
│ ┌──────────────────────────────────────┐ │
│ │ (SM) Sofía Márquez                   │ │
│ │      ● Aviso · ⚠ Alergia: nuez       │ │
│ │      ○ Resumen pendiente        (💬●)│›│
│ ├──────────────────────────────────────┤ │
│ │ (MR) Mateo Ruiz                      │ │
│ │      ● Aviso · ✓ Resumen listo  (💬) │›│
│ ├──────────────────────────────────────┤ │
│ │ (AL) Ana López                       │ │
│ │      ○ Resumen pendiente             │›│
│ │ …                                    │ │
│ └──────────────────────────────────────┘ │
│                                          │
│ AUSENTES HOY (1)                      ⌄ │   ← plegado
└──────────────────────────────────────────┘
```

### 4.1 Encabezado

- Sala y fecha de hoy (la sala sale de `GET /me`, como ya se hace).
- Una línea: **"11 de 12 niños hoy"**. Si hay ausentes se cuenta aquí y se listan al final. Es texto, no una tarjeta.
- Si hay marcas de asistencia sin enviar (modo sin conexión), una franja discreta con el mismo mensaje que hay hoy.

### 4.2 "De casa hoy" (lo que pidiste primero)

Es lo primero que se ve después del encabezado. Una **tarjeta por cada aviso que un papá mandó desde casa hoy** (la nota que el padre deja con `POST /children/{id}/notes`):

- Avatar con iniciales, nombre del niño, hora en que llegó y **el texto completo** del aviso.
- Dos botones: **Responder** (abre el chat de ese niño) y **Ya lo vi** (la tarjeta baja a la lista y deja de ocupar lugar arriba).
- Se muestran máximo 3; si hay más, un renglón **"Ver todos (5) ›"** abre la bandeja *Avisos de casa*.
- Si no hay avisos, la sección **no aparece** (no se deja un hueco vacío).

### 4.3 Filtros

Cuatro chips en una fila que se desplaza si no cabe: **Todos · Con aviso · Con chat · Sin resumen**. Cada chip lleva su cuenta. Sirven para el fin del día ("¿a quién le falta su resumen?") sin crear otra pantalla.

### 4.4 "Los niños"

Una lista de renglones (no tarjetas sueltas con borde), separados por una línea fina. Cada renglón:

- **Avatar** con iniciales (ya existe `InitialsAvatar`).
- **Nombre** en negrita.
- **Segunda línea con hasta 3 etiquetas cortas**, solo las que apliquen:
  - `● Aviso` (hay aviso de casa de hoy; cambia a `✓ Aviso visto` cuando la docente lo marca).
  - `⚠ Alergia: …` (siempre visible si el niño tiene alergias registradas; ver decisión 4).
  - `○ Resumen pendiente` / `✓ Resumen listo`.
- **Atajo de chat** a la derecha (icono de burbuja, con punto si hay mensajes sin leer). Es el único atajo; todo lo demás se hace desde la ficha.
- **Tocar el renglón** abre la *Ficha del niño*.

Orden de la lista: primero los niños con algo pendiente (aviso sin ver, chat sin leer); después los demás, por nombre. Así lo urgente nunca queda abajo.

### 4.5 "Ausentes hoy"

Sección plegada al final. Los niños ausentes salen de la lista principal para que no estorben, se ven atenuados y con la etiqueta "Ausente". Tocar uno abre su ficha (por si hubo error o la familia quiere dejar un mensaje).

### 4.6 Qué se quita

Los dos cuadros con números, los botones **Presente / Falta** de cada niño, el enlace "Resumen del día ›" y el botón de chat grande.

## 5. Pantalla 2 · Ficha del niño

Se abre al tocar un renglón. Es el lugar donde se hace todo lo del niño.

```text
┌──────────────────────────────────────────┐
│ ← Mi sala                                │
│                                          │
│ (SM)  Sofía Márquez                      │
│       2 años 4 meses · Maternal          │
│                                          │
│ ┌──────────────────────────────────────┐ │
│ │ ⚠ ALERGIAS                           │ │   ← solo si hay
│ │ Nuez y lácteos                       │ │
│ └──────────────────────────────────────┘ │
│                                          │
│ AVISO DE LA FAMILIA · hoy 7:40           │   ← solo si hay
│ "Hoy casi no desayunó y tiene un poco    │
│  de tos. Si le sube, avísenme."          │
│                    [ Ya lo vi ✓ ]        │
│                                          │
│ ┌──────────────────────────────────────┐ │
│ │ 💬  Escribir a la familia          › │ │   ← acción principal
│ └──────────────────────────────────────┘ │
│                                          │
│ HOY                                      │
│  Resumen del día   ○ Pendiente  Capturar›│
│  Asistencia        Presente   Corregir › │
│                                          │
│ QUIÉN LA RECOGE                          │
│  Mamá · Laura Márquez        [ Llamar ]  │
│  Abuela · Rosa Pérez                     │
└──────────────────────────────────────────┘
```

Contenido, de arriba abajo:

1. **Identidad:** avatar, nombre, edad y sala.
2. **Alergias** en un recuadro que se nota (color de atención, icono y texto). No aparece si no hay.
3. **Aviso de la familia** de hoy, completo, con **Ya lo vi**. Si hay varios, van uno debajo de otro, el más reciente primero.
4. **Escribir a la familia:** botón ancho, el principal de la pantalla. Lleva al chat de ese niño. Si el niño aún no tiene chat se avisa con el mensaje que ya existe ("Este niño todavía no tiene chat").
5. **Hoy:** dos renglones.
   - *Resumen del día:* estado (pendiente / listo con la hora) y **Capturar** o **Editar**. Abre la pantalla que ya existe.
   - *Asistencia:* muestra el estado de hoy y **Corregir** (ver 6).
6. **Quién la recoge:** las personas autorizadas (`GET /children/{id}/authorized-pickups`, que la docente ya puede leer) y el teléfono del tutor para **Llamar** (`GET /children/{id}` trae `guardians` con teléfono). Ver decisión 5.

Si el niño está **ausente**, la ficha lo dice arriba ("Hoy no vino") y *Capturar resumen* queda desactivado con el motivo, porque la API no deja reportar a un niño ausente.

## 6. Diálogo · Corregir asistencia

Para el caso especial que mencionas: el pase de lista se hace en *Mi día*; aquí solo se **corrige**.

```text
┌──────────────────────────────────────┐
│ Corregir asistencia de Sofía         │
│                                      │
│ El pase de lista se hace en Mi día.  │
│ Usa esto solo si hubo un error.      │
│                                      │
│   ( ) Presente      (•) Ausente      │
│                                      │
│        [ Cancelar ]   [ Guardar ]    │
└──────────────────────────────────────┘
```

- Es un diálogo que se abre desde la ficha, no un botón siempre visible en la lista. Hace falta dos toques deliberados para cambiar la asistencia, así que ya no se marca por accidente.
- Sigue funcionando sin conexión: se guarda en el teléfono y se manda cuando vuelva la señal (la cola que ya existe).

## 7. Pantalla 3 · Avisos de casa

Bandeja con todas las notas de los papás de hoy, para cuando hay más de tres.

```text
┌──────────────────────────────────────────┐
│ ← Mi sala                                │
│ Avisos de casa · hoy                     │
│                                          │
│ SIN VER (2)                              │
│ ┌──────────────────────────────────────┐ │
│ │ (SM) Sofía Márquez · 7:40            │ │
│ │ "Hoy casi no desayunó…"              │ │
│ │ [ Responder ]       [ Ya la vi ✓ ]   │ │
│ └──────────────────────────────────────┘ │
│ …                                        │
│                                          │
│ YA VISTOS (3)                            │
│  (AL) Ana López · 7:55                   │
│  "Trae su cobija nueva."                 │
│ …                                        │
└──────────────────────────────────────────┘
```

Es la misma tarjeta de la pantalla 1, repetida en una lista. Si no hay avisos: un mensaje amable ("Hoy ninguna familia dejó avisos") en lugar de una pantalla vacía.

## 8. Qué datos hay y qué falta

Lo que Mi sala recibe hoy (`GET /classrooms/{id}/children`): nombre, asistencia, conversación, chat sin leer, **cuántas** notas dejó el papá (`notes_today`) y si hay alguna (`has_note_today`). **No trae el texto del aviso, ni las alergias, ni la edad, ni si el resumen ya se capturó.**

| Dato de la pantalla | ¿Existe? | Cómo se obtiene |
| --- | --- | --- |
| Nombre, avatar, chat sin leer | Sí | Ya viene en `RoomChild` |
| Texto y hora del aviso de casa | Hay que agregarlo | Hoy solo se obtiene con `GET /children/{id}/day` **uno por niño** (12 llamadas). Propuesta: que `RoomChild` traiga `notes: [{id, text, created_at}]` |
| Alergias y edad | Hay que agregarlo | Hoy solo en `GET /children/{id}`. Propuesta: `allergies` y `age` en `RoomChild` |
| ¿El resumen ya está capturado? | Hay que agregarlo | Propuesta: `has_summary_today` en `RoomChild` |
| Último mensaje del chat | Sí | `GET /conversations` ya trae `last_message` |
| Autorizados para recoger | Sí | `GET /children/{id}/authorized-pickups`, solo al abrir la ficha |
| Teléfono del tutor | Sí | `GET /children/{id}` → `guardians`, solo al abrir la ficha |
| "Ya lo vi" de un aviso | No | Ver decisión 2 |
| "Hoy aún no se pasa lista" | No | Ver decisión 1 |

Cambios que esto implica:

- **API:** ampliar `RoomChild` con `notes`, `allergies`, `age` y `has_summary_today` (cambio chico, con sus pruebas).
- **App:** `RoomChildDto` y el modelo `Child` ganan esos campos; la caché de Room para uso sin conexión necesita una **migración de esquema (2 → 3)** para guardarlos; y la caché sigue borrándose al cerrar sesión, como ya está.
- **Pantallas nuevas:** `FichaNinoScreen`, `AvisosDeCasaScreen` y el diálogo de asistencia, cada una con su `ViewModel` (StateFlow), siguiendo `docs/estilo.md`. `MyRoomScreen` se reescribe completa.
- **Textos:** todo a `strings.xml` (hoy `MyRoomScreen` tiene varios textos fijos en el código, como "Resumen del día ›").

## 9. Cómo se ve y se siente

- Misma paleta del proyecto (violeta de marca, verde = listo, ámbar = atención, coral = importante). Se revisa en **modo claro y oscuro**.
- Renglones de lista con separador fino en lugar de una tarjeta con borde por niño: se ve más ligero y caben más niños sin perder el orden.
- Las tarjetas grandes se reservan para lo que pide acción (los avisos). Así el ojo cae primero ahí.
- Transiciones suaves al marcar "Ya lo vi" (la tarjeta se desvanece y la lista se reacomoda con `animateItem`, que ya se usa en *Mi día*).
- Texto mínimo de 14 sp en cuerpo y 12 sp en etiquetas; nada de texto blanco sobre fondo claro (falla ya corregida en el recorrido del 18 de octubre).

## 10. Orden de implementación sugerido

Cada paso se puede probar solo.

| Paso | Qué se hace | Se termina cuando |
| --- | --- | --- |
| 1 | API: ampliar `RoomChild` (+ pruebas) | La API devuelve los avisos, las alergias, la edad y el estado del resumen |
| 2 | App: DTO, modelo, mapeo y migración de Room | Mi sala sin conexión conserva los campos nuevos |
| 3 | `MyRoomScreen` nueva: encabezado, "De casa hoy", lista y filtros | La pantalla 1 se ve como el dibujo, en claro y oscuro |
| 4 | Ficha del niño + diálogo de corregir asistencia | Se puede corregir una asistencia desde la ficha, también sin conexión |
| 5 | Avisos de casa + "Ya lo vi" | Un aviso visto baja de la sección de arriba |
| 6 | Pruebas de dominio (orden de la lista, filtros) y revisión en el teléfono | Pruebas en verde y recorrido documentado en `docs/RECORRIDO.md` |

## Decisiones tomadas

Respuestas a lo que se preguntó al revisar la propuesta, y cómo quedó:

| # | Decisión | Cómo quedó |
| --- | --- | --- |
| 1 | Pase de lista | Va en **Mi día** (tarjeta «Pase de lista» arriba de la rutina → pantalla `RollCallScreen`). **Sin tercer estado**: un niño sin marca cuenta como **ausente** en toda la API |
| 2 | «Ya lo vi» | Solo en el teléfono (`SeenNotesStore`, DataStore, por usuario y solo con ids) |
| 3 | Avisos viejos | Solo los **del día** (el servidor solo manda los de hoy y el teléfono olvida los ids de días anteriores) |
| 4 | Alergias | Solo dentro de la **ficha**, no en la lista |
| 5 | Teléfono y autorizados | **Sí**: la ficha trae «Familia» y «Quién lo recoge», con botón **Llamar** (abre el marcador; no pide permisos) |
| 6 | Resumen guiado | **Pendiente**: no se implementó; ver la explicación en la bitácora |

Cambios de la API que salieron de esto: `RoomChild` ganó `notes[]` y `has_summary_today` (no hizo falta `allergies` ni `age`: la ficha los pide con `GET /children/{id}`), y la regla «sin registro = ausente» cambió Mi sala, el día del niño, los reportes (`CHILD_ABSENT`), el calendario y el resumen semanal.

## Lo que se preguntó al revisar (original)

1. **El pase de lista en *Mi día*.** Dices que ya se hace ahí como primer paso, pero **en el código de `main` no lo encuentro**: hoy la única pantalla que marca asistencia es *Mi sala* (`setAttendance` solo se llama desde ahí). ¿Está en otra rama o todavía hay que hacerlo? Si no está, hay que construirlo **antes** de quitar los botones de aquí, o la docente se queda sin forma de pasar lista.
   Además, la API da por *presente* a todo niño que no tenga una falta registrada, así que no distingue "ya pasé lista" de "todavía no". Si quieres un aviso tipo *"Aún no pasas lista hoy"* hace falta un tercer estado (sin registrar) en la API.
2. **"Ya lo vi" de los avisos.** Opción A: se guarda **solo en el teléfono** (rápido, sin tocar la API; si la docente cambia de teléfono se vuelven a ver). Opción B: se guarda **en el servidor** (queda registro y lo ven otras asistentes de la misma sala; cuesta un campo y un endpoint más). Recomiendo A para la entrega y B después.
3. **Avisos viejos.** ¿"De casa hoy" muestra solo las notas **de hoy**, o también las que quedaron sin ver de días anteriores?
4. **Alergias en la lista.** ¿Se muestran siempre en el renglón (más seguro, pero la lista se ve más cargada) o solo dentro de la ficha?
5. **Teléfono y autorizados en la ficha.** ¿Quieres el botón **Llamar** y la lista de quién recoge, o prefieres dejar a la docente solo con el chat? Son datos de contacto de las familias; si la docente no debe verlos, se quita esa sección.
6. **Resumen del día guiado.** Opcional: un botón **"Capturar resúmenes"** que avanza de un niño al siguiente sin regresar a la lista (útil al final del día). ¿Lo incluyo en esta tanda o queda para después?
