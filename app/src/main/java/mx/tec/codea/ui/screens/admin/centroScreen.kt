package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.data.fakeSalas
import mx.tec.codea.model.Sala
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroScreen(
    salas: List<Sala>,
    avisoTexto: String,
    onAvisoTextoChange: (String) -> Unit,
    onPublicarAviso: () -> Unit,
    onCrearSala: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Centro") })
        },
        content = { paddingValues ->
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SeccionLabel("Ocupación de salas")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                salas.forEach { sala ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color.Black.copy(alpha = 0.12f),
                                spotColor = Color.Black.copy(alpha = 0.12f)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(sala.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                            Text(sala.cupoTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B3FE0))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFF4F1FB))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(sala.ocupacion)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(sala.color)
                            )
                        }
                        Text(sala.asistente, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA79BBF))
                    }
                }
            }

            CrearSalaButton(
                onClick = onCrearSala,
                modifier = Modifier.padding(top = 2.dp)
            )

            SeccionLabel("Publicar aviso del centro")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = avisoTexto,
                    onValueChange = onAvisoTextoChange,
                    placeholder = { Text("Escribe el aviso para las 3 salas…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF4F1FB),
                        unfocusedContainerColor = Color(0xFFF4F1FB),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2A2140))
                )
                Button(
                    onClick = onPublicarAviso,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFE9FF), contentColor = Color(0xFF5B3FE0)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Publicar aviso", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
        }
    )
}

@Composable
fun CrearSalaButton(
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
            text = "+ Crear sala",
            fontSize = 15.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CentroScreenPreview() {
    CodeaTheme {
        CentroScreen(
            salas = fakeSalas,
            avisoTexto = "",
            onAvisoTextoChange = {},
            onPublicarAviso = {},
            onCrearSala = {}
        )
    }
}
