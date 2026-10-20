package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// lo que ve la docente después de "Finalizar día": su turno terminó y su salida quedó registrada.
// reemplaza a la rutina en Mi día hasta que llegue el día siguiente.
@Composable
fun DayFinishedScreen(
    record: CheckInRecord,
    teacherName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.day_finished_title),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // el mismo cuadro lila con la palomita morada que al checar la entrada.
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CodeaColores.Lila),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = CodeaColores.Morado, modifier = Modifier.size(42.dp))
            }
            Text(
                text = stringResource(R.string.day_finished_headline, teacherName),
                fontFamily = Poppins, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            record.departureMinutes?.let { minutes ->
                Text(
                    text = stringResource(
                        if (record.autoClosed) R.string.day_finished_auto else R.string.day_finished_exit,
                        formatTimeOfDay(minutes),
                    ),
                    fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = stringResource(R.string.day_finished_tomorrow),
                fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DayFinishedScreenPreview() {
    CodeaTheme {
        DayFinishedScreen(
            record = CheckInRecord(arrivalMinutes = 472, placeName = "La Oruga", minutesLate = 0, lateReason = "", departureMinutes = 16 * 60 + 5),
            teacherName = "miss Karla",
        )
    }
}
