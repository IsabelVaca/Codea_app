package mx.tec.codea.ui.screens.parent.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.domain.ChildHealth
import mx.tec.codea.domain.ChildHealthRules
import mx.tec.codea.domain.Contact
import mx.tec.codea.domain.MyChild
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.components.DatoTarjeta
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// la pestaña "Mi hijo": sus datos y lo que la familia cuenta de su salud (y puede editar).
// con el estilo del detalle del admin: cada dato en su tarjeta, etiqueta en gris y valor en negritas.
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(title = child?.fullName ?: stringResource(R.string.nav_parent_child))
        }

        if (child == null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(color = CodeaColores.Morado)
                    Text(stringResource(R.string.mychild_loading), fontFamily = Poppins, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris)
                }
            }
        } else {
            // los datos del niño tal como los tiene la guardería; no se editan aquí.
            item(key = "nacimiento") { DatoCard(stringResource(R.string.mychild_birth_date), child.birthDateText) }
            item(key = "edad") { DatoCard(stringResource(R.string.mychild_age), child.ageText) }
            item(key = "sala") { DatoCard(stringResource(R.string.mychild_room), child.classroomName) }
            if (child.guardians.isNotEmpty()) {
                item(key = "tutores") {
                    DatoCard(
                        etiqueta = stringResource(R.string.mychild_guardians),
                        valor = child.guardians.joinToString("\n") { g -> g.relationship?.let { "${g.name} ($it)" } ?: g.name },
                    )
                }
            }
            item(key = "aviso") {
                Text(
                    stringResource(R.string.mychild_data_hint),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris,
                )
            }
            item(key = "salud") {
                HealthCard(saved = child.health, saving = saving, onSave = onSaveHealth, modifier = Modifier.padding(top = 6.dp, bottom = 16.dp))
            }
        }
    }
}

@Composable
private fun DatoCard(etiqueta: String, valor: String?) {
    TarjetaBlanca(padding = 14.dp) {
        DatoTarjeta(etiqueta, valor?.ifBlank { null } ?: stringResource(R.string.mychild_unknown))
    }
}

// lo que la familia cuenta de la salud de su hijo. cada campo es texto libre; "Guardar" se enciende solo si algo cambió.
// los campos se reinician cuando cambia lo guardado, así que después de guardar quedan igual que en la API.
@Composable
private fun HealthCard(saved: ChildHealth, saving: Boolean, onSave: (ChildHealth) -> Unit, modifier: Modifier = Modifier) {
    var allergies by rememberSaveable(saved) { mutableStateOf(saved.allergies) }
    var conditions by rememberSaveable(saved) { mutableStateOf(saved.medicalConditions) }
    var intolerances by rememberSaveable(saved) { mutableStateOf(saved.intolerances) }
    var medications by rememberSaveable(saved) { mutableStateOf(saved.medications) }
    var notes by rememberSaveable(saved) { mutableStateOf(saved.otherNotes) }
    val edited = ChildHealth(allergies, conditions, intolerances, medications, notes)

    TarjetaBlanca(modifier = modifier, espacio = 12.dp) {
        Text(stringResource(R.string.mychild_health_title), fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink)
        Text(stringResource(R.string.mychild_health_hint), fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium, color = CodeaColores.GrisTexto)
        CampoSalud(allergies, { allergies = it }, R.string.mychild_allergies, R.string.mychild_allergies_example)
        CampoSalud(conditions, { conditions = it }, R.string.mychild_conditions, R.string.mychild_conditions_example)
        CampoSalud(intolerances, { intolerances = it }, R.string.mychild_intolerances, R.string.mychild_intolerances_example)
        CampoSalud(medications, { medications = it }, R.string.mychild_medications, R.string.mychild_medications_example)
        CampoSalud(notes, { notes = it }, R.string.mychild_notes, R.string.mychild_notes_example)

        Button(
            onClick = { onSave(ChildHealthRules.clean(edited)) },
            enabled = !saving && ChildHealthRules.canSave(saved, edited),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Violeta, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp),
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp).padding(end = 10.dp),
                    color = CodeaColores.Gris,
                    strokeWidth = 2.dp,
                )
            }
            Text(stringResource(if (saving) R.string.mychild_saving else R.string.mychild_save), fontFamily = Poppins, fontWeight = FontWeight.Bold)
        }
    }
}

// un campo de salud: la etiqueta chica en gris y el texto en un campo lila, como el formulario de Avisos.
@Composable
private fun CampoSalud(value: String, onChange: (String) -> Unit, label: Int, example: Int) {
    val tooLong = ChildHealthRules.isTooLong(value)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(label).uppercase(), fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.1.sp, color = if (tooLong) CodeaColores.Rojo else CodeaColores.Gris,
        )
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CodeaColores.Ink),
            cursorBrush = SolidColor(CodeaColores.Violeta),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { interno ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CodeaColores.Campo)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(stringResource(example), fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CodeaColores.Gris)
                    }
                    interno()
                }
            },
        )
        if (tooLong) {
            Text(
                stringResource(R.string.mychild_too_long, ChildHealthRules.MAX_LENGTH),
                fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Rojo,
            )
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
