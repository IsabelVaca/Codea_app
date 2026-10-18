package mx.tec.codea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import mx.tec.codea.R
import mx.tec.codea.domain.CheckInAttempt
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.CheckInStatus
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.state.CheckerUiState
import mx.tec.codea.ui.theme.CodeaTheme

// la hora del reloj se vuelve a leer cada tanto para que no se quede en la de cuando abrió la pantalla.
private const val CLOCK_REFRESH_MS = 20_000L

// the "checador" screen: the hour and the button to register the start of the shift.
// sin ubicación: la docente registra su entrada con un toque, y la salida igual.
// si olvida la salida, a las 9 pm el servidor la cierra solo.
// when the check-in is already saved (registeredCheckIn), it shows the exit instead.
@Composable
fun CheckerScreen(
    uiState: CheckerUiState,
    registeredCheckIn: CheckInRecord?,
    onBackClick: () -> Unit,
    onRefreshTime: () -> Unit,
    onCheckOutClick: () -> Unit,
    onCheckInClick: () -> Unit,
    onNotifyCoordinationClick: () -> Unit,
    onWriteLateReasonClick: () -> Unit,
    onLateReasonChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // sin registrar, el reloj de la pantalla se mantiene al día.
    if (registeredCheckIn == null) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(CLOCK_REFRESH_MS)
                onRefreshTime()
            }
        }
    }

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
            // con la entrada ya registrada la pantalla es la de la salida.
            title = stringResource(if (registeredCheckIn != null) R.string.checker_title_registered else R.string.checker_title),
            onBackClick = onBackClick,
        )

        if (registeredCheckIn != null) {
            AlreadyRegistered(record = registeredCheckIn)
            if (registeredCheckIn.departureMinutes != null) {
                // la salida la puso la docente o el cierre automático de las 9 pm.
                InfoBox(
                    title = stringResource(
                        if (registeredCheckIn.autoClosed) R.string.checker_exit_auto else R.string.checker_exit_done,
                        formatTimeOfDay(registeredCheckIn.departureMinutes),
                    ),
                    body = stringResource(
                        if (registeredCheckIn.autoClosed) R.string.checker_exit_auto_body else R.string.checker_exit_done_body,
                    ),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            } else {
                CheckInButton(
                    text = stringResource(R.string.checker_exit_button),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onCheckOutClick,
                )
            }
            return@Column
        }

        CurrentTime(uiState = uiState)

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
                onNotifyCoordinationClick = onNotifyCoordinationClick,
                onWriteLateReasonClick = onWriteLateReasonClick,
                onLateReasonChange = onLateReasonChange,
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

// e2: the teacher arrived after her hour. she can check in, but it shows as late.
@Composable
private fun LateActions(
    uiState: CheckerUiState,
    onCheckInClick: () -> Unit,
    onNotifyCoordinationClick: () -> Unit,
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
        OutlinedButton(
            onClick = onNotifyCoordinationClick,
            enabled = !uiState.coordinationNotified,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(
                    if (uiState.coordinationNotified) R.string.checker_coordination_notified else R.string.checker_notify_coordination,
                ),
            )
        }
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

// the preview shows the screen with the check-in not registered yet.
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun CheckerScreenLatePreview() {
    CodeaTheme {
        CheckerScreen(
            uiState = CheckerUiState(
                attempt = CheckInAttempt(
                    date = "Miércoles 14 de octubre",
                    arrivalMinutes = 8 * 60 + 19,
                    shiftStartMinutes = 8 * 60,
                    placeName = "La Oruga",
                ),
            ),
            registeredCheckIn = null,
            onBackClick = {},
            onRefreshTime = {},
            onCheckOutClick = {},
            onCheckInClick = {},
            onNotifyCoordinationClick = {},
            onWriteLateReasonClick = {},
            onLateReasonChange = {},
        )
    }
}
