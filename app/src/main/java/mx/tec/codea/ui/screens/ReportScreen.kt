package mx.tec.codea.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.ReportMethod
import mx.tec.codea.domain.ReportType
import mx.tec.codea.domain.ReportValidator
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.state.ReportFormUiState
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Sun

// step 2 of the report: what happened? (screen 5, route a and e1 of the prototype).
// the camera and the dictation are started by the parent, this screen only reports the taps.
@Composable
fun ReportScreen(
    subprocess: Subprocess,
    uiState: ReportFormUiState,
    onTypeChange: (ReportType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDictateClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BackHeader(
            overline = stringResource(R.string.report_overline, subprocess.number, subprocess.name),
            title = stringResource(
                if (uiState.method == ReportMethod.WRITTEN) R.string.report_written_title else R.string.report_new_title,
            ),
            onBackClick = onBackClick,
        )

        // the top card changes with the way the teacher chose to write the report.
        when (uiState.method) {
            ReportMethod.PHOTO -> PhotoCard(
                uiState = uiState,
                subprocessName = subprocess.name,
                onChangePhotoClick = onTakePhotoClick,
            )
            ReportMethod.DICTATION -> DictationCard(onClick = onDictateClick)
            ReportMethod.WRITTEN -> NoPhotoRow(onAttachPhotoClick = onTakePhotoClick)
        }

        TypeSelector(selected = uiState.type, onTypeChange = onTypeChange)

        DescriptionField(
            uiState = uiState,
            onDescriptionChange = onDescriptionChange,
            onDictateClick = onDictateClick,
        )

        InfoBox(
            title = stringResource(R.string.report_next_step),
            body = stringResource(R.string.report_next_step_body),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            titleColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onContinueClick,
                enabled = uiState.canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = stringResource(
                        if (uiState.canContinue) R.string.report_continue else R.string.report_continue_blocked,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (!uiState.canContinue) {
                Text(
                    text = stringResource(R.string.report_continue_hint, ReportValidator.DESCRIPTION_MIN),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// the photo that was taken, when it was taken, and a way to take it again.
@Composable
private fun PhotoCard(uiState: ReportFormUiState, subprocessName: String, onChangePhotoClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            val photo = uiState.photo
            if (photo != null) {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = stringResource(R.string.report_photo_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Sun, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "📷", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.report_photo_attached, uiState.photoTime),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.report_photo_taken_during, subprocessName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            TextButton(onClick = onChangePhotoClick) {
                Text(stringResource(R.string.report_change_photo), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// the big button that opens the dictation of the phone.
@Composable
private fun DictationCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🎙️", style = MaterialTheme.typography.titleMedium)
            }
            Column {
                Text(
                    text = stringResource(R.string.report_dictation_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.report_dictation_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

// route a of the prototype: a written report has no photo, but it can still get one.
@Composable
private fun NoPhotoRow(onAttachPhotoClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.report_no_photo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAttachPhotoClick) {
            Text(stringResource(R.string.report_attach_photo), fontWeight = FontWeight.Bold)
        }
    }
}

// the four kinds of report. only one can be picked.
@Composable
private fun TypeSelector(selected: ReportType, onTypeChange: (ReportType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.report_type_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReportType.entries.forEach { type ->
                val isSelected = type == selected
                Surface(
                    onClick = { onTypeChange(type) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                ) {
                    Text(
                        text = stringResource(type.labelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

// the text of the report, a counter and a shortcut to dictate instead of typing.
@Composable
private fun DescriptionField(
    uiState: ReportFormUiState,
    onDescriptionChange: (String) -> Unit,
    onDictateClick: () -> Unit,
) {
    val isTooShort = !uiState.canContinue

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.report_what_happened),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            placeholder = { Text(stringResource(R.string.report_description_hint)) },
            minLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isTooShort) {
                    stringResource(R.string.report_characters_needed, uiState.descriptionLength, ReportValidator.DESCRIPTION_MIN)
                } else {
                    stringResource(R.string.report_characters, uiState.descriptionLength)
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isTooShort) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            )
            TextButton(onClick = onDictateClick) {
                Text(stringResource(R.string.report_dictate_instead), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// the spanish name of each kind of report. the domain only knows the enum,
// and the ui turns it into text, so the domain does not care about the language.
@StringRes
internal fun ReportType.labelRes(): Int = when (this) {
    ReportType.INCIDENT -> R.string.report_type_incident
    ReportType.ACHIEVEMENT -> R.string.report_type_achievement
    ReportType.HEALTH -> R.string.report_type_health
    ReportType.BEHAVIOR -> R.string.report_type_behavior
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReportScreenPreview() {
    CodeaTheme {
        ReportScreen(
            subprocess = Subprocess(4, "Patio y juego libre", SubprocessStatus.ACTIVE),
            uiState = ReportFormUiState(method = ReportMethod.PHOTO, photoTime = "11:40"),
            onTypeChange = {},
            onDescriptionChange = {},
            onDictateClick = {},
            onTakePhotoClick = {},
            onContinueClick = {},
            onBackClick = {},
        )
    }
}
