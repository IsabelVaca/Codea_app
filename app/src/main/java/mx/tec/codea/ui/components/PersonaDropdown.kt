package mx.tec.codea.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.Poppins


private val dropdownColors
    @Composable get() = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF4F1FB),
        unfocusedContainerColor = Color(0xFFF4F1FB),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = Color(0xFF2A2140),
        unfocusedTextColor = Color(0xFF2A2140)
    )

private val dropdownTextStyle
    @Composable get() = LocalTextStyle.current.copy(fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaDropdown(
    opciones: List<String>,
    seleccionado: String?,
    onSeleccionar: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = seleccionado ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, fontFamily = Poppins) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = dropdownColors,
            textStyle = dropdownTextStyle,
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, fontFamily = Poppins) },
                    onClick = {
                        onSeleccionar(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

// el menú no se cierra al marcar una opción, solo al tocar fuera.
// opciones es (id, nombre a mostrar); seleccionados y onToggle trabajan con ids.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasMultiDropdown(
    opciones: List<Pair<String, String>>,
    seleccionados: List<String>,
    onToggle: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val textoMostrado = opciones
        .filter { (id, _) -> id in seleccionados }
        .joinToString(", ") { (_, nombre) -> nombre }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = textoMostrado,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, fontFamily = Poppins) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = dropdownColors,
            textStyle = dropdownTextStyle,
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { (id, nombre) ->
                DropdownMenuItem(
                    text = { Text(nombre, fontFamily = Poppins) },
                    leadingIcon = {
                        Checkbox(checked = id in seleccionados, onCheckedChange = { onToggle(id) })
                    },
                    onClick = { onToggle(id) }
                )
            }
        }
    }
}
