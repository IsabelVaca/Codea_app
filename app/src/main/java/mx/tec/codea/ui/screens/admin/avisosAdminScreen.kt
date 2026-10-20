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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Aviso
import mx.tec.codea.domain.Sala
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.AvisoImagen
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.components.rememberPickPhoto
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton

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
    formularioAbierto: Boolean = false,      // false = solo el botón «+ Nuevo aviso»
    onNuevoAviso: () -> Unit = {},
    onCancelar: () -> Unit = {},
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
            if (!formularioAbierto) {
                NuevoAvisoButton(onClick = onNuevoAviso)
            } else NuevoAvisoCard(
                salas = salas,
                destinoId = destino?.id,
                onDestinoChange = onDestinoChange,
                texto = avisoTexto,
                onTextoChange = onAvisoTextoChange,
                placeholder = if (destino == null) "Escribe el aviso para todas las salas…"
                else "Escribe el aviso para ${destino.nombre}…",
                foto = avisoFoto,
                onElegirFoto = elegirFoto,
                onQuitarFoto = { onAvisoFotoChange(null) },
                canPublicar = canPublicarAviso,
                publicando = publicando,
                onPublicar = onPublicarAviso,
                onCancelar = onCancelar,
            )

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

// el botón grande de arriba; al tocarlo aparece el formulario. mismo estilo que «+ Crear sala».
@Composable
private fun NuevoAvisoButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDE9FE), contentColor = Color(0xFF6D28D9)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text("+ Nuevo aviso", fontSize = 15.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold)
    }
}

// el formulario en una sola tarjeta: a quién va arriba, el texto en medio y, abajo,
// el botón de imagen a la izquierda y «Publicar» a la derecha.
@Composable
private fun NuevoAvisoCard(
    salas: List<Sala>,
    destinoId: String?,
    onDestinoChange: (String?) -> Unit,
    texto: String,
    onTextoChange: (String) -> Unit,
    placeholder: String,
    foto: Uri?,
    onElegirFoto: () -> Unit,
    onQuitarFoto: () -> Unit,
    canPublicar: Boolean,
    publicando: Boolean,
    onPublicar: () -> Unit,
    onCancelar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Para", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AvisosColores.Gris)
            DestinoChip(salas = salas, seleccionId = destinoId, onSeleccion = onDestinoChange)
        }
        CampoAviso(valor = texto, onCambio = onTextoChange, placeholder = placeholder)
        if (foto != null) {
            Box {
                // la vista previa se ve igual que en el historial: completa y con su relación de aspecto.
                AvisoImagen(model = foto)
                IconButton(
                    onClick = onQuitarFoto,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Quitar imagen", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        HorizontalDivider(color = AvisosColores.Campo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onElegirFoto, enabled = !publicando) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = AvisosColores.Violeta, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (foto == null) "Imagen" else "Cambiar",
                    fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AvisosColores.Violeta
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onCancelar, enabled = !publicando) {
                    Text("Cancelar", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AvisosColores.Gris)
                }
                Button(
                    onClick = onPublicar,
                    enabled = canPublicar,
                    colors = ButtonDefaults.buttonColors(containerColor = AvisosColores.Violeta, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(if (publicando) "Publicando…" else "Publicar", fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// a quién va el aviso, en una pastilla que abre la lista: todas las salas o solo una.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinoChip(
    salas: List<Sala>,
    seleccionId: String?,
    onSeleccion: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val etiqueta = salas.firstOrNull { it.id == seleccionId }?.nombre ?: "Todas las salas"

    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
        Row(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(50))
                .background(AvisosColores.VioletaSuave)
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(etiqueta, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AvisosColores.Violeta)
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = AvisosColores.Violeta, modifier = Modifier.size(20.dp))
        }
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Todas las salas", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AvisosColores.Ink) },
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

// el texto del aviso, sin caja propia: ya va dentro de la tarjeta.
@Composable
private fun CampoAviso(valor: String, onCambio: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        textStyle = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp, color = AvisosColores.Ink),
        cursorBrush = SolidColor(AvisosColores.Violeta),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { interno ->
            Box(Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
                if (valor.isEmpty()) {
                    Text(placeholder, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AvisosColores.Gris)
                }
                interno()
            }
        }
    )
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
