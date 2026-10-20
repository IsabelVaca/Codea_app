package mx.tec.codea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.domain.DepartureEntry
import mx.tec.codea.domain.PickupChoice
import mx.tec.codea.domain.PickupKind
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.avatarColorAt
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// el pase de salida: lo último del día. la docente toca a cada niño que se va, elige quién lo recoge (de las personas que
// registró el admin y sus tutores) y se guarda la hora. con todos fuera aparece "Finalizar día", que termina su turno.
// es una pantalla "tonta": recibe datos y avisa de los toques.
@Composable
fun DeparturePassScreen(
    entries: List<DepartureEntry>,
    loading: Boolean,
    failed: Boolean,
    busyChildId: String?,
    finishing: Boolean,
    canFinishDay: Boolean,
    onRegister: (DepartureEntry, PickupChoice) -> Unit,
    onUndo: (DepartureEntry) -> Unit,
    onFinishDay: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var choosing by remember { mutableStateOf<DepartureEntry?>(null) }
    var confirmingFinish by remember { mutableStateOf(false) }
    val departed = entries.count { it.departed }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                BackHeader(
                    title = stringResource(R.string.departure_title),
                    onBackClick = onBackClick,
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.departure_hint),
                        fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
                    )
                    if (!loading && !failed && entries.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.departure_progress, departed, entries.size),
                            fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink,
                        )
                    }
                }
            }
            when {
                loading -> item {
                    Row(
                        modifier = Modifier.padding(vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(color = CodeaColores.Morado)
                        Text(stringResource(R.string.departure_loading), fontFamily = Poppins, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris)
                    }
                }
                failed -> item {
                    Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.departure_failed), fontFamily = Poppins, fontWeight = FontWeight.SemiBold, color = CodeaColores.Rojo)
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Lila, contentColor = CodeaColores.Morado),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                        ) { Text(stringResource(R.string.departure_retry), fontFamily = Poppins, fontWeight = FontWeight.Bold) }
                    }
                }
                entries.isEmpty() -> item {
                    Text(
                        text = stringResource(R.string.departure_empty),
                        fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
                else -> items(entries, key = { it.childId }) { entry ->
                    DepartureRow(
                        entry = entry,
                        avatarColor = avatarColorAt(entries.indexOfFirst { it.childId == entry.childId }.coerceAtLeast(0)),
                        busy = busyChildId == entry.childId,
                        enabled = busyChildId == null,
                        onRegisterClick = { choosing = entry },
                        onUndoClick = { onUndo(entry) },
                    )
                }
            }
        }

        // "Finalizar día" solo aparece cuando ya salieron todos; antes se dice cuántos faltan.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (canFinishDay) {
                Button(
                    onClick = { confirmingFinish = true },
                    enabled = !finishing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Violeta, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    Text(
                        text = stringResource(if (finishing) R.string.departure_finishing else R.string.departure_finish),
                        fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    )
                }
            } else if (!loading && !failed) {
                Text(
                    text = pluralStringResource(
                        R.plurals.departure_pending,
                        entries.count { !it.departed },
                        entries.count { !it.departed },
                    ),
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    choosing?.let { entry ->
        PickupChooserDialog(
            entry = entry,
            onConfirm = { choice ->
                onRegister(entry, choice)
                choosing = null
            },
            onDismiss = { choosing = null },
        )
    }
    if (confirmingFinish) {
        AlertDialog(
            onDismissRequest = { confirmingFinish = false },
            title = { Text(stringResource(R.string.departure_finish_title)) },
            text = { Text(stringResource(R.string.departure_finish_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmingFinish = false
                    onFinishDay()
                }) { Text(stringResource(R.string.departure_finish_confirm), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingFinish = false }) { Text(stringResource(R.string.departure_cancel)) }
            },
        )
    }
}

@Composable
private fun DepartureRow(
    entry: DepartureEntry,
    avatarColor: Color,
    busy: Boolean,
    enabled: Boolean,
    onRegisterClick: () -> Unit,
    onUndoClick: () -> Unit,
) {
    // tarjeta blanca como las del admin; el que ya salió queda en lila.
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (entry.departed) CodeaColores.Lila else Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140)),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 76.dp)
                .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InitialsAvatar(
                initials = initialsOf(entry.name),
                color = avatarColor,
                size = 48.dp,
                background = if (entry.departed) Color.White else CodeaColores.Lila,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.name, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(
                    text = if (entry.departed) {
                        stringResource(R.string.departure_row_done, entry.time.orEmpty(), entry.pickedUpBy.orEmpty())
                    } else {
                        stringResource(R.string.departure_row_pending)
                    },
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = if (entry.departed) CodeaColores.Morado else CodeaColores.GrisTexto,
                )
            }
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp), color = CodeaColores.Morado)
            } else if (entry.departed) {
                TextButton(
                    onClick = onUndoClick,
                    enabled = enabled,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = CodeaColores.Violeta),
                ) {
                    Text(stringResource(R.string.departure_undo), fontFamily = Poppins, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onRegisterClick,
                    enabled = enabled,
                    modifier = Modifier.heightIn(min = 44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Violeta, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    Text(stringResource(R.string.departure_register), fontFamily = Poppins, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ¿quién recoge a este niño? una persona de la lista y listo: la hora la pone el servidor al guardar.
@Composable
private fun PickupChooserDialog(
    entry: DepartureEntry,
    onConfirm: (PickupChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf<PickupChoice?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.departure_choose_title, entry.name)) },
        text = {
            if (entry.choices.isEmpty()) {
                Text(stringResource(R.string.departure_choose_empty))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    entry.choices.forEach { choice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .selectable(
                                    selected = selected == choice,
                                    role = Role.RadioButton,
                                    onClick = { selected = choice },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(selected = selected == choice, onClick = null)
                            Column {
                                Text(choice.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                val detail = listOfNotNull(
                                    choice.relationship,
                                    stringResource(R.string.departure_guardian_tag).takeIf { choice.kind == PickupKind.GUARDIAN },
                                ).joinToString(", ")
                                if (detail.isNotBlank()) {
                                    Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { selected?.let(onConfirm) }, enabled = selected != null) {
                Text(stringResource(R.string.departure_choose_confirm), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.departure_cancel)) } },
    )
}

// "Ana López" -> "AL".
private fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DeparturePassScreenPreview() {
    val choices = listOf(
        PickupChoice(PickupKind.AUTHORIZED, "1", "Andrea Torres", "Tía"),
        PickupChoice(PickupKind.GUARDIAN, "g1", "Laura Iglesias", "Mamá"),
    )
    CodeaTheme {
        DeparturePassScreen(
            entries = listOf(
                DepartureEntry("1", "Mateo Iglesias", choices, time = "15:42", pickedUpBy = "Laura Iglesias"),
                DepartureEntry("2", "Sofía Márquez", choices),
            ),
            loading = false,
            failed = false,
            busyChildId = null,
            finishing = false,
            canFinishDay = false,
            onRegister = { _, _ -> },
            onUndo = {},
            onFinishDay = {},
            onRetry = {},
            onBackClick = {},
        )
    }
}
