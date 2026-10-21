package mx.tec.codea.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import mx.tec.codea.R
import mx.tec.codea.domain.ReportMethod
import mx.tec.codea.domain.ReportType
import mx.tec.codea.domain.ReportValidator
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.state.ReportFormUiState
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

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
            containerColor = CodeaColores.Lila,
            titleColor = CodeaColores.Morado,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onContinueClick,
                enabled = uiState.canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = BotonVioleta(),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(
                    text = stringResource(
                        if (uiState.canContinue) R.string.report_continue else R.string.report_continue_blocked,
                    ),
                    fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                )
            }
            if (!uiState.canContinue) {
                Text(
                    text = stringResource(R.string.report_continue_hint, ReportValidator.DESCRIPTION_MIN),
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris,
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
    TarjetaBlanca {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CodeaColores.Lila),
            contentAlignment = Alignment.Center,
        ) {
            val photoUri = uiState.photoUri
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = stringResource(R.string.report_photo_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = CodeaColores.Morado, modifier = Modifier.size(30.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.report_photo_attached, uiState.photoTime),
                fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink,
            )
            Text(
                text = stringResource(R.string.report_photo_taken_during, subprocessName),
                fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
            )
            TextButton(onClick = onChangePhotoClick, colors = ButtonDefaults.textButtonColors(contentColor = CodeaColores.Violeta)) {
                Text(stringResource(R.string.report_change_photo), fontFamily = Poppins, fontWeight = FontWeight.Bold)
            }
        }
    }
    }
}

// the big button that opens the dictation of the phone.
@Composable
private fun DictationCard(onClick: () -> Unit) {
    TarjetaBlanca(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(CodeaColores.Lila),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, tint = CodeaColores.Morado, modifier = Modifier.size(26.dp))
            }
            Column {
                Text(
                    text = stringResource(R.string.report_dictation_title),
                    fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink,
                )
                Text(
                    text = stringResource(R.string.report_dictation_body),
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
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
            .background(CodeaColores.Campo, RoundedCornerShape(14.dp))
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.report_no_photo),
            fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAttachPhotoClick, colors = ButtonDefaults.textButtonColors(contentColor = CodeaColores.Violeta)) {
            Text(stringResource(R.string.report_attach_photo), fontFamily = Poppins, fontWeight = FontWeight.Bold)
        }
    }
}

// the four kinds of report. only one can be picked.
@Composable
private fun TypeSelector(selected: ReportType, onTypeChange: (ReportType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EtiquetaCampo(stringResource(R.string.report_type_label))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReportType.entries.forEach { type ->
                val isSelected = type == selected
                Surface(
                    onClick = { onTypeChange(type) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) CodeaColores.Lila else CodeaColores.Campo,
                    border = if (isSelected) BorderStroke(2.dp, CodeaColores.Morado) else null,
                ) {
                    Text(
                        text = stringResource(type.labelRes()),
                        fontFamily = Poppins, fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) CodeaColores.Morado else CodeaColores.GrisTexto,
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
        EtiquetaCampo(stringResource(R.string.report_what_happened))
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            placeholder = { Text(stringResource(R.string.report_description_hint), fontFamily = Poppins, color = CodeaColores.Gris) },
            minLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            textStyle = TextStyle(fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CodeaColores.Ink),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = CodeaColores.Morado,
                unfocusedBorderColor = CodeaColores.Lila,
                cursorColor = CodeaColores.Morado,
            ),
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
                fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (isTooShort) CodeaColores.Rojo else CodeaColores.Gris,
            )
            TextButton(onClick = onDictateClick, colors = ButtonDefaults.textButtonColors(contentColor = CodeaColores.Violeta)) {
                Text(stringResource(R.string.report_dictate_instead), fontFamily = Poppins, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// the label above a field, like the ones of the parent forms: small, uppercase and grey.
@Composable
private fun EtiquetaCampo(text: String) {
    Text(
        text.uppercase(),
        fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp, color = CodeaColores.Gris,
    )
}

// the violet of the main buttons; grey when the button is off.
@Composable
internal fun BotonVioleta() = ButtonDefaults.buttonColors(
    containerColor = CodeaColores.Violeta,
    contentColor = Color.White,
    disabledContainerColor = CodeaColores.Campo,
    disabledContentColor = CodeaColores.Gris,
)

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
