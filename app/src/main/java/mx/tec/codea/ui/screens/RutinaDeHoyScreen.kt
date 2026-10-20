package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Routine
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.screens.admin.BotonPrincipal
import mx.tec.codea.ui.theme.Poppins

// "rutina de hoy": se muestra una sola vez, justo después de registrar la entrada.
// es solo para ver: la rutina de la sala la asigna coordinación, así que aquí no se
// reordena ni se apaga nada. el botón lleva a "mi día", que ya tiene esta misma rutina.
@Composable
fun RutinaDeHoyScreen(
    routine: Routine,
    onComenzar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val momentos = routine.subprocesses

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CodeaColores.Lila)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        routine.name,
                        fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Morado,
                    )
                    Text(
                        "Esta es la rutina que coordinación asignó a tu sala para hoy. " +
                            "Sin horarios: tú solo confirmas cada momento cuando termina.",
                        fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium, color = CodeaColores.Morado,
                    )
                }
            }

            if (momentos.isEmpty()) {
                item(key = "vacia") {
                    Text(
                        "Todavía no se carga la rutina de tu sala. Si no aparece en unos segundos, " +
                            "tu sala no tiene una rutina asignada: avisa a coordinación.",
                        fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF8C7FA8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    )
                }
            }

            itemsIndexed(momentos, key = { _, momento -> momento.number }) { i, momento ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x1F2A2140), spotColor = Color(0x1F2A2140))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(Color(0xFFEFE9FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${i + 1}",
                            fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5B3FE0),
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            momento.name,
                            fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140),
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        categoriaDeMomento(momento.category)?.let { categoria ->
                            Text(
                                categoria,
                                fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA79BBF),
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            if (momentos.isNotEmpty()) {
                item(key = "resumen") {
                    Text(
                        "${momentos.size} momentos, en este orden",
                        fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8C7FA8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        BotonPrincipal(
            if (momentos.isEmpty()) "Ir a mi día" else "Comenzar el día",
            CodeaColores.Violeta,
            onComenzar,
        )
    }
}

// la categoría llega de la API en inglés; aquí la mostramos en español.
private fun categoriaDeMomento(categoria: String?): String? = when (categoria) {
    "ARRIVAL" -> "Llegada"
    "MEAL" -> "Comida"
    "ACTIVITY" -> "Actividad"
    "NAP" -> "Siesta"
    "HYGIENE" -> "Higiene"
    "PICKUP" -> "Salida"
    else -> null
}
