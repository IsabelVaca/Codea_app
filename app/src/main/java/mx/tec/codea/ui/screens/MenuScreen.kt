package mx.tec.codea.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.Pastilla
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.theme.Poppins
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mx.tec.codea.R
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.Teacher
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme

// the other options of the menu. they do not have a screen yet,
// so the nav host opens "muy pronto" with their title.
enum class MenuOption(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int) {
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
    // avisos de la fundación que todavía no ha visto (sale en coral junto a la opción).
    avisosPendientes: Int = 0,
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.menu_title),
        )
        TeacherCard(teacher = teacher, checkIn = checkIn)
        CheckerCard(
            checkIn = checkIn,
            interactionSource = checkerInteraction,
            onClick = onCheckerClick,
            modifier = Modifier.onSizeChanged { checkerSize = it },
        )
        // las opciones en una tarjeta agrupada, como Ajustes del admin.
        TarjetaBlanca(padding = 0.dp, espacio = 0.dp) {
            MenuOption.entries.forEachIndexed { i, option ->
                val esAvisos = option == MenuOption.NOTICES
                if (i > 0) HorizontalDivider(modifier = Modifier.padding(start = 70.dp), color = Color(0xFFF1EEF8))
                MenuRow(
                    icono = if (esAvisos) Icons.Outlined.Campaign else Icons.Outlined.AccountCircle,
                    title = stringResource(option.titleRes),
                    // los avisos no llevan subtítulo fijo: el número de nuevos va en el globito.
                    subtitle = if (esAvisos) null else stringResource(option.subtitleRes),
                    badge = if (esAvisos) avisosPendientes else 0,
                    onClick = { onOptionClick(option) },
                )
            }
        }
        // cerrar sesión va aparte, en rojo, como las acciones que no se deshacen en el admin.
        TarjetaBlanca(padding = 0.dp, espacio = 0.dp) {
            MenuRow(
                icono = Icons.AutoMirrored.Outlined.Logout,
                title = stringResource(R.string.menu_logout),
                subtitle = null,
                titleColor = CodeaColores.Rojo,
                onClick = { showLogoutDialog = true },
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.menu_logout_dialog_title), fontFamily = Poppins, fontWeight = FontWeight.ExtraBold) },
            text = { Text(stringResource(R.string.menu_logout_dialog_body), fontFamily = Poppins) },
            containerColor = Color.White,
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirm()
                    },
                ) { Text(stringResource(R.string.menu_logout), fontFamily = Poppins, fontWeight = FontWeight.Bold, color = CodeaColores.Rojo) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_cancel), fontFamily = Poppins, fontWeight = FontWeight.Bold, color = CodeaColores.GrisTexto)
                }
            },
        )
    }
}

// la miss, su sala y su horario, con el estado de su entrada en una pastilla (como «Checó» en Docentes del admin).
@Composable
private fun TeacherCard(teacher: Teacher, checkIn: CheckInRecord?) {
    TarjetaBlanca(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(20.dp)).background(CodeaColores.Lila),
                contentAlignment = Alignment.Center,
            ) {
                Text(teacher.initials, color = CodeaColores.Morado, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    stringResource(R.string.menu_teacher_name, teacher.fullName),
                    fontFamily = Poppins, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black,
                )
                Text(
                    stringResource(
                        R.string.menu_teacher_shift,
                        teacher.roomName,
                        formatTimeOfDay(teacher.shiftStartMinutes),
                        formatTimeOfDay(teacher.shiftEndMinutes),
                    ),
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
                )
                val texto = when {
                    checkIn == null -> stringResource(R.string.menu_no_check_in)
                    checkIn.isLate -> stringResource(R.string.menu_checked_in_late, formatTimeOfDay(checkIn.arrivalMinutes))
                    else -> stringResource(R.string.menu_checked_in, formatTimeOfDay(checkIn.arrivalMinutes))
                }
                Pastilla(texto, modifier = Modifier.padding(top = 2.dp), apagada = checkIn == null)
            }
        }
    }
}

// la tarjeta del checador. Mientras falta la entrada lleva un borde morado; ya registrada, queda como las demás.
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

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140)),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = if (checkIn == null) BorderStroke(2.dp, CodeaColores.Morado) else null,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // el ícono: un anillo dentro de un cuadro lila, como los íconos de Ajustes.
            Box(
                modifier = Modifier.size(40.dp).background(CodeaColores.Lila, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(18.dp).border(3.dp, CodeaColores.Morado, CircleShape))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(stringResource(R.string.menu_checker_title), fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CodeaColores.Ink)
                Text(
                    text = when {
                        checkIn == null -> stringResource(R.string.menu_checker_subtitle)
                        checkIn.departureMinutes == null ->
                            stringResource(R.string.menu_checker_subtitle_done, formatTimeOfDay(checkIn.arrivalMinutes))
                        else -> stringResource(
                            R.string.menu_checker_subtitle_out,
                            formatTimeOfDay(checkIn.arrivalMinutes),
                            formatTimeOfDay(checkIn.departureMinutes),
                        )
                    },
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = CodeaColores.Gris)
        }
    }
}

// una fila de las tarjetas agrupadas (como en Ajustes del admin): ícono en cuadro lila, título, una línea y la flecha.
@Composable
private fun MenuRow(
    icono: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    titleColor: Color = CodeaColores.Ink,
    // más de 0: un globito morado con el número, antes de la flecha.
    badge: Int = 0,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(if (titleColor == CodeaColores.Rojo) CodeaColores.RojoSuave else CodeaColores.Lila),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = if (titleColor == CodeaColores.Rojo) CodeaColores.Rojo else CodeaColores.Morado, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto, maxLines = 1)
            }
        }
        if (badge > 0) {
            Text(
                text = badge.toString(),
                modifier = Modifier
                    .defaultMinSize(minWidth = 24.dp)
                    .background(CodeaColores.Morado, RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = CodeaColores.Gris)
    }
}
