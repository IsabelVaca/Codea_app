package mx.tec.codea.ui.screens.parent.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.components.imagenEstable
import mx.tec.codea.ui.screens.parent.PadreColores
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// ---------- Modelo ----------
enum class Categoria(val label: String) {
    TODAS("Todas"),
    ACTIVIDADES("Actividades"),
    COMIDAS("Comidas"),
    SIESTA("Siesta"),
    INCIDENTES("Incidentes"),
}

data class Foto(
    val id: Int,
    val dia: String,
    val fecha: String,
    val etiqueta: String,
    val categoria: Categoria,
    val hora: String,
    val maestra: String,
    val nota: String,
    val url: String? = null,
) {
    val esIncidente get() = categoria == Categoria.INCIDENTES
}

// el fondo del visor: casi negro, para que la foto resalte.
private val FondoVisor = Color(0xFF1D1830)

// ---------- Pantalla ----------
// las fotos del niño, con el estilo del admin: chips para filtrar, miniaturas en cuadrícula y el visor completo.
@Composable
fun ParentPhotosScreen(
    nombreNino: String = "",
    fotos: List<Foto> = emptyList(),
    onDescargar: (Foto) -> Unit = {},
    onBorrar: (Foto) -> Unit = {},
) {
    var filtro by remember { mutableStateOf(Categoria.TODAS) }
    var visorIndex by remember { mutableStateOf<Int?>(null) }
    var confirmarBorrado by remember { mutableStateOf<Foto?>(null) }

    val filtradas = remember(filtro, fotos) {
        fotos.filter { filtro == Categoria.TODAS || it.categoria == filtro }
    }
    val grupos = remember(filtradas) { filtradas.groupBy { it.dia to it.fecha } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ScreenHeader(title = if (nombreNino.isBlank()) "Fotos" else "Fotos de $nombreNino")
                if (fotos.isNotEmpty()) {
                    FiltroCategorias(
                        fotos = fotos,
                        elegida = filtro,
                        onElegir = { filtro = it },
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
        }

        // sin fotos (todavía no hay, o la categoría elegida no tiene) la pantalla lo dice en vez de quedar vacía.
        if (filtradas.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (fotos.isEmpty()) "Todavía no hay fotos de $nombreNino. Cuando la maestra suba una, la verás aquí."
                    else "No hay fotos en esta categoría.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp, color = PadreColores.Gris,
                )
            }
        }

        grupos.forEach { (clave, lista) ->
            item(span = { GridItemSpan(maxLineSpan) }, key = "h-${clave.first}-${clave.second}") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(clave.first, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PadreColores.Ink)
                    Text(clave.second, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PadreColores.GrisTexto)
                }
            }
            items(lista, key = { it.id }) { foto ->
                Miniatura(foto) { visorIndex = filtradas.indexOf(foto) }
            }
        }
    }

    visorIndex?.let { i ->
        val foto = filtradas.getOrNull(i) ?: return@let
        VisorFoto(
            foto = foto,
            posicion = "${i + 1} de ${filtradas.size}",
            onCerrar = { visorIndex = null },
            onAnterior = { visorIndex = (i - 1 + filtradas.size) % filtradas.size },
            onSiguiente = { visorIndex = (i + 1) % filtradas.size },
            onDescargar = { onDescargar(foto) },
            onBorrar = { confirmarBorrado = foto },
        )
    }

    confirmarBorrado?.let { foto ->
        AlertDialog(
            onDismissRequest = { confirmarBorrado = null },
            title = { Text("¿Borrar esta foto?", fontFamily = Poppins, fontWeight = FontWeight.ExtraBold) },
            text = { Text("Se borra para siempre y ya no la verá nadie, ni tú ni la maestra. No se puede deshacer.", fontFamily = Poppins) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = null
                    visorIndex = null
                    onBorrar(foto)
                }) { Text("Borrar", fontFamily = Poppins, fontWeight = FontWeight.Bold, color = PadreColores.Rojo) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = null }) {
                    Text("Cancelar", fontFamily = Poppins, fontWeight = FontWeight.Bold, color = PadreColores.GrisTexto)
                }
            },
            containerColor = Color.White,
        )
    }
}

// ---------- Filtro (como «Activos | Todos» del admin) ----------
// solo salen las categorías que tienen fotos, con su número.
@Composable
private fun FiltroCategorias(fotos: List<Foto>, elegida: Categoria, onElegir: (Categoria) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Categoria.entries.forEach { c ->
            val n = if (c == Categoria.TODAS) fotos.size else fotos.count { it.categoria == c }
            if (n > 0 || c == elegida) {
                FilterChip(
                    selected = c == elegida,
                    onClick = { onElegir(c) },
                    label = { Text("${c.label} · $n", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PadreColores.Lila,
                        selectedLabelColor = PadreColores.Morado,
                    ),
                )
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
            .clip(RoundedCornerShape(14.dp))
            .background(PadreColores.Lila)
            .clickable(onClick = onClick)
    ) {
        if (foto.url != null) {
            AsyncImage(
                model = imagenEstable(foto.url),
                contentDescription = foto.etiqueta,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
        Text(
            foto.etiqueta,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 7.dp, vertical = 2.dp),
            fontFamily = Poppins, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PadreColores.Morado,
        )
        if (foto.esIncidente) {
            Text(
                "Incidente",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PadreColores.Rojo)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                fontFamily = Poppins, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
            )
        }
    }
}

// ---------- Visor ----------
@Composable
private fun VisorFoto(
    foto: Foto,
    posicion: String,
    onCerrar: () -> Unit,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onDescargar: () -> Unit,
    onBorrar: () -> Unit,
) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(FondoVisor).systemBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotonRedondo(onCerrar, Color.White.copy(alpha = 0.12f)) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                }
                Text(
                    posicion, Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(Modifier.size(40.dp))
            }
            Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center,
            ) {
                if (foto.url != null) {
                    AsyncImage(
                        model = imagenEstable(foto.url),
                        contentDescription = foto.etiqueta,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.matchParentSize(),
                    )
                }
                BotonRedondo(onAnterior, Color.White.copy(alpha = 0.85f), Modifier.align(Alignment.CenterStart).padding(start = 10.dp)) {
                    Icon(Icons.Default.ChevronLeft, "Anterior", tint = PadreColores.Ink)
                }
                BotonRedondo(onSiguiente, Color.White.copy(alpha = 0.85f), Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) {
                    Icon(Icons.Default.ChevronRight, "Siguiente", tint = PadreColores.Ink)
                }
            }
            Spacer(Modifier.height(14.dp))
            // la hoja de abajo: quién la subió, cuándo, su nota y las acciones.
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(Color.White)
                    .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(PadreColores.Lila),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(foto.maestra.trim().take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PadreColores.Morado)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(foto.maestra, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PadreColores.Ink)
                        Text("${foto.dia} · ${foto.hora}", fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PadreColores.GrisTexto)
                    }
                    Text(
                        foto.etiqueta,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(if (foto.esIncidente) PadreColores.RojoSuave else PadreColores.Lila)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (foto.esIncidente) PadreColores.Rojo else PadreColores.Morado,
                    )
                }
                if (foto.nota.isNotBlank()) {
                    Text(foto.nota, fontFamily = Poppins, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium, color = PadreColores.Ink)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDescargar,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PadreColores.Violeta, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) { Text("Descargar", fontFamily = Poppins, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = onBorrar,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PadreColores.RojoSuave, contentColor = PadreColores.Rojo),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) { Text("Borrar", fontFamily = Poppins, fontWeight = FontWeight.Bold) }
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
private fun ParentPhotosScreenPreview() {
    CodeaTheme {
        ParentPhotosScreen(nombreNino = "Mateo")
    }
}
