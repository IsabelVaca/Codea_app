package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import mx.tec.codea.R
import mx.tec.codea.domain.ReportType
import mx.tec.codea.domain.SavedReport
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.DatoTarjeta
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.components.imagenEstable
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// el botón "Ver reportes" de la tarjeta grande de Mi día abre esto: los reportes de ese momento,
// en tarjetas con una vista previa, y al tocar una se abre su info completa. las fotos se ven
// chicas en la tarjeta y completas al tocarlas, como en el visor de fotos del padre.
@Composable
fun ReportsScreen(
    subprocessName: String,
    reports: List<SavedReport>,
    loading: Boolean,
    childNameOf: (String) -> String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        BackHeader(
            title = stringResource(R.string.reports_title),
            overline = subprocessName,
            onBackClick = onBackClick,
        )
        Spacer(Modifier.height(14.dp))
        when {
            loading -> Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CodeaColores.VioletaTexto)
            }
            reports.isEmpty() -> Text(
                text = stringResource(R.string.reports_empty),
                fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp,
                color = CodeaColores.GrisTexto,
                modifier = Modifier.padding(top = 8.dp),
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        childNames = report.childIds.mapNotNull(childNameOf),
                        onClick = { selectedId = report.id },
                    )
                }
            }
        }
    }

    reports.firstOrNull { it.id == selectedId }?.let { report ->
        ReportDetailDialog(
            report = report,
            childNames = report.childIds.mapNotNull(childNameOf),
            onDismiss = { selectedId = null },
            onOpenPhoto = { photoUrl = report.photoUrl },
        )
    }

    photoUrl?.let { url ->
        FullPhotoDialog(url = url, onDismiss = { photoUrl = null })
    }
}

// la miniatura o el icono, el tipo, la hora, la descripción (dos líneas) y los niños.
@Composable
private fun ReportCard(report: SavedReport, childNames: List<String>, onClick: () -> Unit) {
    TarjetaBlanca(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportThumbnail(report)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TypePill(report.type)
                    Text(
                        report.time, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = CodeaColores.GrisTexto,
                    )
                }
                Text(
                    report.description,
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp,
                    color = CodeaColores.Ink,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                if (childNames.isNotEmpty()) {
                    Text(
                        childNames.joinToString(", "),
                        fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = CodeaColores.Gris,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportThumbnail(report: SavedReport) {
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(CodeaColores.Lila),
        contentAlignment = Alignment.Center,
    ) {
        if (report.photoUrl != null) {
            AsyncImage(
                model = imagenEstable(report.photoUrl),
                contentDescription = stringResource(R.string.reports_photo_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(Icons.Filled.Description, contentDescription = null, tint = CodeaColores.Morado)
        }
    }
}

// la incidencia se marca en rojo, como en la pantalla de fotos del padre; el resto en morado.
@Composable
private fun TypePill(type: ReportType) {
    val isIncident = type == ReportType.INCIDENT
    Text(
        text = stringResource(type.labelRes()),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isIncident) CodeaColores.RojoSuave else CodeaColores.Lila)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
        color = if (isIncident) CodeaColores.Rojo else CodeaColores.Morado,
    )
}

// la info completa del reporte: tipo, hora, descripción entera, niños y, si hay, la foto
// (chica; se toca para abrirla completa en FullPhotoDialog).
@Composable
private fun ReportDetailDialog(
    report: SavedReport,
    childNames: List<String>,
    onDismiss: () -> Unit,
    onOpenPhoto: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        TarjetaBlanca {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TypePill(report.type)
                    Text(
                        report.time, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = CodeaColores.GrisTexto,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.reports_close))
                }
            }
            Text(
                report.description,
                fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp,
                color = CodeaColores.Ink,
            )
            if (childNames.isNotEmpty()) {
                DatoTarjeta(etiqueta = stringResource(R.string.reports_children_label), valor = childNames.joinToString(", "))
            }
            if (report.photoUrl != null) {
                AsyncImage(
                    model = imagenEstable(report.photoUrl),
                    contentDescription = stringResource(R.string.reports_photo_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.4f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CodeaColores.Lila)
                        .clickable(onClick = onOpenPhoto),
                )
                Text(
                    stringResource(R.string.reports_open_photo_hint),
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = CodeaColores.VioletaTexto,
                )
            }
        }
    }
}

// el visor de pantalla completa de la foto, igual de simple que el de Fotos del padre: solo cerrar.
@Composable
private fun FullPhotoDialog(url: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color(0xFF1D1830)).systemBarsPadding()) {
            AsyncImage(
                model = imagenEstable(url),
                contentDescription = stringResource(R.string.reports_photo_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(20.dp),
            )
            Box(
                Modifier
                    .padding(14.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.reports_close), tint = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportsScreenPreview() {
    CodeaTheme {
        ReportsScreen(
            subprocessName = "Subproceso 3 · Comida",
            reports = listOf(
                SavedReport(
                    id = 1,
                    type = ReportType.INCIDENT,
                    method = mx.tec.codea.domain.ReportMethod.WRITTEN,
                    description = "Raspón leve en la rodilla al jugar en el patio.",
                    childIds = setOf("1"),
                    photoUrl = null,
                    time = "11:20",
                ),
            ),
            loading = false,
            childNameOf = { "Sofía" },
            onBackClick = {},
        )
    }
}
