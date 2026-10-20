package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.PersonaAutorizada
import mx.tec.codea.domain.PersonaAutorizadaRules
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.avatarColorAt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import mx.tec.codea.ui.theme.CodeaTheme

// Ajustes → Personas autorizadas, paso 1: la lista de niños. al tocar uno se ven (y cambian) las personas que pueden recogerlo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasAutorizadasScreen(
    infantes: List<Infante>,
    onInfanteClick: (Infante) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    // los niños dados de baja no van: ya no se les registra salida.
    val visibles = infantes
        .filter { !it.baja && it.nombre.contains(busqueda.trim(), ignoreCase = true) }
        .sortedBy { it.nombre }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pa_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.pa_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.pa_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.pa_search)) },
                    )
                }
            }
            if (visibles.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.pa_no_children),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(visibles, key = { it.id }) { infante ->
                Surface(
                    onClick = { onInfanteClick(infante) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        InitialsAvatar(
                            initials = infante.iniciales,
                            color = avatarColorAt(infantes.indexOfFirst { it.id == infante.id }.coerceAtLeast(0)),
                            size = 48.dp,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(infante.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = infante.sala.ifBlank { stringResource(R.string.pa_no_room) },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

// paso 2: las personas de un niño, con "Agregar persona" y "Quitar".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasAutorizadasDetalleScreen(
    nombreNino: String,
    personas: List<PersonaAutorizada>,
    cargando: Boolean,
    fallo: Boolean,
    onReintentar: () -> Unit,
    onAgregar: (nombre: String, parentesco: String, telefono: String) -> Unit,
    onQuitar: (PersonaAutorizada) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarAgregar by rememberSaveable { mutableStateOf(false) }
    var porQuitar by remember { mutableStateOf<PersonaAutorizada?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(nombreNino, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.pa_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.pa_detail_hint, nombreNino),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = { mostrarAgregar = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text(stringResource(R.string.pa_add), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            when {
                cargando -> item {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
                }
                fallo -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(stringResource(R.string.pa_failed), color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = onReintentar) { Text(stringResource(R.string.pa_retry)) }
                    }
                }
                personas.isEmpty() -> item {
                    Text(
                        text = stringResource(R.string.pa_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
                else -> items(personas, key = { it.id }) { persona ->
                    PersonaCard(persona = persona, onQuitar = { porQuitar = persona })
                }
            }
        }
    }

    if (mostrarAgregar) {
        AgregarPersonaDialog(
            onConfirm = { nombre, parentesco, telefono ->
                onAgregar(nombre, parentesco, telefono)
                mostrarAgregar = false
            },
            onDismiss = { mostrarAgregar = false },
        )
    }
    porQuitar?.let { persona ->
        AlertDialog(
            onDismissRequest = { porQuitar = null },
            title = { Text(stringResource(R.string.pa_remove_title, persona.nombre)) },
            text = { Text(stringResource(R.string.pa_remove_body, nombreNino)) },
            confirmButton = {
                TextButton(onClick = {
                    onQuitar(persona)
                    porQuitar = null
                }) { Text(stringResource(R.string.pa_remove_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { porQuitar = null }) { Text(stringResource(R.string.pa_cancel)) } },
        )
    }
}

@Composable
private fun PersonaCard(persona: PersonaAutorizada, onQuitar: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(persona.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val detalle = listOf(persona.parentesco, persona.telefono).filter { it.isNotBlank() }.joinToString(" · ")
                if (detalle.isNotBlank()) {
                    Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = onQuitar, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.pa_remove), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// nombre (obligatorio), parentesco y teléfono (opcionales).
@Composable
private fun AgregarPersonaDialog(
    onConfirm: (nombre: String, parentesco: String, telefono: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var parentesco by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pa_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.pa_field_name)) },
                )
                OutlinedTextField(
                    value = parentesco,
                    onValueChange = { parentesco = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.pa_field_relationship)) },
                    placeholder = { Text(stringResource(R.string.pa_field_relationship_example)) },
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    label = { Text(stringResource(R.string.pa_field_phone)) },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nombre.trim(), parentesco.trim(), telefono.trim()) },
                enabled = PersonaAutorizadaRules.puedeAgregar(nombre),
            ) { Text(stringResource(R.string.pa_add_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.pa_cancel)) } },
    )
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PersonasAutorizadasDetallePreview() {
    CodeaTheme {
        PersonasAutorizadasDetalleScreen(
            nombreNino = "Mateo Iglesias",
            personas = listOf(
                PersonaAutorizada("1", "Andrea Torres", "Tía", "81 1234 5678"),
                PersonaAutorizada("2", "Luis Torres"),
            ),
            cargando = false,
            fallo = false,
            onReintentar = {},
            onAgregar = { _, _, _ -> },
            onQuitar = {},
            onBackClick = {},
        )
    }
}
