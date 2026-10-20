package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.formatTimeOfDay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.theme.CodeaTheme

// the feedback after the check-in (screen 3 and e3 of the prototype).
// it tells the teacher it worked and stays until she taps "Ir a Mi día": it does not close by itself.
@Composable
fun CheckInDoneScreen(
    record: CheckInRecord,
    teacherName: String,
    onGoToMyDayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // con retardo, ámbar (lo que pide atención); a tiempo, lila con morado como el resto de la app.
    val acento = if (record.isLate) MaterialTheme.colorScheme.secondary else CodeaColores.Morado
    val fondoAcento = if (record.isLate) MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f) else CodeaColores.Lila
    val time = formatTimeOfDay(record.arrivalMinutes)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.weight(0.4f))

        // sin título arriba: el encabezado de en medio ya dice lo que pasó.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier.size(84.dp).clip(RoundedCornerShape(28.dp)).background(fondoAcento),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (record.isLate) Icons.Filled.Info else Icons.Filled.Check,
                    contentDescription = null,
                    tint = acento,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (record.isLate) R.string.checker_done_headline_late else R.string.checker_done_headline,
                ),
                fontFamily = Poppins, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.checker_done_time_place, time, record.placeName),
                fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
            )
            Text(
                text = if (record.isLate) {
                    pluralStringResource(R.plurals.checker_done_minutes_late, record.minutesLate, record.minutesLate)
                } else {
                    stringResource(R.string.checker_done_greeting, teacherName)
                },
                fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
            )
        }

        // lo que sigue, en una tarjeta blanca con sus íconos en cuadro lila.
        TarjetaBlanca(espacio = 14.dp) {
            if (record.isLate) {
                DoneItem(Icons.Filled.Info, stringResource(R.string.checker_done_coordination))
            } else {
                DoneItem(Icons.Filled.Check, stringResource(R.string.checker_done_no_paper))
            }
            DoneItem(
                Icons.Filled.Refresh,
                stringResource(if (record.isLate) R.string.checker_done_day_starts else R.string.checker_done_exit),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGoToMyDayClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Violeta, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Text(stringResource(R.string.checker_done_go_to_my_day), fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// una línea de la tarjeta: el ícono en un cuadro lila y el texto.
@Composable
private fun DoneItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(CodeaColores.Lila),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CodeaColores.Morado, modifier = Modifier.size(20.dp))
        }
        Text(text, fontFamily = Poppins, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Ink)
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CheckInDoneScreenPreview() {
    CodeaTheme {
        CheckInDoneScreen(
            record = CheckInRecord(arrivalMinutes = 472, placeName = "La Oruga", minutesLate = 0, lateReason = ""),
            teacherName = "miss Karla",
            onGoToMyDayClick = {},
        )
    }
}
