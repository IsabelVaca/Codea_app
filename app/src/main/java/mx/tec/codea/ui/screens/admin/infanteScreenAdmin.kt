package mx.tec.codea.ui.screens.admin

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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import mx.tec.codea.data.fakeInfantes
import mx.tec.codea.model.Infante
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfanteScreen(){
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Infantes inscritos") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(1f).padding(vertical = 3.dp, horizontal = 15.dp),
                    color = Color(red = 254, green = 245, blue = 217),
                    shape = RoundedCornerShape(18.dp)
                ){
                    Column {
                        Text(
                            text = "${fakeInfantes.size}",
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 5.dp
                            ).padding(top = 12.dp),
                            color = Color(red = 171, green = 129, blue = 44),
                            fontSize = 24.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Niños inscritos en las 3 salas",
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 2.dp

                            ).padding(bottom = 12.dp),
                            color = Color(red = 143, green = 125, blue = 82),
                            fontSize = 14.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold
                        )
                    }



                }

                Text(text = "INFANTES INSCRITOS",
                    modifier = Modifier.padding(
                        horizontal = 19.dp,
                        vertical = 2.dp

                    ).padding(top = 7.dp, bottom = 7.dp), color = Color(red = 138, green = 128, blue = 165),
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold)

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(fakeInfantes) { infante ->
                        InfanteCard(infante = infante)
                    }

                    item {
                        InscribirInfanteButton(
                            onClick = { },
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
                        )
                    }

                }
            }
        }
    )
}

@Composable
fun InfanteCard(infante: Infante) {
    Surface(modifier = Modifier.fillMaxWidth(1f).padding(vertical = 5.dp, horizontal = 15.dp).shadow(
            elevation = 20.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color.Black.copy(alpha = 0.70f),
        spotColor = Color.Black.copy(alpha = 0.30f)
    ),
        color = Color(red = 255, green = 255, blue = 255),
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
                    .background(Color(0xFFEDE9FE))
                    ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = infante.iniciales,
                    color = Color(0xFF6D28D9),
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
                    color = Color(red = 1, green = 1, blue = 1),
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
                        text = "${infante.edad} años" + " • ",
                        modifier = Modifier,
                        color = Color(red = 138, green = 128, blue = 165),
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                    //tutor
                    Text(
                        text = infante.tutor + " • ",
                        modifier = Modifier,
                        color = Color(red = 138, green = 128, blue = 165),
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                    //titulo
                    Text(
                        text = infante.tituloTutor,
                        modifier = Modifier,
                        color = Color(red = 138, green = 128, blue = 165),
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )

                }

            }

            Surface(
                modifier = Modifier.padding(end = 9.dp),
                color = Color(0xFFEDE9FE),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = infante.sala,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    color = Color(0xFF6D28D9),
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
        border = BorderStroke(2.dp, Color(0xFFEDE9FE)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEDE9FE),
            contentColor = Color(0xFF6D28D9)
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

@Preview(showBackground = true)
@Composable
fun InfanteScreenPreview() {
    CodeaTheme {
        InfanteScreen()
    }
}
