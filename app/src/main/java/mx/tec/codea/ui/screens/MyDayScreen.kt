package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.data.RoutineRepository
import mx.tec.codea.domain.Routine
import mx.tec.codea.domain.RoutineRules
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Sun

// the "mi día" screen: the routine of today. the teacher taps any moment to pick it,
// and the picked one opens as a big card with its actions.
// it copies screen 2 of the "reporte con foto" flow in the html prototype.
// it is a "dumb" screen: it receives data and reports taps, the parent decides what to do.
@Composable
fun MyDayScreen(
    routine: Routine,
    selectedSubprocessNumber: Int,
    reportCountOf: (Int) -> Int,
    onSubprocessClick: (Int) -> Unit,
    onMakeReportClick: (Int) -> Unit,
    onConfirmSubprocessClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // a lazy column only draws the rows that fit on the screen, and it can scroll.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item {
            ScreenHeader(
                overline = routine.name,
                title = stringResource(R.string.my_day_title),
            )
        }
        item {
            RoutineProgressCard(current = routine.currentStep, total = routine.subprocesses.size)
        }
        // each moment picks the design that matches its status, and the picked one is big.
        items(routine.subprocesses, key = { it.number }) { subprocess ->
            val reportCount = reportCountOf(subprocess.number)
            if (subprocess.number == selectedSubprocessNumber) {
                SelectedSubprocessCard(
                    subprocess = subprocess,
                    reportCount = reportCount,
                    onMakeReportClick = { onMakeReportClick(subprocess.number) },
                    onConfirmClick = { onConfirmSubprocessClick(subprocess.number) },
                    // the prototype leaves more space around the big card, so it stands out.
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .animateItem(),
                )
            } else {
                SubprocessRow(
                    subprocess = subprocess,
                    reportCount = reportCount,
                    onClick = { onSubprocessClick(subprocess.number) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

// the light purple card that says how far the teacher is in the routine.
@Composable
private fun RoutineProgressCard(current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(22.dp))
            .padding(horizontal = 17.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.my_day_routine_steps),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Pill(
            text = stringResource(R.string.my_day_progress, current, total),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

// a small row for every moment that is not picked. the colors say its status:
// green = done, amber = in progress, grey = not started.
@Composable
private fun SubprocessRow(
    subprocess: Subprocess,
    reportCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (containerColor, contentColor) = when (subprocess.status) {
        SubprocessStatus.DONE -> colors.tertiaryContainer to colors.onTertiaryContainer
        SubprocessStatus.ACTIVE -> colors.secondary.copy(alpha = 0.16f) to colors.onSurface
        SubprocessStatus.PENDING -> colors.surfaceVariant to colors.onSurfaceVariant
    }

    // a surface with onClick is a card that can be tapped, with the ripple included.
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            StepBadge(subprocess)
            Text(
                text = subprocess.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (reportCount > 0) {
                Text(
                    text = pluralStringResource(R.plurals.my_day_report_count, reportCount, reportCount),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                )
            }
            if (subprocess.time != null) {
                Text(
                    text = subprocess.time,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.75f),
                )
            }
        }
    }
}

// the small rounded square at the start of each row: a check when it is done,
// and the number of the moment when it is not.
@Composable
private fun StepBadge(subprocess: Subprocess) {
    val colors = MaterialTheme.colorScheme
    val badgeColor = when (subprocess.status) {
        SubprocessStatus.DONE -> colors.tertiary
        SubprocessStatus.ACTIVE -> colors.secondary
        SubprocessStatus.PENDING -> colors.outlineVariant
    }
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(badgeColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (subprocess.status == SubprocessStatus.DONE) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.my_day_done_description),
                tint = colors.onTertiary,
                modifier = Modifier.size(14.dp),
            )
        } else {
            Text(
                text = subprocess.number.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (subprocess.status == SubprocessStatus.ACTIVE) colors.onSecondary else colors.onSurfaceVariant,
            )
        }
    }
}

// the big dark card of the picked moment. the buttons it shows depend on the rules:
// done and in progress moments can have reports, only the one in progress can be confirmed.
@Composable
private fun SelectedSubprocessCard(
    subprocess: Subprocess,
    reportCount: Int,
    onMakeReportClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // on a dark card we use "inverse" colors, so the text is light.
    val contentColor = MaterialTheme.colorScheme.inverseOnSurface
    val canReport = RoutineRules.canReport(subprocess)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(subprocess.status.stepLabelRes(), subprocess.number),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor.copy(alpha = 0.7f),
                )
                val timeLabel = subprocess.timeLabel()
                if (timeLabel != null) {
                    // yellow means "time" in the prototype. it stays the same in dark mode.
                    Pill(
                        text = timeLabel,
                        containerColor = Sun.copy(alpha = 0.22f),
                        contentColor = Sun,
                    )
                }
            }
            Text(
                text = subprocess.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            if (subprocess.description != null) {
                Text(
                    text = subprocess.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
            Text(
                text = when {
                    !canReport -> stringResource(R.string.my_day_not_started_yet)
                    reportCount == 0 -> stringResource(R.string.my_day_no_reports)
                    else -> pluralStringResource(R.plurals.my_day_reports_here, reportCount, reportCount)
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
            )

            if (canReport) {
                // the main action is amber and big.
                Button(
                    onClick = onMakeReportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                    contentPadding = PaddingValues(14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.my_day_make_report),
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }
            }
            if (RoutineRules.canConfirm(subprocess)) {
                // the second action is almost transparent, so it does not compete.
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = contentColor.copy(alpha = 0.14f),
                        contentColor = contentColor,
                    ),
                    contentPadding = PaddingValues(13.dp),
                ) {
                    Text(
                        text = stringResource(R.string.my_day_confirm_step),
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

// the line on top of the big card, like "subproceso 4 · en curso".
private fun SubprocessStatus.stepLabelRes(): Int = when (this) {
    SubprocessStatus.DONE -> R.string.my_day_step_done
    SubprocessStatus.ACTIVE -> R.string.my_day_step_active
    SubprocessStatus.PENDING -> R.string.my_day_step_pending
}

// the text of the yellow time pill. a moment that has not started has no time.
@Composable
private fun Subprocess.timeLabel(): String? {
    val time = time ?: return null
    return when (status) {
        SubprocessStatus.DONE -> stringResource(R.string.my_day_done_at, time)
        SubprocessStatus.ACTIVE -> stringResource(R.string.my_day_active_since, time)
        SubprocessStatus.PENDING -> null
    }
}

// a small rounded label, like "4 de 9" or "desde 11:20".
@Composable
private fun Pill(text: String, containerColor: Color, contentColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = contentColor,
        modifier = Modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MyDayScreenPreview() {
    CodeaTheme {
        MyDayScreen(
            routine = RoutineRepository().getToday(),
            selectedSubprocessNumber = 4,
            // fake data: it is only the drawing.
            reportCountOf = { number -> if (number == 3) 2 else 0 },
            onSubprocessClick = {},
            onMakeReportClick = {},
            onConfirmSubprocessClick = {},
        )
    }
}
