package mx.tec.codea.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.ReportMethod
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.muted

// paso 1 del reporte: ¿cómo quiere levantarlo la docente?
// la foto es la ruta principal del prototipo, así que es la tarjeta grande; escribir y dictar son las alternativas, lado a lado.
// el aviso de "se puede cambiar después" va como texto al final, sin caja. solo hay violetas y neutros.
@Composable
fun SelectReportMethodScreen(
    subprocess: Subprocess,
    onMethodSelected: (ReportMethod) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        MethodHeader(subprocess = subprocess, onBackClick = onBackClick)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.report_method_question),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.report_method_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PhotoOption(onClick = { onMethodSelected(ReportMethod.PHOTO) })
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MethodTile(
                    title = stringResource(R.string.report_method_written),
                    subtitle = stringResource(R.string.report_method_written_subtitle),
                    icon = Icons.Filled.Edit,
                    onClick = { onMethodSelected(ReportMethod.WRITTEN) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                MethodTile(
                    title = stringResource(R.string.report_method_dictation),
                    subtitle = stringResource(R.string.report_method_dictation_subtitle),
                    icon = Icons.Filled.Mic,
                    onClick = { onMethodSelected(ReportMethod.DICTATION) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.report_method_can_change),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Text(
                text = stringResource(R.string.report_method_can_change_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
        }
    }
}

// el botón de regreso y, a su lado, el título y debajo en qué momento de la rutina se hace el reporte.
@Composable
private fun MethodHeader(subprocess: Subprocess, onBackClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(colors.primaryContainer, CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                tint = colors.onPrimaryContainer,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.report_new_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.report_context, subprocess.number, subprocess.name),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
        }
    }
}

// la opción principal: la tarjeta grande en violeta pleno. toda la tarjeta se puede tocar.
@Composable
private fun PhotoOption(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.primary,
        contentColor = colors.onPrimary,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 120.dp)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colors.onPrimary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(28.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.report_method_photo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.report_method_photo_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onPrimary.copy(alpha = 0.85f),
                )
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

// una de las dos alternativas: icono arriba, nombre y una línea de lo que se hace. toda la tarjeta se puede tocar.
@Composable
private fun MethodTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SelectReportMethodScreenPreview() {
    CodeaTheme {
        SelectReportMethodScreen(
            subprocess = Subprocess(4, "Patio y juego libre", SubprocessStatus.ACTIVE),
            onMethodSelected = {},
            onBackClick = {},
        )
    }
}
