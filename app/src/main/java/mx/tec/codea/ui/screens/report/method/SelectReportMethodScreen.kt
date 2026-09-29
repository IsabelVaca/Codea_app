package mx.tec.codea.ui.screens.report.method

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.Sun

@Composable
fun SelectReportMethodScreen(
    modifier: Modifier = Modifier,
    subprocessName: String = "PATIO Y JUEGO LIBRE",
    onMethodSelected: (ReportMethod) -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header con botón de regreso y título estilizado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            ScreenHeader(
                overline = "SUBPROCESO · $subprocessName",
                title = "Nuevo reporte"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card morada de explicación
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "¿Cómo lo quieres levantar?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Las tres formas guardan lo mismo: elige la que te quede a mano en la actividad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Opción 1: Escrito
        MethodOptionCard(
            title = "Escrito",
            subtitle = "Teclea la nota · 30 s",
            iconText = "📝",
            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
            highlightBorder = false,
            onClick = { onMethodSelected(ReportMethod.WRITTEN) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Opción 2: Dictado
        MethodOptionCard(
            title = "Dictado",
            subtitle = "Habla y la app redacta · 10 s",
            iconText = "🎙️",
            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
            highlightBorder = false,
            onClick = { onMethodSelected(ReportMethod.DICTATION) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Opción 3: Con foto (Resaltado con borde ámbar)
        MethodOptionCard(
            title = "Con foto",
            subtitle = "Toma la foto y luego describes",
            iconText = "📷",
            iconBgColor = Sun.copy(alpha = 0.3f),
            highlightBorder = true,
            onClick = { onMethodSelected(ReportMethod.PHOTO) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Banner de confirmación verde (Teal)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Se puede cambiar después",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aunque empieces con foto, puedes dictar el texto en el paso siguiente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Componente individual para cada una de las 3 opciones de reporte
 */
@Composable
private fun MethodOptionCard(
    title: String,
    subtitle: String,
    iconText: String,
    iconBgColor: Color,
    highlightBorder: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = if (highlightBorder) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconText, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (highlightBorder) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
