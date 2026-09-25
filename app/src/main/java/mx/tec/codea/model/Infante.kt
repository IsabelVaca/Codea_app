package mx.tec.codea.model

import androidx.compose.ui.graphics.Color

data class Infante(
    val nombre: String,
    val iniciales: String,
    val edad: Int,
    val tutor: String,
    val tituloTutor: String,
    val sala: String,
    val avatarBg: Color = Color(0xFFEDE9FE),
    val avatarFg: Color = Color(0xFF6D28D9),
    val baja: Boolean = false
)
