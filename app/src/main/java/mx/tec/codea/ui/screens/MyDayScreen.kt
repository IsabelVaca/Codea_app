package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.Routine
import mx.tec.codea.domain.RoutineRules
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.SubprocessPendingBg
import mx.tec.codea.ui.theme.SubprocessPendingFg

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
    onViewReportsClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    // true mientras se confirma el momento elegido: el botón avisa que ya reaccionó al toque.
    confirmingSubprocess: Boolean = false,
    // el pase de lista es el primer paso del día: cuántos niños están marcados presentes y cuántos hay en la sala.
    // con 0 niños en la sala no hay a quién pasar lista y la tarjeta no se muestra.
    rollCallPresent: Int = 0,
    rollCallTotal: Int = 0,
    onRollCallClick: () -> Unit = {},
    // con todos los momentos hechos aparece el pase de salida: lo último del día. se muestra con cuántos niños están hoy.
    departureReady: Boolean = false,
    departurePresent: Int = 0,
    onDepartureClick: () -> Unit = {},
) {
    // al terminar la rutina la pantalla se mueve sola hasta el pase de salida (el último paso) y lo deja a media pantalla:
    // así se nota que es lo que sigue.
    val listState = rememberLazyListState()
    LaunchedEffect(departureReady) {
        if (departureReady) {
            val last = listState.layoutInfo.totalItemsCount - 1
            if (last >= 0) listState.animateScrollToItem(last, scrollOffset = -(listState.layoutInfo.viewportSize.height / 3))
        }
    }
    // a lazy column only draws the rows that fit on the screen, and it can scroll.
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item {
            ScreenHeader(title = stringResource(R.string.my_day_title))
        }
        if (rollCallTotal > 0) {
            item(key = "roll-call") {
                RollCallCard(present = rollCallPresent, total = rollCallTotal, onClick = onRollCallClick)
            }
        }
        item {
            RoutineTitle()
        }
        // each moment picks the design that matches its status, and the picked one is big.
        items(routine.subprocesses, key = { it.number }) { subprocess ->
            val reportCount = reportCountOf(subprocess.number)
            if (subprocess.number == selectedSubprocessNumber) {
                SelectedSubprocessCard(
                    subprocess = subprocess,
                    reportCount = reportCount,
                    confirming = confirmingSubprocess,
                    onMakeReportClick = { onMakeReportClick(subprocess.number) },
                    onConfirmClick = { onConfirmSubprocessClick(subprocess.number) },
                    onViewReportsClick = { onViewReportsClick(subprocess.number) },
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
        // lo que sigue después de la rutina: el pase de salida. mientras falten momentos se ve como el paso que viene;
        // con la rutina terminada se destaca y es lo que hay que hacer.
        item(key = "departure") {
            DepartureStep(
                ready = departureReady,
                present = departurePresent,
                onClick = onDepartureClick,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

// el último paso del día. antes de terminar la rutina es una fila gris con candado ("se abre al terminar los momentos");
// con la rutina terminada se vuelve una tarjeta destacada que lleva a registrar quién recoge a cada niño y a finalizar el día.
@Composable
private fun DepartureStep(ready: Boolean, present: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    if (!ready) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.surfaceVariant,
            contentColor = colors.onSurfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(22.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.departure_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.departure_locked_hint),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        return
    }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = colors.primary,
        contentColor = colors.onPrimary,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.departure_next_step),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(0.8f),
            )
            Text(
                text = stringResource(R.string.departure_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = pluralStringResource(R.plurals.departure_card_body, present, present),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = stringResource(R.string.departure_card_action),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

// el primer paso del día: el pase de lista. mientras nadie esté marcado se ve como algo por hacer;
// ya con niños marcados queda tranquila, con el avance y una forma de revisar la lista.
@Composable
private fun RollCallCard(present: Int, total: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val pending = present == 0
    // pendiente = lavanda (hay que hacerlo); hecho = casi blanco (ya quedó tranquilo).
    val contentColor = if (pending) colors.onPrimaryContainer else colors.onSurface
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (pending) colors.primaryContainer else colors.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.roll_call_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                )
                Text(
                    text = if (pending) {
                        stringResource(R.string.roll_call_card_pending_body)
                    } else {
                        pluralStringResource(R.plurals.roll_call_card_progress, total, present, total)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                )
            }
            Text(
                text = stringResource(if (pending) R.string.roll_call_card_action else R.string.roll_call_card_review),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = contentColor,
            )
        }
    }
}

// el título de la lista de momentos: solo "Rutina", sin caja ni contador de avance.
@Composable
private fun RoutineTitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.my_day_routine_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .semantics { heading() },
    )
}

// una fila chica por cada momento que no está elegido. el tono violeta dice su estado:
// lavanda = en curso, casi blanco = hecho, gris violáceo = sin empezar.
@Composable
private fun SubprocessRow(
    subprocess: Subprocess,
    reportCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (containerColor, contentColor) = when (subprocess.status) {
        SubprocessStatus.DONE -> colors.surfaceContainer to colors.onSurface
        SubprocessStatus.ACTIVE -> colors.primaryContainer to colors.onPrimaryContainer
        SubprocessStatus.PENDING -> SubprocessPendingBg to SubprocessPendingFg
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

// el cuadrito del inicio de cada fila: una palomita suave si ya se hizo, el número en violeta pleno si está en curso
// y el número en gris si todavía no empieza.
@Composable
private fun StepBadge(subprocess: Subprocess) {
    val colors = MaterialTheme.colorScheme
    val badgeColor = when (subprocess.status) {
        SubprocessStatus.DONE -> colors.primary.copy(alpha = 0.16f)
        SubprocessStatus.ACTIVE -> colors.primary
        // un cuadrito translúcido, no colors.outlineVariant: ese tono ahora es igual al fondo de la
        // fila pendiente y el número se perdía dentro de la tarjeta.
        SubprocessStatus.PENDING -> SubprocessPendingFg.copy(alpha = 0.18f)
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
                tint = colors.primary,
                modifier = Modifier.size(14.dp),
            )
        } else {
            Text(
                text = subprocess.number.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (subprocess.status == SubprocessStatus.ACTIVE) colors.onPrimary else SubprocessPendingFg,
            )
        }
    }
}

// la tarjeta grande violeta del momento elegido. los botones que muestra dependen de las reglas:
// los momentos hechos y en curso pueden tener reportes, solo el que está en curso se puede confirmar.
@Composable
private fun SelectedSubprocessCard(
    subprocess: Subprocess,
    reportCount: Int,
    onMakeReportClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onViewReportsClick: () -> Unit,
    confirming: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // sobre el violeta pleno el texto es "onPrimary" (blanco en claro; tinta en oscuro) y los botones usan los mismos dos colores al revés.
    val colors = MaterialTheme.colorScheme
    val contentColor = colors.onPrimary
    val canReport = RoutineRules.canReport(subprocess)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = colors.primary,
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
                    text = stringResource(R.string.my_day_step_number, subprocess.number),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor.copy(alpha = 0.7f),
                )
                Pill(
                    text = subprocess.statusLabel(),
                    containerColor = contentColor.copy(alpha = 0.18f),
                    contentColor = contentColor,
                )
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when {
                        !canReport -> stringResource(R.string.my_day_not_started_yet)
                        reportCount == 0 -> stringResource(R.string.my_day_no_reports)
                        else -> pluralStringResource(R.plurals.my_day_reports_here, reportCount, reportCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f),
                )
                if (reportCount > 0) {
                    Text(
                        text = stringResource(R.string.my_day_view_reports),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor,
                        modifier = Modifier.clickable(onClick = onViewReportsClick),
                    )
                }
            }

            if (canReport) {
                // la acción principal es grande y va en el color contrario al de la tarjeta.
                Button(
                    onClick = onMakeReportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.onPrimary,
                        contentColor = colors.primary,
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
                // la segunda acción es casi transparente, para que no compita.
                Button(
                    onClick = onConfirmClick,
                    enabled = !confirming,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = contentColor.copy(alpha = 0.14f),
                        contentColor = contentColor,
                        // sin esto, al deshabilitarse mientras confirma, Material3 pone sus propios
                        // colores "disabled" (muy tenues) y el texto se pierde contra la tarjeta.
                        disabledContainerColor = contentColor.copy(alpha = 0.14f),
                        disabledContentColor = contentColor,
                    ),
                    contentPadding = PaddingValues(13.dp),
                ) {
                    Text(
                        text = if (confirming) {
                            stringResource(R.string.my_day_confirming_step)
                        } else {
                            stringResource(R.string.my_day_confirm_step)
                        },
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

// lo que dice la píldora de la tarjeta grande: el estado y, si ya hay hora, desde cuándo o cuándo terminó.
@Composable
private fun Subprocess.statusLabel(): String = when (status) {
    SubprocessStatus.DONE -> time?.let { stringResource(R.string.my_day_done_at, it) } ?: stringResource(R.string.my_day_status_done)
    SubprocessStatus.ACTIVE -> time?.let { stringResource(R.string.my_day_active_since, it) } ?: stringResource(R.string.my_day_status_active)
    SubprocessStatus.PENDING -> stringResource(R.string.my_day_status_pending)
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
