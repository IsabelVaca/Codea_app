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
            MonthlySummaryCard(summaryText = monthData.summaryText)
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    selectedDayNumber: Int,
    onDayClick: (CalendarDay) -> Unit,
) {
    // August 2026 starts on Saturday (5 empty slots before day 1 in 0-indexed Monday-start grid).
    val startOffset = 5
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
        else -> MaterialTheme.colorScheme.outlineVariant
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
private fun MonthlySummaryCard(summaryText: String) {
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
                text = "Agosto en una línea",
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
