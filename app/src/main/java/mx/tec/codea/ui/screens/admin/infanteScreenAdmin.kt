package mx.tec.codea.ui.screens.admin

import mx.tec.codea.ui.components.CodeaColores
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.theme.CodeaTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import mx.tec.codea.domain.Infante
import androidx.compose.ui.draw.shadow

@Composable
fun InfanteCard(infante: Infante, onClick: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxWidth(1f).padding(vertical = 5.dp, horizontal = 15.dp).shadow(
            elevation = 20.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color.Black.copy(alpha = 0.70f),
        spotColor = Color.Black.copy(alpha = 0.30f)
    ).clickable(onClick = onClick),
        color = CodeaColores.Tarjeta,
        border = BorderStroke(1.dp, CodeaColores.Borde),
        shape = RoundedCornerShape(18.dp))
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //siglas del nombre
            Box(
                modifier = Modifier.padding(18.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CodeaColores.Lila)
                    ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = infante.iniciales,
                    color = CodeaColores.Morado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            //Nombre y datos
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = infante.nombre,
                    modifier = Modifier.padding(top = 10.dp),
                    color = CodeaColores.Titulo,
                    fontSize = 17.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold
                )
                FlowRow(
                    modifier = Modifier.padding(top = 3.dp, bottom = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    //edad
                    Text(
                        text = "${infante.edad} años",
                        modifier = Modifier,
                        color = CodeaColores.GrisTexto,
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )

                }

            }

            // dado de baja: «De baja» en gris en lugar de la sala.
            Surface(
                modifier = Modifier.padding(end = 9.dp),
                color = if (infante.baja) CodeaColores.Campo else CodeaColores.Lila,
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = if (infante.baja) "De baja" else infante.sala,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    color = if (infante.baja) CodeaColores.Gris else CodeaColores.Morado,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }

        }

    }
}

@Composable
fun InscribirInfanteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, CodeaColores.Lila),
        colors = ButtonDefaults.buttonColors(
            containerColor = CodeaColores.Lila,
            contentColor = CodeaColores.Morado
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "+ Inscribir a un infante",
            fontSize = 15.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold
        )
    }
}
