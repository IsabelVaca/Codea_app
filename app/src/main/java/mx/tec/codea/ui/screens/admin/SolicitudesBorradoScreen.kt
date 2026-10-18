package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import mx.tec.codea.domain.SolicitudBorrado
import mx.tec.codea.ui.components.BackHeader

// las familias que piden que se borre una foto. el admin ve la foto y decide:
// borrarla (se va del servidor y de todas las galerías) o conservarla.
@Composable
fun SolicitudesBorradoScreen(
    solicitudes: List<SolicitudBorrado>,
    cargando: Boolean,
    onBackClick: () -> Unit,
    onAprobar: (SolicitudBorrado) -> Unit,
    onRechazar: (SolicitudBorrado) -> Unit,
    modifier: Modifier = Modifier,
) {
    // borrar no se puede deshacer, así que antes se confirma.
    var porBorrar by remember { mutableStateOf<SolicitudBorrado?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            BackHeader(
                overline = "Centro",
                title = "Fotos que piden borrar",
                onBackClick = onBackClick,
            )
        }
        if (solicitudes.isEmpty() && !cargando) {
            item {
                Text(
                    text = "No hay solicitudes pendientes.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(items = solicitudes, key = { "solicitud-${it.id}" }) { solicitud ->
            SolicitudCard(
                solicitud = solicitud,
                onBorrar = { porBorrar = solicitud },
                onConservar = { onRechazar(solicitud) },
            )
        }
    }

    porBorrar?.let { solicitud ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            title = { Text("¿Borrar la foto?") },
            text = {
                Text("Se borra del servidor y desaparece de las galerías de todas las familias. No se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    porBorrar = null
                    onAprobar(solicitud)
                }) { Text("Borrar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { porBorrar = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun SolicitudCard(
    solicitud: SolicitudBorrado,
    onBorrar: () -> Unit,
    onConservar: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (solicitud.fotoUrl != null) {
                AsyncImage(
                    model = solicitud.fotoUrl,
                    contentDescription = "Foto que se pidió borrar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
            Text(
                text = if (solicitud.ninos.isEmpty()) "Sin niños etiquetados" else solicitud.ninos.joinToString(", "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "La pidió ${solicitud.solicitante} · ${solicitud.fecha}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = solicitud.motivo?.let { "Motivo: $it" } ?: "No escribió un motivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onBorrar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) { Text("Borrar la foto") }
                OutlinedButton(onClick = onConservar, modifier = Modifier.weight(1f)) { Text("Conservar") }
            }
        }
    }
}
