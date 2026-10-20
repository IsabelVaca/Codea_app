package mx.tec.codea.ui.screens.parent.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.MenuDelDia
import mx.tec.codea.ui.components.MenuCard
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.Pastilla
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.screens.parent.notifications.HoyHeader
import mx.tec.codea.ui.screens.parent.notifications.NotifUi
import mx.tec.codea.ui.screens.parent.notifications.PanelNotificaciones
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// "Hoy" del padre, con el estilo del admin: tarjetas blancas, lila con morado y sin etiquetas de sección.
@Composable
fun ParentTodayScreen(
    modifier: Modifier = Modifier,
    summary: DaySummary = ParentTodaySampleData.daySummary,
    timelineEvents: List<TimelineEvent> = ParentTodaySampleData.timelineEvents,
    // lo que se come ese día; null = todavía no hay menú y no se muestra nada.
    menu: MenuDelDia? = null,
    onNavigateToCalendar: () -> Unit = {},
    onSendNote: (String) -> Unit = {},
    // la campana: cuántas notificaciones no ha visto y la lista del panel.
    pendientes: Int = 0,
    notifs: List<NotifUi> = emptyList(),
    // se llama al tocar la campana (marca las notificaciones como vistas).
    onAbrirNotif: () -> Unit = {},
    // abre Mi cuenta (cambiar contraseña y cerrar sesión).
    onCuentaClick: (() -> Unit)? = null,
) {
    var panelAbierto by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HoyHeader(
                titulo = "El día de ${summary.childFirstName}",
                pendientes = pendientes,
                onAbrirNotif = {
                    onAbrirNotif()
                    panelAbierto = true
                },
                // el LazyColumn ya deja 20dp a los lados.
                horizontalPadding = 0.dp,
                onCuentaClick = onCuentaClick,
            )
        }

        item { FechaCard(fecha = summary.dateFormatted, onVerMes = onNavigateToCalendar) }

        item { NinoCard(summary = summary) }

        // solo las métricas que la maestra ya llenó; sin resumen del día no sale ninguna.
        val metricas = listOf(
            "Ánimo" to summary.mood,
            "Comidas" to summary.meals,
            "Pañal / baño" to summary.diaperOrBathroom,
            "Siesta" to summary.napDuration,
        ).filter { (_, valor) -> valor != "—" && valor.isNotBlank() }
        if (metricas.isNotEmpty()) {
            item { Metricas(metricas) }
        }

        menu?.let { item { MenuCard(menu = it) } }

        if (timelineEvents.isEmpty()) {
            item {
                Text(
                    "Todavía no hay actividades registradas este día.",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris,
                )
            }
        }
        items(items = timelineEvents, key = { it.id }) { event -> EventoCard(event) }

        item {
            AvisoDesdeCasa(
                nombre = summary.childFirstName,
                onEnviar = onSendNote,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
        }
    }

    if (panelAbierto) {
        PanelNotificaciones(notifs = notifs, onCerrar = { panelAbierto = false })
    }
    }
}

// el día que se está viendo y el atajo al calendario.
@Composable
private fun FechaCard(fecha: String, onVerMes: () -> Unit) {
    TarjetaBlanca(padding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                fecha, modifier = Modifier.weight(1f).padding(start = 4.dp),
                fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CodeaColores.Ink,
            )
            TextButton(onClick = onVerMes) {
                Text("Ver el mes", fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Morado)
            }
        }
    }
}

// el niño: iniciales en lila como las tarjetas del admin, su nombre, su sala y la nota del día.
@Composable
private fun NinoCard(summary: DaySummary) {
    TarjetaBlanca(espacio = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CodeaColores.Lila),
                contentAlignment = Alignment.Center,
            ) {
                Text(summary.childInitials, color = CodeaColores.Morado, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(summary.childFullName, fontFamily = Poppins, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                if (summary.childGroup.isNotBlank()) {
                    Text(summary.childGroup, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto)
                }
            }
        }
        // sin nota de la maestra, el aviso va en gris para que no parezca una nota.
        Text(
            summary.generalNote.ifBlank { "Todavía no hay notas de este día." },
            fontFamily = Poppins, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium,
            color = if (summary.generalNote.isBlank()) CodeaColores.Gris else CodeaColores.Ink,
        )
    }
}

// las métricas del día de dos en dos: etiqueta chica en gris y valor en morado.
@Composable
private fun Metricas(metricas: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metricas.chunked(2).forEach { fila ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                fila.forEach { (etiqueta, valor) ->
                    TarjetaBlanca(modifier = Modifier.weight(1f), padding = 14.dp, espacio = 4.dp) {
                        Text(etiqueta.uppercase(), fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp, color = CodeaColores.Gris)
                        Text(valor, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Morado)
                    }
                }
                // si queda una sola en la fila, ocupa la mitad igual que las demás.
                if (fila.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

// un momento del día: la hora en pastilla, el título y lo que escribió la maestra.
@Composable
private fun EventoCard(event: TimelineEvent) {
    TarjetaBlanca(padding = 14.dp, espacio = 6.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (event.time.isNotBlank()) Pastilla(event.time)
            Text(event.title, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink)
        }
        if (event.description.isNotBlank()) {
            Text(event.description, fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium, color = CodeaColores.GrisTexto)
        }
    }
}

// la nota de la familia para la asistente, con el mismo campo y botón que el formulario de Avisos.
@Composable
private fun AvisoDesdeCasa(nombre: String, onEnviar: (String) -> Unit, modifier: Modifier = Modifier) {
    var texto by remember { mutableStateOf("") }
    TarjetaBlanca(modifier = modifier, espacio = 10.dp) {
        Text("Aviso desde casa", fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink)
        BasicTextField(
            value = texto,
            onValueChange = { texto = it },
            textStyle = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CodeaColores.Ink),
            cursorBrush = SolidColor(CodeaColores.Violeta),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { interno ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CodeaColores.Campo)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (texto.isEmpty()) {
                        Text(
                            "Cuéntale a la asistente cómo amaneció $nombre…",
                            fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CodeaColores.Gris,
                        )
                    }
                    interno()
                }
            },
        )
        Button(
            onClick = {
                onEnviar(texto)
                texto = ""
            },
            enabled = texto.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Violeta, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp),
        ) {
            Text("Enviar nota de hoy", fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ParentTodayScreenPreview() {
    CodeaTheme {
        ParentTodayScreen()
    }
}
