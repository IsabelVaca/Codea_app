package mx.tec.codea.ui.screens.parent.documents

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// pide el nombre de la persona que podrá recoger al niño.
@Composable
fun AddAuthorizedDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar autorizado") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                singleLine = true,
                placeholder = { Text("Nombre completo") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nombre.trim()) },
                enabled = nombre.isNotBlank(),
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
