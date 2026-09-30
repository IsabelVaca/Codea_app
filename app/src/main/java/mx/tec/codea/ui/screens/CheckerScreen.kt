package mx.tec.codea.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import mx.tec.codea.R
import mx.tec.codea.data.CheckInRepository
import mx.tec.codea.data.CheckInScenario
import mx.tec.codea.domain.CheckInAttempt
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.CheckInStatus
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.state.CheckerUiState
import mx.tec.codea.ui.theme.CodeaTheme

// the "checador" screen: the hour, a small map that shows where the teacher is,
// and the button to register the start of the shift.
// it follows screen 2 and the error cases e1 and e2 of "registrar entrada" in the prototype.
// when the check-in is already saved (registeredCheckIn), it only shows that.
@Composable
fun CheckerScreen(
    uiState: CheckerUiState,
    registeredCheckIn: CheckInRecord?,
    onBackClick: () -> Unit,
    onScenarioChange: (CheckInScenario) -> Unit,
    onCheckInClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNotifyCoordinationClick: () -> Unit,
    onWriteLateReasonClick: () -> Unit,
    onLateReasonChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // the content can be taller than a small phone, so the column can scroll.
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BackHeader(
            overline = stringResource(R.string.checker_overline),
            title = stringResource(R.string.checker_title),
            onBackClick = onBackClick,
        )

        if (registeredCheckIn != null) {
            AlreadyRegistered(record = registeredCheckIn)
            LocationMap(attempt = uiState.attempt, status = CheckInStatus.ON_TIME)
            return@Column
        }

        // it goes on top, so changing the case never needs a scroll.
        ScenarioPicker(selected = uiState.scenario, onScenarioChange = onScenarioChange)
        CurrentTime(uiState = uiState)
        LocationMap(attempt = uiState.attempt, status = uiState.status)

        // each status has its own message and its own buttons.
        when (uiState.status) {
            CheckInStatus.ON_TIME -> CheckInButton(
                text = stringResource(R.string.checker_check_in),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                onClick = onCheckInClick,
            )
            CheckInStatus.LATE -> LateActions(
                uiState = uiState,
                onCheckInClick = onCheckInClick,
                onWriteLateReasonClick = onWriteLateReasonClick,
                onLateReasonChange = onLateReasonChange,
            )
            CheckInStatus.OUT_OF_RANGE -> OutOfRangeActions(
                coordinationNotified = uiState.coordinationNotified,
                onRetryClick = onRetryClick,
                onNotifyCoordinationClick = onNotifyCoordinationClick,
            )
        }
    }
}

// the date, the big hour, and a short line about the hour of the shift.
@Composable
private fun CurrentTime(uiState: CheckerUiState) {
    val attempt = uiState.attempt
    val shiftStart = formatTimeOfDay(attempt.shiftStartMinutes)
    val note = when (uiState.status) {
        CheckInStatus.ON_TIME -> if (uiState.minutesEarly > 0) {
            pluralStringResource(R.plurals.checker_minutes_early, uiState.minutesEarly, uiState.minutesEarly)
        } else {
            stringResource(R.string.checker_on_time)
        }
        CheckInStatus.LATE -> stringResource(R.string.checker_late_note, shiftStart, uiState.minutesLate)
        CheckInStatus.OUT_OF_RANGE -> stringResource(R.string.checker_shift_starts, shiftStart)
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = attempt.date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatTimeOfDay(attempt.arrivalMinutes),
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold,
            // late turns the clock amber, like the prototype says.
            color = if (uiState.status == CheckInStatus.LATE) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )
        Text(
            text = note,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// a small drawing of the place, like the one in the prototype: two streets, two
// buildings, the area of the school (a circle) and a pin where the teacher is.
@Composable
private fun LocationMap(attempt: CheckInAttempt, status: CheckInStatus) {
    val isInside = status != CheckInStatus.OUT_OF_RANGE
    val areaColor = if (isInside) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val streetColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(areaColor.copy(alpha = 0.08f)),
    ) {
        // the streets are white stripes that repeat across the drawing.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripe = 12.dp.toPx()
            var x = 40.dp.toPx()
            while (x < size.width) {
                drawRect(streetColor, topLeft = Offset(x, 0f), size = Size(stripe, size.height))
                x += 140.dp.toPx()
            }
            var y = 60.dp.toPx()
            while (y < size.height) {
                drawRect(streetColor, topLeft = Offset(0f, y), size = Size(size.width, stripe))
                y += 120.dp.toPx()
            }
        }
        // two buildings, only to make it look like a map.
        Box(
            Modifier
                .padding(start = 26.dp, top = 150.dp)
                .size(96.dp, 70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(areaColor.copy(alpha = 0.14f)),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 34.dp)
                .size(78.dp, 58.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(areaColor.copy(alpha = 0.14f)),
        )

        if (isInside) {
            // the teacher is inside the area: one pin in the middle of the circle.
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(areaColor.copy(alpha = 0.16f))
                    .border(2.dp, areaColor.copy(alpha = 0.45f), CircleShape),
            )
            MapPin(
                color = MaterialTheme.colorScheme.error,
                size = 26.dp,
                modifier = Modifier.align(Alignment.Center).offset(y = (-18).dp),
            )
        } else {
            // the teacher is far: a grey pin marks the school, and hers is outside.
            Box(
                Modifier
                    .align(Alignment.Center)
                    .offset(x = (-10).dp, y = (-13).dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(areaColor.copy(alpha = 0.10f))
                    .border(2.dp, areaColor.copy(alpha = 0.5f), CircleShape),
            )
            MapPin(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                size = 22.dp,
                modifier = Modifier.align(Alignment.Center).offset(x = (-10).dp, y = (-28).dp),
            )
            MapPin(
                color = areaColor,
                size = 26.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 34.dp, bottom = 74.dp),
            )
        }

        // the two labels at the bottom: how far she is, and the result.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val distanceText = if (isInside) {
                stringResource(R.string.checker_place_distance, attempt.placeName, formatDistance(attempt.distanceMeters))
            } else {
                stringResource(R.string.checker_you_are_at, formatDistance(attempt.distanceMeters))
            }
            MapLabel(
                text = distanceText,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = if (isInside) MaterialTheme.colorScheme.onTertiaryContainer else areaColor,
            )
            when (status) {
                CheckInStatus.ON_TIME -> MapLabel(
                    text = stringResource(R.string.checker_location_verified),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                )
                CheckInStatus.LATE -> MapLabel(
                    text = stringResource(R.string.checker_out_of_schedule),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                )
                CheckInStatus.OUT_OF_RANGE -> MapLabel(
                    text = stringResource(R.string.checker_out_of_range),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}

// a map pin: a square with three round corners, turned so the sharp corner points down.
@Composable
private fun MapPin(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .rotate(-45f)
            .clip(RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomEndPercent = 50, bottomStartPercent = 0))
            .background(color),
    )
}

// a small pill with text, used for the labels on the map.
@Composable
private fun MapLabel(text: String, containerColor: Color, contentColor: Color) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = contentColor,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    )
}

// e2: the teacher arrived after her hour. she can check in, but it shows as late.
@Composable
private fun LateActions(
    uiState: CheckerUiState,
    onCheckInClick: () -> Unit,
    onWriteLateReasonClick: () -> Unit,
    onLateReasonChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox(
            title = stringResource(R.string.checker_late_title),
            body = stringResource(R.string.checker_late_body),
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            titleColor = MaterialTheme.colorScheme.onSurface,
        )
        CheckInButton(
            text = stringResource(R.string.checker_check_in_late),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            onClick = onCheckInClick,
        )
        // the reason is optional, so the field only opens when the teacher asks for it.
        if (uiState.isWritingLateReason) {
            OutlinedTextField(
                value = uiState.lateReason,
                onValueChange = onLateReasonChange,
                label = { Text(stringResource(R.string.checker_late_reason_label)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            TextButton(onClick = onWriteLateReasonClick) {
                Text(stringResource(R.string.checker_write_late_reason))
            }
        }
    }
}

// e1: the teacher is not at the school. the button does nothing: she can only
// try again or tell coordination.
@Composable
private fun OutOfRangeActions(
    coordinationNotified: Boolean,
    onRetryClick: () -> Unit,
    onNotifyCoordinationClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox(
            title = stringResource(R.string.checker_out_title),
            body = stringResource(R.string.checker_out_body),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            titleColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Text(
                text = stringResource(R.string.checker_check_in_blocked),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRetryClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.checker_retry))
            }
            OutlinedButton(
                onClick = onNotifyCoordinationClick,
                enabled = !coordinationNotified,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (coordinationNotified) R.string.checker_coordination_notified else R.string.checker_notify_coordination,
                    ),
                )
            }
        }
    }
}

// shown when the teacher comes back to the check-in after she registered.
@Composable
private fun AlreadyRegistered(record: CheckInRecord) {
    InfoBox(
        title = stringResource(
            if (record.isLate) R.string.checker_already_registered_late else R.string.checker_already_registered,
            formatTimeOfDay(record.arrivalMinutes),
        ),
        body = stringResource(R.string.checker_exit_is_automatic),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        titleColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}

// the main button of the screen. green when all is good, amber when it is late.
@Composable
private fun CheckInButton(text: String, containerColor: Color, contentColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

// there is no real gps in the prototype, so these chips let us show each case.
@Composable
private fun ScenarioPicker(selected: CheckInScenario, onScenarioChange: (CheckInScenario) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.checker_simulate),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CheckInScenario.entries.forEach { scenario ->
                FilterChip(
                    selected = scenario == selected,
                    onClick = { onScenarioChange(scenario) },
                    label = { Text(stringResource(scenario.labelRes())) },
                )
            }
        }
    }
}

private fun CheckInScenario.labelRes(): Int = when (this) {
    CheckInScenario.ON_TIME -> R.string.checker_scenario_on_time
    CheckInScenario.LATE -> R.string.checker_scenario_late
    CheckInScenario.OUT_OF_RANGE -> R.string.checker_scenario_out
}

// 12 -> "12 m", 1400 -> "1.4 km".
private fun formatDistance(meters: Int): String =
    if (meters < 1000) "$meters m" else String.format(Locale.getDefault(), "%.1f km", meters / 1000.0)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CheckerScreenLatePreview() {
    CodeaTheme {
        CheckerScreen(
            uiState = CheckerUiState(
                scenario = CheckInScenario.LATE,
                attempt = CheckInRepository().attemptFor(CheckInScenario.LATE),
            ),
            registeredCheckIn = null,
            onBackClick = {},
            onScenarioChange = {},
            onCheckInClick = {},
            onRetryClick = {},
            onNotifyCoordinationClick = {},
            onWriteLateReasonClick = {},
            onLateReasonChange = {},
        )
    }
}
