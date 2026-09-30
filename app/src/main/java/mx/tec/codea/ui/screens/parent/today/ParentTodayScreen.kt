package mx.tec.codea.ui.screens.parent.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Coral
import mx.tec.codea.ui.theme.CoralDeep
import mx.tec.codea.ui.theme.CoralWash
import mx.tec.codea.ui.theme.Lavender
import mx.tec.codea.ui.theme.Teal
import mx.tec.codea.ui.theme.TealDeep
import mx.tec.codea.ui.theme.TealWash
import mx.tec.codea.ui.theme.VioletDeep

@Composable
fun ParentTodayScreen(
    modifier: Modifier = Modifier,
    summary: DaySummary = ParentTodaySampleData.daySummary,
    timelineEvents: List<TimelineEvent> = ParentTodaySampleData.timelineEvents,
    onNavigateToCalendar: () -> Unit = {},
    onSendNote: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                overline = "ESTANCIA LA ORUGA · FAMILIA",
                title = "El día de ${summary.childFirstName}",
            )
        }

        item {
            DateBannerCard(
                dateText = summary.dateFormatted,
                onNavigateToCalendar = onNavigateToCalendar,
            )
        }

        item {
            ChildSummaryCard(summary = summary)
        }

        item {
            MetricsGrid(summary = summary)
        }

        item {
            Text(
                text = "SU DÍA, PASO A PASO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
            )
        }

        items(
            items = timelineEvents,
            key = { it.id },
        ) { event ->
            TimelineEventCard(event = event)
        }

        item {
            HomeNoticeCard(
                childFirstName = summary.childFirstName,
                onSendNote = onSendNote,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun DateBannerCard(
    dateText: String,
    onNavigateToCalendar: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Teal, CircleShape)
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            OutlinedButton(
                onClick = onNavigateToCalendar,
                shape = RoundedCornerShape(14.dp),
                border = null,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Ver el mes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun ChildSummaryCard(summary: DaySummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CoralWash,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Coral, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = summary.childInitials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = summary.childFullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = summary.childGroup,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CoralDeep.copy(alpha = 0.8f),
                    )
                }
            }

            Text(
                text = summary.generalNote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 21.sp,
            )
        }
    }
}

@Composable
private fun MetricsGrid(summary: DaySummary) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                label = "ÁNIMO",
                value = summary.mood,
                containerColor = Lavender,
                valueColor = VioletDeep,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "COMIDAS",
                value = summary.meals,
                containerColor = TealWash,
                valueColor = TealDeep,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                label = "PAÑAL / BAÑO",
                value = summary.diaperOrBathroom,
                containerColor = Color(0xFFFFF8E7),
                valueColor = Color(0xFF8A6400),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "SIESTA",
                value = summary.napDuration,
                containerColor = CoralWash,
                valueColor = CoralDeep,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = valueColor.copy(alpha = 0.7f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun TimelineEventCard(event: TimelineEvent) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    val dotColor = when (event.type) {
                        TimelineEventType.CHECK_IN, TimelineEventType.MEAL -> Teal
                        TimelineEventType.ACTIVITY, TimelineEventType.NAP -> MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(dotColor, RoundedCornerShape(4.dp))
                    )

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = event.time,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 22.dp),
            )
        }
    }
}

@Composable
private fun HomeNoticeCard(
    childFirstName: String,
    onSendNote: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var noteText by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(value = false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = TealWash,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Aviso desde casa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TealDeep,
            )

            Text(
                text = "Cuéntale a la asistente cómo amaneció $childFirstName.",
                style = MaterialTheme.typography.bodyMedium,
                color = TealDeep.copy(alpha = 0.85f),
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    if (isSubmitted) isSubmitted = false
                },
                placeholder = {
                    Text(
                        text = "Escribe una nota o aviso para hoy...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = Teal.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
            )

            Button(
                onClick = {
                    if (noteText.isNotBlank()) {
                        onSendNote(noteText)
                        noteText = ""
                        isSubmitted = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = noteText.isNotBlank(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor = Color.White,
                    disabledContainerColor = Teal.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f),
                ),
                contentPadding = PaddingValues(14.dp),
            ) {
                Text(
                    text = if (isSubmitted) "¡Nota enviada para hoy!" else "Enviar nota de hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            if (isSubmitted) {
                Text(
                    text = "✓ La nota fue enviada a la asistente.",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealDeep,
                )
            }
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
