package mx.tec.codea.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mx.tec.codea.R
import mx.tec.codea.data.TeacherRepository
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.Teacher
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme

// the other options of the menu. they do not have a screen yet,
// so the nav host opens "muy pronto" with their title.
enum class MenuOption(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int) {
    DOCUMENTS(R.string.menu_documents_title, R.string.menu_documents_subtitle),
    PROFILE(R.string.menu_profile_title, R.string.menu_profile_subtitle),
    NOTICES(R.string.menu_notices_title, R.string.menu_notices_subtitle),
}

// the "menú" screen, copied from screen 1 of "registrar entrada" in the prototype:
// the teacher, the check-in card and the other options.
// when openChecker is true, the check-in card taps itself, so the teacher sees
// where the check-in lives before the check-in screen opens.
@Composable
fun MenuScreen(
    teacher: Teacher,
    checkIn: CheckInRecord?,
    openChecker: Boolean,
    onCheckerClick: () -> Unit,
    onOptionClick: (MenuOption) -> Unit,
    onLogoutConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // the interaction source is where the card hears about touches.
    // we keep it here, so we can also send a "fake" touch to it.
    val checkerInteraction = remember { MutableInteractionSource() }
    // we need the card size to start the ripple from its center.
    var checkerSize by remember { mutableStateOf(IntSize.Zero) }
    // rememberSaveable keeps this value when the user comes back from the check-in,
    // so the card does not tap itself a second time.
    var autoTapDone by rememberSaveable { mutableStateOf(false) }
    // the dialog only matters to this screen, so its state lives here and not in a view model.
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    if (openChecker && !autoTapDone) {
        // a launched effect runs this code once, outside the drawing of the screen.
        LaunchedEffect(Unit) {
            // we wait until the slide between tabs is over.
            delay(450)
            val center = Offset(checkerSize.width / 2f, checkerSize.height / 2f)
            val press = PressInteraction.Press(center)
            // the same events a real finger sends: press, wait a little, release.
            checkerInteraction.emit(press)
            delay(300)
            checkerInteraction.emit(PressInteraction.Release(press))
            // a short pause, so the eye can follow the ripple before the screen changes.
            delay(150)
            autoTapDone = true
            onCheckerClick()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader(
            overline = stringResource(R.string.my_room_overline),
            title = stringResource(R.string.menu_title),
        )
        TeacherCard(teacher = teacher, checkIn = checkIn)
        CheckerCard(
            checkIn = checkIn,
            interactionSource = checkerInteraction,
            onClick = onCheckerClick,
            modifier = Modifier.onSizeChanged { checkerSize = it },
        )
        MenuOption.entries.forEach { option ->
            MenuRow(
                title = stringResource(option.titleRes),
                subtitle = stringResource(option.subtitleRes),
                // "2 nuevos" is the only subtitle that asks for attention.
                subtitleColor = if (option == MenuOption.NOTICES) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                onClick = { onOptionClick(option) },
            )
        }
        MenuRow(
            title = stringResource(R.string.menu_logout),
            subtitle = null,
            titleColor = MaterialTheme.colorScheme.error,
            onClick = { showLogoutDialog = true },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.menu_logout_dialog_title)) },
            text = { Text(stringResource(R.string.menu_logout_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirm()
                    },
                ) { Text(stringResource(R.string.menu_logout)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

// the teacher, her room and her shift, and a line that says if she already checked in.
@Composable
private fun TeacherCard(teacher: Teacher, checkIn: CheckInRecord?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(22.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        InitialsAvatar(initials = teacher.initials, color = MaterialTheme.colorScheme.primary)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.menu_teacher_name, teacher.fullName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(
                    R.string.menu_teacher_shift,
                    teacher.roomName,
                    formatTimeOfDay(teacher.shiftStartMinutes),
                    formatTimeOfDay(teacher.shiftEndMinutes),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val (text, color) = when {
                checkIn == null ->
                    stringResource(R.string.menu_no_check_in) to MaterialTheme.colorScheme.error
                checkIn.isLate ->
                    stringResource(R.string.menu_checked_in_late, formatTimeOfDay(checkIn.arrivalMinutes)) to
                        MaterialTheme.colorScheme.secondary
                else ->
                    stringResource(R.string.menu_checked_in, formatTimeOfDay(checkIn.arrivalMinutes)) to
                        MaterialTheme.colorScheme.onTertiaryContainer
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

// the check-in card. coral means "you still have to do this", green means "done".
@Composable
private fun CheckerCard(
    checkIn: CheckInRecord?,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // the card becomes a bit smaller while it is pressed, like a real button.
    // this works for a real finger and also for the automatic tap.
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "checkerScale")
    val accent = if (checkIn == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    val accentContainer = if (checkIn == null) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(2.dp, accent),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            // the icon is a ring inside a light square, like in the prototype.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(3.dp, accent, CircleShape),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.menu_checker_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = if (checkIn == null) {
                        stringResource(R.string.menu_checker_subtitle)
                    } else {
                        stringResource(R.string.menu_checker_subtitle_done, formatTimeOfDay(checkIn.arrivalMinutes))
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = accent,
            )
        }
    }
}

// a plain white row of the menu, with a title, an optional subtitle and an arrow.
@Composable
private fun MenuRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = subtitleColor,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MenuScreenPreview() {
    CodeaTheme {
        MenuScreen(
            teacher = TeacherRepository().current(),
            checkIn = null,
            openChecker = false,
            onCheckerClick = {},
            onOptionClick = {},
            onLogoutConfirm = {},
        )
    }
}
