package mx.tec.codea.ui.screens.parent.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.DiaSemana
import mx.tec.codea.domain.EstadoDia
import mx.tec.codea.domain.ResumenSemana
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// los colores de cada día: completo en morado, con nota en verde y ausente en rojo (los mismos del admin).
private fun colorDe(status: DayAttendanceStatus): Color? = when (status) {
    DayAttendanceStatus.FULL_DAY -> CodeaColores.Morado
    DayAttendanceStatus.WITH_NOTE -> CodeaColores.Verde
    DayAttendanceStatus.ABSENT -> CodeaColores.Rojo
    DayAttendanceStatus.NO_RECORD -> null
}

@Composable
fun ParentCalendarScreen(
    modifier: Modifier = Modifier,
    monthData: CalendarMonthData = ParentCalendarSampleData.monthData,
    initialSelectedDay: Int = 19,
    onDaySelected: (CalendarDay) -> Unit = {},
    resumenSemana: ResumenSemana? = null,
) {
    var selectedDayNumber by remember { mutableIntStateOf(initialSelectedDay) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(title = "Calendario de ${monthData.childFirstName}")
        }

        item {
            MesCard(
                mes = monthData.monthName,
                days = monthData.days,
                startOffset = monthData.startOffset,
                selectedDayNumber = selectedDayNumber,
                onDayClick = { day ->
                    selectedDayNumber = day.dayNumber
                    onDaySelected(day)
                },
            )
        }

        if (monthData.summaryText.isNotBlank()) {
            item {
                TarjetaBlanca(espacio = 6.dp) {
                    Text(
                        // "Octubre 2026" -> "Octubre"
                        monthData.monthName.substringBefore(' ').ifBlank { "El mes" },
                        fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink,
                    )
                    Text(monthData.summaryText, fontFamily = Poppins, fontSize = 13.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = CodeaColores.GrisTexto)
                }
            }
        }

        if (resumenSemana != null) {
            item { SemanaCard(resumen = resumenSemana) }
        }
    }
}

// el mes en una tarjeta: su nombre, la cuadrícula y la leyenda de los colores.
@Composable
private fun MesCard(
    mes: String,
    days: List<CalendarDay>,
    startOffset: Int,
    selectedDayNumber: Int,
    onDayClick: (CalendarDay) -> Unit,
) {
    TarjetaBlanca(espacio = 8.dp) {
        if (mes.isNotBlank()) {
            Text(mes, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink, modifier = Modifier.padding(bottom = 4.dp))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { letra ->
                Text(
                    letra, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Gris,
                )
            }
        }
        val total = startOffset + days.size
        var inicio = 0
        while (inicio < total) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (col in 0 until 7) {
                    val slot = inicio + col
                    if (slot in startOffset until total) {
                        val day = days[slot - startOffset]
                        DiaCelda(day, day.dayNumber == selectedDayNumber, { onDayClick(day) }, Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            inicio += 7
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Leyenda(CodeaColores.Morado, "Completo")
            Leyenda(CodeaColores.Verde, "Con nota")
            Leyenda(CodeaColores.Rojo, "No asistió")
        }
    }
}

// un día: el elegido en morado; los que tienen registro en lila con su punto de color; los demás, en gris.
@Composable
private fun DiaCelda(day: CalendarDay, elegido: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val conRegistro = day.status != DayAttendanceStatus.NO_RECORD
    Column(
        modifier = modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    elegido -> CodeaColores.Morado
                    conRegistro -> CodeaColores.Campo
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            day.dayNumber.toString(),
            fontFamily = Poppins, fontSize = 14.sp,
            fontWeight = if (conRegistro || elegido) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = when {
                elegido -> Color.White
                conRegistro -> CodeaColores.Ink
                else -> CodeaColores.Gris
            },
        )
        val punto = if (elegido && conRegistro) Color.White else colorDe(day.status)
        Spacer(Modifier.height(3.dp))
        Box(Modifier.size(6.dp).clip(CircleShape).background(punto ?: Color.Transparent))
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(texto, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto)
    }
}

// el resumen de la semana (RF-12): cómo estuvo cada día y los totales, con el estilo de los datos del admin.
@Composable
private fun SemanaCard(resumen: ResumenSemana) {
    TarjetaBlanca(espacio = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Resumen de la semana", fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink)
            Text(resumen.rango, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            resumen.dias.forEach { dia -> DiaDeLaSemana(dia) }
        }
        Linea("Asistió", "${resumen.diasAsistidos} ${if (resumen.diasAsistidos == 1) "día" else "días"}" +
            if (resumen.diasAusente > 0) " · faltó ${resumen.diasAusente}" else "")
        // lo que la maestra no registró en la semana ("—") no se muestra.
        listOf("Ánimo" to resumen.animo, "Comidas" to resumen.comidas, "Siesta promedio" to resumen.siesta)
            .filter { (_, valor) -> valor.isNotBlank() && valor != "—" }
            .forEach { (etiqueta, valor) -> Linea(etiqueta, valor) }
        if (resumen.cambios > 0) Linea("Cambios de pañal", resumen.cambios.toString())
        if (resumen.notasFamilia > 0) Linea("Tus notas", resumen.notasFamilia.toString())
        if (resumen.logros.isNotEmpty()) {
            Text("Logros de la semana", fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink, modifier = Modifier.padding(top = 2.dp))
            resumen.logros.forEach { logro ->
                Text("• $logro", fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium, color = CodeaColores.Ink)
            }
        }
    }
}

// un día de la semana: la letra y una pastilla con la marca (asistió, faltó o sin registro).
@Composable
private fun DiaDeLaSemana(dia: DiaSemana) {
    val (marca, fondo, color) = when (dia.estado) {
        EstadoDia.ASISTIO -> Triple("✓", CodeaColores.Lila, CodeaColores.Morado)
        EstadoDia.FALTO -> Triple("✗", CodeaColores.RojoSuave, CodeaColores.Rojo)
        EstadoDia.SIN_REGISTRO -> Triple("·", CodeaColores.Campo, CodeaColores.Gris)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(dia.letra, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Gris)
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(fondo),
            contentAlignment = Alignment.Center,
        ) {
            Text(marca, fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// una línea del resumen: la etiqueta en gris a la izquierda y el valor en negritas a la derecha.
@Composable
private fun Linea(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto)
        Text(valor, fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CodeaColores.Ink, textAlign = TextAlign.End, modifier = Modifier.padding(start = 12.dp))
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ParentCalendarScreenPreview() {
    CodeaTheme {
        ParentCalendarScreen()
    }
}
