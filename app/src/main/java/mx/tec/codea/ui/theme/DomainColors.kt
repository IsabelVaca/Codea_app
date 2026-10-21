package mx.tec.codea.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// el dominio no maneja Color; los colores van aquí.

// lila con iniciales moradas; en oscuro, el mismo par del tema que usan los avatares de "Mi sala".
val AvatarBg: Color
    @Composable @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) VioletNight else Color(0xFFEDE9FE)
val AvatarFg: Color
    @Composable @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) VioletLight else Color(0xFF6D28D9)

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
