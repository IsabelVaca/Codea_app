package mx.tec.codea.ui.screens.admin

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import mx.tec.codea.domain.Aviso
import mx.tec.codea.domain.Sala
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.AvisoImagen
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.components.rememberPickPhoto

// la pestaña Avisos del admin: publicar un aviso (a todas las salas o a una) y ver el historial.
// antes era "Centro" y también tenía la ocupación de salas, que ahora vive en Docentes → Salas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosAdminScreen(
    salas: List<Sala>,                       // para elegir el destino del aviso
    avisoTexto: String,
    onAvisoTextoChange: (String) -> Unit,
    avisoFoto: Uri? = null,                  // la imagen elegida para el aviso
    onAvisoFotoChange: (Uri?) -> Unit = {},
    onPublicarAviso: () -> Unit,
    canPublicarAviso: Boolean,
    publicando: Boolean = false,             // el aviso se está subiendo (con imagen puede tardar unos segundos)
    destinoSalaId: String? = null,           // null = todas las salas
    onDestinoChange: (String?) -> Unit = {},
    avisos: List<Aviso> = emptyList(),       // el historial, el más reciente primero
) {
    // si la sala elegida deja de existir, el aviso vuelve a ser para todas.
    val destino = salas.firstOrNull { it.id == destinoSalaId }
    val elegirFoto = rememberPickPhoto { onAvisoFotoChange(it) }
    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Avisos",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
            )
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
            SeccionLabel("Publicar aviso del centro")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DestinoDropdown(
                    salas = salas,
                    seleccionId = destino?.id,
                    onSeleccion = onDestinoChange
                )
                CampoAviso(
                    valor = avisoTexto,
                    onCambio = onAvisoTextoChange,
                    placeholder = if (destino == null) "Escribe el aviso para todas las salas…"
                    else "Escribe el aviso para ${destino.nombre}…"
                )
                FotoDelAviso(foto = avisoFoto, onElegir = elegirFoto, onQuitar = { onAvisoFotoChange(null) })
                Button(
                    onClick = onPublicarAviso,
                    enabled = canPublicarAviso,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFE9FF), contentColor = Color(0xFF5B3FE0)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(if (publicando) "Publicando…" else "Publicar aviso", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            SeccionLabel("Historial de avisos")
            if (avisos.isEmpty()) {
                Text(
                    "Todavía no se ha publicado ningún aviso.",
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AvisosColores.Gris
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                avisos.forEach { aviso -> AvisoCard(aviso) }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
        }
    )
}

private object AvisosColores {
    val Ink = Color(0xFF2A2140)
    val Violeta = Color(0xFF5B3FE0)
    val VioletaSuave = Color(0xFFEFE9FF)
    val Campo = Color(0xFFF4F1FB)
    val Gris = Color(0xFFA79BBF)
    val Verde = Color(0xFF0F7A6C)
    val VerdeSuave = Color(0xFFE8FBF6)
}

// elige a quién va el aviso: a todas las salas o solo a una.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinoDropdown(
    salas: List<Sala>,
    seleccionId: String?,
    onSeleccion: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val etiqueta = salas.firstOrNull { it.id == seleccionId }?.let { "Solo ${it.nombre}" }
        ?: "Para todos · todas las salas"

    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
        Row(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(AvisosColores.Campo)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(etiqueta, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AvisosColores.Ink)
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = AvisosColores.Violeta)
        }
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Para todos · todas las salas", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AvisosColores.Ink) },
                onClick = { onSeleccion(null); abierto = false }
            )
            salas.forEach { sala ->
                DropdownMenuItem(
                    text = { Text("Solo ${sala.nombre}", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AvisosColores.Ink) },
                    onClick = { onSeleccion(sala.id); abierto = false }
                )
            }
        }
    }
}

@Composable
private fun CampoAviso(valor: String, onCambio: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        textStyle = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AvisosColores.Ink),
        cursorBrush = SolidColor(AvisosColores.Violeta),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { interno ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AvisosColores.Campo)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (valor.isEmpty()) {
                    Text(placeholder, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AvisosColores.Gris)
                }
                interno()
            }
        }
    )
}

// la imagen que va con el aviso: un botón para elegirla o, ya elegida, su vista previa con «Quitar».
@Composable
private fun FotoDelAviso(foto: Uri?, onElegir: () -> Unit, onQuitar: () -> Unit) {
    if (foto == null) {
        TextButton(onClick = onElegir, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar imagen", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AvisosColores.Violeta)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AsyncImage(
            model = foto,
            contentDescription = "Imagen del aviso",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AvisosColores.Campo)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onElegir) {
                Text("Cambiar imagen", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AvisosColores.Violeta)
            }
            TextButton(onClick = onQuitar) {
                Text("Quitar", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AvisosColores.Gris)
            }
        }
    }
}

// una fila del historial: etiqueta de destino + fecha, el texto del aviso y su imagen.
@Composable
private fun AvisoCard(aviso: Aviso) {
    val paraTodos = aviso.destino == null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                aviso.destino ?: "Todas las salas",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (paraTodos) AvisosColores.VioletaSuave else AvisosColores.VerdeSuave)
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                fontFamily = Poppins, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                color = if (paraTodos) AvisosColores.Violeta else AvisosColores.Verde
            )
            Text(aviso.fecha, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = AvisosColores.Gris)
        }
        if (aviso.texto.isNotBlank()) {
            Text(
                aviso.texto, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, lineHeight = 21.sp, color = AvisosColores.Ink
            )
        }
        aviso.imagenUrl?.let { AvisoImagen(it) }
    }
}
