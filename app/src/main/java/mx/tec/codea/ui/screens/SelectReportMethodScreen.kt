package mx.tec.codea.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.ReportMethod
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Sun

// step 1 of the report: how does the teacher want to write it?
// it copies screen 3 of "reporte con foto" in the prototype.
@Composable
fun SelectReportMethodScreen(
    subprocess: Subprocess,
    onMethodSelected: (ReportMethod) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BackHeader(
            overline = stringResource(R.string.report_overline, subprocess.number, subprocess.name),
            title = stringResource(R.string.report_new_title),
            onBackClick = onBackClick,
        )
        InfoBox(
            title = stringResource(R.string.report_method_question),
            body = stringResource(R.string.report_method_explanation),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        MethodOptionCard(
            title = stringResource(R.string.report_method_written),
            subtitle = stringResource(R.string.report_method_written_subtitle),
            iconText = "📝",
            iconBackground = MaterialTheme.colorScheme.primaryContainer,
            isHighlighted = false,
            onClick = { onMethodSelected(ReportMethod.WRITTEN) },
        )
        MethodOptionCard(
            title = stringResource(R.string.report_method_dictation),
            subtitle = stringResource(R.string.report_method_dictation_subtitle),
            iconText = "🎙️",
            iconBackground = MaterialTheme.colorScheme.primaryContainer,
            isHighlighted = false,
            onClick = { onMethodSelected(ReportMethod.DICTATION) },
        )
        // the photo is the path of the prototype, so it has the amber border.
        MethodOptionCard(
            title = stringResource(R.string.report_method_photo),
            subtitle = stringResource(R.string.report_method_photo_subtitle),
            iconText = "📷",
            iconBackground = Sun.copy(alpha = 0.3f),
            isHighlighted = true,
            onClick = { onMethodSelected(ReportMethod.PHOTO) },
        )
        InfoBox(
            title = stringResource(R.string.report_method_can_change),
            body = stringResource(R.string.report_method_can_change_body),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            titleColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

// one of the three options. the whole card can be tapped.
@Composable
private fun MethodOptionCard(
    title: String,
    subtitle: String,
    iconText: String,
    iconBackground: Color,
    isHighlighted: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = if (isHighlighted) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(iconBackground, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = iconText, style = MaterialTheme.typography.titleMedium)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isHighlighted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
            )
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
