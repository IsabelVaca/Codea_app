package mx.tec.codea.ui.screens.parent.child

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.ChildHealth
import mx.tec.codea.domain.ChildHealthRules
import mx.tec.codea.domain.Contact
import mx.tec.codea.domain.MyChild
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme

// la pestaña "Mi hijo": sus datos y lo que la familia cuenta de su salud (y puede editar).
// es una pantalla "tonta": recibe datos y avisa de los toques, el ViewModel decide qué hacer.
@Composable
fun MyChildScreen(
    child: MyChild?,
    saving: Boolean,
    onSaveHealth: (ChildHealth) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                overline = stringResource(R.string.mychild_overline),
                title = child?.fullName ?: stringResource(R.string.nav_parent_child),
            )
        }

        if (child == null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.mychild_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            item(key = "datos") { DataCard(child) }
            item(key = "salud") { HealthCard(saved = child.health, saving = saving, onSave = onSaveHealth) }
        }
    }
}

// los datos del niño tal como los tiene la guardería. no se editan aquí: si algo está mal, se corrige con la dirección.
@Composable
private fun DataCard(child: MyChild) {
    Card {
        Text(
            text = stringResource(R.string.mychild_data_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        InfoRow(stringResource(R.string.mychild_full_name), child.fullName)
        InfoRow(stringResource(R.string.mychild_birth_date), child.birthDateText)
        InfoRow(stringResource(R.string.mychild_age), child.ageText)
        InfoRow(stringResource(R.string.mychild_room), child.classroomName)
        if (child.guardians.isNotEmpty()) {
            InfoRow(
                label = stringResource(R.string.mychild_guardians),
                value = child.guardians.joinToString("\n") { guardian ->
                    guardian.relationship?.let { "${guardian.name} ($it)" } ?: guardian.name
                },
            )
        }
        Text(
            text = stringResource(R.string.mychild_data_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value?.ifBlank { null } ?: stringResource(R.string.mychild_unknown),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// lo que la familia cuenta de la salud de su hijo. cada campo es texto libre; "Guardar" se enciende solo si algo cambió.
// los campos se reinician cuando cambia lo guardado, así que después de guardar quedan igual que en la API.
@Composable
private fun HealthCard(saved: ChildHealth, saving: Boolean, onSave: (ChildHealth) -> Unit) {
    var allergies by rememberSaveable(saved) { mutableStateOf(saved.allergies) }
    var conditions by rememberSaveable(saved) { mutableStateOf(saved.medicalConditions) }
    var intolerances by rememberSaveable(saved) { mutableStateOf(saved.intolerances) }
    var medications by rememberSaveable(saved) { mutableStateOf(saved.medications) }
    var notes by rememberSaveable(saved) { mutableStateOf(saved.otherNotes) }
    val edited = ChildHealth(allergies, conditions, intolerances, medications, notes)

    Card {
        Text(
            text = stringResource(R.string.mychild_health_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = stringResource(R.string.mychild_health_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HealthField(allergies, { allergies = it }, R.string.mychild_allergies, R.string.mychild_allergies_example)
        HealthField(conditions, { conditions = it }, R.string.mychild_conditions, R.string.mychild_conditions_example)
        HealthField(intolerances, { intolerances = it }, R.string.mychild_intolerances, R.string.mychild_intolerances_example)
        HealthField(medications, { medications = it }, R.string.mychild_medications, R.string.mychild_medications_example)
        HealthField(notes, { notes = it }, R.string.mychild_notes, R.string.mychild_notes_example)

        Button(
            onClick = { onSave(ChildHealthRules.clean(edited)) },
            enabled = !saving && ChildHealthRules.canSave(saved, edited),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(if (saving) R.string.mychild_saving else R.string.mychild_save),
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun HealthField(value: String, onChange: (String) -> Unit, label: Int, example: Int) {
    val tooLong = ChildHealthRules.isTooLong(value)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 6,
        isError = tooLong,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        label = { Text(stringResource(label)) },
        placeholder = { Text(stringResource(example)) },
        supportingText = if (tooLong) {
            { Text(stringResource(R.string.mychild_too_long, ChildHealthRules.MAX_LENGTH)) }
        } else {
            null
        },
    )
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun MyChildScreenPreview() {
    CodeaTheme {
        MyChildScreen(
            child = MyChild(
                fullName = "Mateo Iglesias",
                birthDateText = "20 de enero de 2022",
                ageText = "4 años 9 meses",
                classroomName = "Preescolar 2",
                guardians = listOf(Contact("Laura Iglesias", "Mamá", "81 1234 5678")),
                health = ChildHealth(allergies = "Nueces", intolerances = "Lactosa"),
            ),
            saving = false,
            onSaveHealth = {},
        )
    }
}
