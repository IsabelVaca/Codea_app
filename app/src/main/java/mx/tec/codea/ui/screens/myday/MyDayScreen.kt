package mx.tec.codea.ui.screens.myday

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.components.ScreenSwitchButton
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Sun

// the "mi día" screen: the routine of today, with the subprocess that is running now.
// it copies screen 2 of the "reporte con foto" flow in the html prototype.
// the buttons only report the tap for now. the parent will connect them later.
@Composable
fun MyDayScreen(
    modifier: Modifier = Modifier,
    routine: Routine = MyDaySampleData.routine,
    onMakeReportClick: (Subprocess) -> Unit = {},
    onConfirmSubprocessClick: (Subprocess) -> Unit = {},
    // opens the "day not started" version of this tab.
    onSwitchClick: () -> Unit = {},
) {
    // a box lets us put the switch button in the corner, on top of the list.
    Box(modifier = modifier.fillMaxSize()) {
        RoutineList(
            routine = routine,
            onMakeReportClick = onMakeReportClick,
            onConfirmSubprocessClick = onConfirmSubprocessClick,
        )
        ScreenSwitchButton(
            onClick = onSwitchClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        )
    }
}

// the header, the progress card and every subprocess of the routine, in a list that scrolls.
@Composable
private fun RoutineList(
    routine: Routine,
    onMakeReportClick: (Subprocess) -> Unit,
    onConfirmSubprocessClick: (Subprocess) -> Unit,
) {
    // we count the done steps plus the active one, like the "4 de 9" of the prototype.
    val currentStep = routine.subprocesses.count { it.status != SubprocessStatus.PENDING }

    // a lazy column only draws the rows that fit on the screen, and it can scroll.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
            RoutineProgressCard(current = currentStep, total = routine.subprocesses.size)
        }
        // each subprocess picks the row design that matches its status.
        items(routine.subprocesses, key = { it.number }) { subprocess ->
            when (subprocess.status) {
                SubprocessStatus.DONE -> DoneSubprocessRow(subprocess)
                SubprocessStatus.ACTIVE -> ActiveSubprocessCard(
                    subprocess = subprocess,
                    onMakeReportClick = { onMakeReportClick(subprocess) },
                    onConfirmClick = { onConfirmSubprocessClick(subprocess) },
                    // the prototype leaves more space above the active card, so it stands out.
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SubprocessStatus.PENDING -> PendingSubprocessRow(subprocess)
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

// a finished subprocess: green, with a check and the time it was confirmed.
@Composable
private fun DoneSubprocessRow(subprocess: Subprocess) {
    SubprocessRow(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        badge = {
            StepBadge(containerColor = MaterialTheme.colorScheme.tertiary) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.my_day_done_description),
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(14.dp),
                )
            }
        },
        name = subprocess.name,
        nameColor = MaterialTheme.colorScheme.onTertiaryContainer,
        trailing = subprocess.time,
    )
}

// a subprocess that has not started: grey, with its number.
@Composable
private fun PendingSubprocessRow(subprocess: Subprocess) {
    SubprocessRow(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        badge = {
            StepBadge(containerColor = MaterialTheme.colorScheme.outlineVariant) {
                Text(
                    text = subprocess.number.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        name = subprocess.name,
        nameColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// done and pending rows have the same shape, only the colors and the badge change.
// so we write the shape once here and the two rows above only pass what is different.
@Composable
private fun SubprocessRow(
    containerColor: Color,
    badge: @Composable () -> Unit,
    name: String,
    nameColor: Color,
    trailing: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        badge()
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = nameColor,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = nameColor.copy(alpha = 0.75f),
            )
        }
    }
}

// the small rounded square at the start of each row.
@Composable
private fun StepBadge(containerColor: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(containerColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// the big dark card of the subprocess that is running now.
// it has the two actions the teacher uses the most during the day.
@Composable
private fun ActiveSubprocessCard(
    subprocess: Subprocess,
    onMakeReportClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // on a dark card we use "inverse" colors, so the text is light.
    val contentColor = MaterialTheme.colorScheme.inverseOnSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = contentColor,
        shadowElevation = 8.dp,
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
                    text = stringResource(R.string.my_day_active_step, subprocess.number).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = contentColor.copy(alpha = 0.7f),
                )
                if (subprocess.time != null) {
                    // yellow means "time" in the prototype. it stays the same in dark mode.
                    Pill(
                        text = stringResource(R.string.my_day_active_since, subprocess.time),
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

            // the main action is amber and big, the second one is almost transparent.
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
        MyDayScreen()
    }
}
