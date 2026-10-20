package mx.tec.codea.ui.screens.parent.calendar

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import mx.tec.codea.ui.theme.Amber
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Coral
import mx.tec.codea.ui.theme.Teal
import mx.tec.codea.ui.theme.TealDeep
import mx.tec.codea.ui.theme.TealWash

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
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            ScreenHeader(
                overline = "ESTANCIA LA ORUGA · FAMILIA",
                title = "Calendario de ${monthData.childFirstName}",
            )
        }

        item {
            CalendarGrid(
                days = monthData.days,
                startOffset = monthData.startOffset,
                selectedDayNumber = selectedDayNumber,
                onDayClick = { day ->
                    selectedDayNumber = day.dayNumber
                    onDaySelected(day)
                },
            )
        }

        item {
            AttendanceLegendCard()
        }

        item {
            MonthlySummaryCard(
                // "Octubre 2026" -> "Octubre"
                monthText = monthData.monthName.substringBefore(' ').ifBlank { "El mes" },
                summaryText = monthData.summaryText,
            )
        }

        if (resumenSemana != null) {
            item {
                WeeklySummaryCard(resumen = resumenSemana)
            }
        }
    }
}

// el resumen de la semana (RF-12): cómo estuvo cada día y los totales, en palabras.
@Composable
private fun WeeklySummaryCard(resumen: ResumenSemana) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Resumen de la semana",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = resumen.rango,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                resumen.dias.forEach { dia -> DiaDeLaSemana(dia) }
            }
            WeeklyLine("Asistió", "${resumen.diasAsistidos} ${if (resumen.diasAsistidos == 1) "día" else "días"}" +
                if (resumen.diasAusente > 0) " · faltó ${resumen.diasAusente}" else "")
            WeeklyLine("Ánimo", resumen.animo)
            WeeklyLine("Comidas", resumen.comidas)
            WeeklyLine("Siesta promedio", resumen.siesta)
            if (resumen.cambios > 0) WeeklyLine("Cambios de pañal", resumen.cambios.toString())
            if (resumen.notasFamilia > 0) WeeklyLine("Tus notas", resumen.notasFamilia.toString())
            if (resumen.logros.isNotEmpty()) {
                Text(
                    text = "Logros de la semana",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp),
                )
                resumen.logros.forEach { logro ->
                    Text(
                        text = "• $logro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaDeLaSemana(dia: DiaSemana) {
    val (marca, color) = when (dia.estado) {
        EstadoDia.ASISTIO -> "✓" to Teal
        EstadoDia.FALTO -> "✗" to Coral
        EstadoDia.SIN_REGISTRO -> "·" to MaterialTheme.colorScheme.outline
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dia.letra,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = marca,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color,
        )
    }
}

@Composable
private fun WeeklyLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    selectedDayNumber: Int,
    onDayClick: (CalendarDay) -> Unit,
    startOffset: Int,
) {
    val totalSlots = startOffset + days.size
    val weekDays = listOf("L", "M", "M", "J", "V", "S", "D")

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            weekDays.forEach { dayLetter ->
                Text(
                    text = dayLetter,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        var slotIndex = 0
        while (slotIndex < totalSlots) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (col in 0 until 7) {
                    val currentSlot = slotIndex + col
                    if (currentSlot >= startOffset && currentSlot < totalSlots) {
                        val dayIndex = currentSlot - startOffset
                        val day = days[dayIndex]
                        CalendarDayCell(
                            day = day,
                            isSelected = day.dayNumber == selectedDayNumber,
                            onClick = { onDayClick(day) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            slotIndex += 7
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isHasRecord = day.status != DayAttendanceStatus.NO_RECORD

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isHasRecord -> MaterialTheme.colorScheme.surfaceContainerLowest
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isHasRecord -> MaterialTheme.colorScheme.onSurface
        // los días sin registro se leen en claro y en oscuro, pero más suaves que los que tienen algo.
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    val dotColor = when {
        isSelected -> Color.White
        day.status == DayAttendanceStatus.FULL_DAY -> Teal
        day.status == DayAttendanceStatus.WITH_NOTE -> Amber
        day.status == DayAttendanceStatus.ABSENT -> Coral
        else -> null
    }

    val border = when {
        isSelected -> BorderStroke(1.5.dp, Color.Black)
        isHasRecord -> BorderStroke(1.2.dp, Color.Black)
        else -> null
    }

    Surface(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = border,
        shadowElevation = if (isHasRecord && !isSelected) 2.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isHasRecord || isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
            )

            if (dotColor != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, CircleShape,)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun AttendanceLegendCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LegendRow(
                dotColor = Teal,
                label = "Día completo, sin novedad",
            )
            LegendRow(
                dotColor = Amber,
                label = "Con nota de la maestra",
            )
            LegendRow(
                dotColor = Coral,
                label = "No asistió",
            )
        }
    }
}

@Composable
private fun LegendRow(
    dotColor: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MonthlySummaryCard(monthText: String, summaryText: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = TealWash,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "$monthText en una línea",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TealDeep,
            )
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = TealDeep.copy(alpha = 0.85f),
                lineHeight = 21.sp,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ParentCalendarScreenPreview() {
    CodeaTheme {
        ParentCalendarScreen()
    }
}
