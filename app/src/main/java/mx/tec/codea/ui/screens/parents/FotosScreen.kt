package mx.tec.codea.ui.screens.parents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.tec.codea.ui.theme.CodeaTheme

// ---------- Colores ----------
private val Ink = Color(0xFF2B2542)
private val Muted = Color(0xFFA49CC0)
private val Body = Color(0xFF4A4363)
private val Bg = Color(0xFFFFFCF8)
private val Line = Color(0xFFEBE6F3)
private val SelectedRow = Color(0xFFF4F0FD)
private val Accent = Color(0xFFF0A202)
private val Incident = Color(0xFFC9503A)
private val ViewerBg = Color(0xFF1D1830)

// Agrega poppins_*.ttf en res/font. Si no, usa FontFamily.Default.
// private val Poppins = FontFamily(
//     Font(R.font.poppins_medium, FontWeight.Medium),
//     Font(R.font.poppins_semibold, FontWeight.SemiBold),
//     Font(R.font.poppins_bold, FontWeight.Bold),
//     Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
// )
private val Poppins = FontFamily.Default

// ---------- Modelo ----------
enum class Categoria(val label: String, val dot: Color) {
    TODAS("Todas", Ink),
    ACTIVIDADES("Actividades", Color(0xFF8B7BE0)),
    COMIDAS("Comidas", Accent),
    SIESTA("Siesta", Color(0xFF5FBF9A)),
    INCIDENTES("Incidentes", Incident),
}

enum class Tono(val a: Color, val b: Color) {
    LAVANDA(Color(0xFFEBE6FC), Color(0xFFE0D8F8)),
    AMARILLO(Color(0xFFFDF2D2), Color(0xFFF9E6AB)),
    MENTA(Color(0xFFE3F7EF), Color(0xFFCDEEE1)),
    DURAZNO(Color(0xFFFDE7E1), Color(0xFFF8D3C8)),
}

data class Foto(
    val id: Int,
    val dia: String,
    val fecha: String,
    val etiqueta: String,
    val categoria: Categoria,
    val tono: Tono,
    val hora: String,
    val maestra: String,
    val nota: String,
    val url: String? = null, // reemplazar placeholder por Coil AsyncImage
) {
    val esIncidente get() = categoria == Categoria.INCIDENTES
}

val fotosDemo = listOf(
    Foto(1, "Hoy", "mar 29 sep", "Trazos", Categoria.ACTIVIDADES, Tono.LAVANDA, "10:15", "Maestra Laura", "Mateo practicó líneas curvas con crayones gruesos. Ya sostiene muy bien el crayón."),
    Foto(2, "Hoy", "mar 29 sep", "Patio", Categoria.ACTIVIDADES, Tono.AMARILLO, "11:00", "Maestra Sofía", "Juego libre en el patio con sus compañeros de sala."),
    Foto(3, "Hoy", "mar 29 sep", "Rodilla", Categoria.INCIDENTES, Tono.DURAZNO, "12:40", "Maestra Sofía", "Raspón leve en la rodilla al correr. Se limpió y se aplicó hielo 10 min. Siguió jugando con normalidad."),
    Foto(4, "Hoy", "mar 29 sep", "Colación", Categoria.COMIDAS, Tono.DURAZNO, "13:00", "Maestra Laura", "Comió todo el arroz y la mitad de la fruta."),
    Foto(5, "Hoy", "mar 29 sep", "Siesta", Categoria.SIESTA, Tono.LAVANDA, "14:00", "Maestra Laura", "Durmió 1 h 20 min."),
    Foto(6, "Hoy", "mar 29 sep", "Música", Categoria.ACTIVIDADES, Tono.MENTA, "15:30", "Maestra Ana", "Clase de música con panderos y maracas."),
    Foto(7, "Ayer", "lun 28 sep", "Lectura", Categoria.ACTIVIDADES, Tono.MENTA, "09:40", "Maestra Laura", "Cuento en círculo: \"El pollito curioso\"."),
    Foto(8, "Ayer", "lun 28 sep", "Pintura", Categoria.ACTIVIDADES, Tono.AMARILLO, "11:20", "Maestra Ana", "Pintura con dedos, colores primarios."),
    Foto(9, "Ayer", "lun 28 sep", "Desayuno", Categoria.COMIDAS, Tono.AMARILLO, "08:45", "Maestra Laura", "Desayunó avena y plátano."),
    Foto(10, "Ayer", "lun 28 sep", "Bloques", Categoria.ACTIVIDADES, Tono.LAVANDA, "12:10", "Maestra Sofía", "Construyó una torre de 8 bloques."),
    Foto(11, "Ayer", "lun 28 sep", "Siesta", Categoria.SIESTA, Tono.MENTA, "14:05", "Maestra Laura", "Durmió 1 h."),
)

// ---------- Pantalla ----------
@Composable
fun FotosScreen(
    nombreNino: String = "Mateo",
    fotos: List<Foto> = fotosDemo,
    onDescargar: (Foto) -> Unit = {},
    onPedirBorrar: (Foto) -> Unit = {},
) {
    var filtro by remember { mutableStateOf(Categoria.TODAS) }
    var menuAbierto by remember { mutableStateOf(false) }
    var visorIndex by remember { mutableStateOf<Int?>(null) }
    val borradoSolicitado = remember { mutableStateListOf<Int>() }

    val filtradas = remember(filtro, fotos) {
        fotos.filter { filtro == Categoria.TODAS || it.categoria == filtro }
    }
    val grupos = remember(filtradas) { filtradas.groupBy { it.dia to it.fecha } }

    Scaffold(
        containerColor = Bg,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        "ESTANCIA LA ORUGA · FAMILIA",
                        style = TextStyle(fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Muted),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Fotos de $nombreNino",
                        style = TextStyle(fontFamily = Poppins, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp, color = Ink),
                    )
                    Spacer(Modifier.height(14.dp))
                    MenuCategorias(
                        seleccionada = filtro,
                        abierto = menuAbierto,
                        conteo = { c -> if (c == Categoria.TODAS) fotos.size else fotos.count { it.categoria == c } },
                        onToggle = { menuAbierto = !menuAbierto },
                        onElegir = { filtro = it; menuAbierto = false },
                    )
                    Spacer(Modifier.height(18.dp))
                }
            }

            grupos.forEach { (clave, lista) ->
                item(span = { GridItemSpan(maxLineSpan) }, key = "h-${clave.first}") {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(clave.first, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(clave.second, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Muted)
                    }
                }
                items(lista, key = { it.id }) { foto ->
                    Miniatura(foto) { visorIndex = filtradas.indexOf(foto) }
                }
            }
        }
    }

    visorIndex?.let { i ->
        val foto = filtradas[i]
        VisorFoto(
            foto = foto,
            posicion = "${i + 1} de ${filtradas.size}",
            borradoSolicitado = foto.id in borradoSolicitado,
            onCerrar = { visorIndex = null },
            onAnterior = { visorIndex = (i - 1 + filtradas.size) % filtradas.size },
            onSiguiente = { visorIndex = (i + 1) % filtradas.size },
            onDescargar = { onDescargar(foto) },
            onPedirBorrar = {
                if (foto.id !in borradoSolicitado) {
                    borradoSolicitado.add(foto.id)
                    onPedirBorrar(foto)
                }
            },
        )
    }
}

// ---------- Menú desplegable (acordeón) ----------
@Composable
private fun MenuCategorias(
    seleccionada: Categoria,
    abierto: Boolean,
    conteo: (Categoria) -> Int,
    onToggle: () -> Unit,
    onElegir: (Categoria) -> Unit,
) {
    val rot by animateFloatAsState(if (abierto) 180f else 0f, label = "chevron")
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, Line, RoundedCornerShape(16.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().height(52.dp).clickable(onClick = onToggle).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                seleccionada.label,
                modifier = Modifier.weight(1f),
                fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink,
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Ink, modifier = Modifier.rotate(rot))
        }
        AnimatedVisibility(visible = abierto, enter = expandVertically(), exit = shrinkVertically()) {
            Column(
                Modifier.fillMaxWidth().drawBehind {
                    drawLine(Color(0xFFF1EDF7), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }.padding(6.dp)
            ) {
                Categoria.entries.forEach { c ->
                    val on = c == seleccionada
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (on) SelectedRow else Color.Transparent)
                            .clickable { onElegir(c) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(c.dot))
                        Text(
                            c.label, Modifier.weight(1f),
                            fontFamily = Poppins, fontSize = 14.sp,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, color = Ink,
                        )
                        Text("${conteo(c)}", fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Muted)
                    }
                }
            }
        }
    }
}

// ---------- Miniatura ----------
@Composable
private fun Miniatura(foto: Foto, onClick: () -> Unit) {
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .rayado(foto.tono)
            .clickable(onClick = onClick)
    ) {
        // TODO: AsyncImage(model = foto.url, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        Etiqueta(foto.etiqueta, Modifier.align(Alignment.BottomStart).padding(6.dp))
        if (foto.esIncidente) {
            Text(
                "Incidente",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Incident)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                fontFamily = Poppins, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White,
            )
        }
    }
}

@Composable
private fun Etiqueta(texto: String, modifier: Modifier = Modifier, grande: Boolean = false) {
    Text(
        texto,
        modifier = modifier
            .clip(RoundedCornerShape(if (grande) 8.dp else 5.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = if (grande) 10.dp else 5.dp, vertical = if (grande) 5.dp else 2.dp),
        fontFamily = FontFamily.Monospace,
        fontSize = if (grande) 12.sp else 9.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF5D5578),
    )
}

// Placeholder rayado diagonal
private fun Modifier.rayado(tono: Tono): Modifier = drawBehind {
    drawRect(tono.a)
    val paso = 12.dp.toPx()
    val ancho = paso * 1.414f / 2f
    var x = -size.height
    while (x < size.width + size.height) {
        drawLine(tono.b, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = ancho)
        x += paso * 2 * 0.707f * 2
    }
}

// ---------- Visor ----------
@Composable
private fun VisorFoto(
    foto: Foto,
    posicion: String,
    borradoSolicitado: Boolean,
    onCerrar: () -> Unit,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onDescargar: () -> Unit,
    onPedirBorrar: () -> Unit,
) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(ViewerBg).systemBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotonRedondo(onCerrar, Color.White.copy(alpha = 0.12f)) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                }
                Text(
                    posicion, Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(Modifier.size(40.dp))
            }
            Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(20.dp)).rayado(foto.tono),
                contentAlignment = Alignment.Center,
            ) {
                Etiqueta("foto · ${foto.etiqueta}", grande = true)
                BotonRedondo(onAnterior, Color.White.copy(alpha = 0.8f), Modifier.align(Alignment.CenterStart).padding(start = 10.dp)) {
                    Icon(Icons.Default.ChevronLeft, "Anterior", tint = Ink)
                }
                BotonRedondo(onSiguiente, Color.White.copy(alpha = 0.8f), Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) {
                    Icon(Icons.Default.ChevronRight, "Siguiente", tint = Ink)
                }
            }
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(Bg)
                    .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE6E0FB)), contentAlignment = Alignment.Center) {
                        Text(foto.maestra.split(" ").last().take(1), fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF6A5BB5))
                    }
                    Column {
                        Text(foto.maestra, fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("${foto.dia} · ${foto.hora}", fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Muted)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(foto.nota, fontFamily = Poppins, fontSize = 14.sp, lineHeight = 21.sp, color = Body)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDescargar,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
                    ) { Text("Descargar", fontFamily = Poppins, fontWeight = FontWeight.SemiBold) }
                    OutlinedButton(
                        onClick = onPedirBorrar,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE4DEF2)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Ink),
                    ) {
                        Text(
                            if (borradoSolicitado) "Solicitud enviada" else "Pedir que se borre",
                            fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonRedondo(onClick: () -> Unit, fondo: Color, modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Box(
        modifier.size(40.dp).clip(CircleShape).background(fondo).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { contenido() }
}

@Preview(showBackground = true)
@Composable
private fun FotosScreenPreview() {
    CodeaTheme {
        FotosScreen()
    }
}
