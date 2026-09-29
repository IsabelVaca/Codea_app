package mx.tec.codea.ui.theme

import androidx.compose.ui.graphics.Color

// el dominio no maneja Color; los colores van aquí.

val AvatarBg = Color(0xFFEDE9FE)
val AvatarFg = Color(0xFF6D28D9)

private val salaPalette = listOf(
    Color(0xFF5B3FE0),
    Color(0xFF0F9D8A),
    Color(0xFFC4432A),
)

// color estable por id de sala.
fun salaColor(id: String): Color {
    val index = (id.hashCode() % salaPalette.size).let { if (it < 0) it + salaPalette.size else it }
    return salaPalette[index]
}
