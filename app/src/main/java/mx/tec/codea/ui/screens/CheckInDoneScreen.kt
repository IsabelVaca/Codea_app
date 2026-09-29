package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mx.tec.codea.R
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme

// how long the screen waits before it opens "mi día" by itself.
private const val AUTO_CONTINUE_MILLIS = 2000L

// the feedback after the check-in (screen 3 and e3 of the prototype).
// it tells the teacher it worked, and after 2 seconds it takes her to "mi día".
@Composable
fun CheckInDoneScreen(
    record: CheckInRecord,
    teacherName: String,
    onGoToMyDayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberUpdatedState keeps the newest lambda, but the timer does not start again.
    val currentOnGoToMyDay by rememberUpdatedState(onGoToMyDayClick)
    LaunchedEffect(Unit) {
        delay(AUTO_CONTINUE_MILLIS)
        currentOnGoToMyDay()
    }

    // late check-ins are amber, the normal ones are green.
    val accent = if (record.isLate) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val time = formatTimeOfDay(record.arrivalMinutes)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(
            overline = stringResource(R.string.checker_done_overline),
            title = stringResource(
                if (record.isLate) R.string.checker_done_title_late else R.string.checker_done_title,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (record.isLate) Icons.Filled.Info else Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (record.isLate) R.string.checker_done_headline_late else R.string.checker_done_headline,
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.checker_done_time_place, time, record.placeName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (record.isLate) {
                    pluralStringResource(R.plurals.checker_done_minutes_late, record.minutesLate, record.minutesLate)
                } else {
                    stringResource(R.string.checker_done_greeting, teacherName)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DoneItem(
                icon = Icons.Filled.Check,
                text = stringResource(R.string.checker_done_location),
                color = MaterialTheme.colorScheme.tertiary,
            )
            if (record.isLate) {
                DoneItem(
                    icon = Icons.Filled.Info,
                    text = stringResource(R.string.checker_done_coordination),
                    color = MaterialTheme.colorScheme.secondary,
                )
            } else {
                DoneItem(
                    icon = Icons.Filled.Check,
                    text = stringResource(R.string.checker_done_no_paper),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            DoneItem(
                icon = Icons.Filled.Refresh,
                text = stringResource(
                    if (record.isLate) R.string.checker_done_day_starts else R.string.checker_done_exit,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGoToMyDayClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Text(
                text = stringResource(R.string.checker_done_go_to_my_day),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = stringResource(R.string.checker_done_auto),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// one line of the list: a small colored icon and a text.
@Composable
private fun DoneItem(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CheckInDoneScreenPreview() {
    CodeaTheme {
        CheckInDoneScreen(
            record = CheckInRecord(arrivalMinutes = 472, placeName = "La Oruga", minutesLate = 0, lateReason = ""),
            teacherName = "miss Karla",
            onGoToMyDayClick = {},
        )
    }
}
