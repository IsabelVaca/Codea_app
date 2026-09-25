package mx.tec.codea.model

import androidx.compose.ui.graphics.Color

data class Docente(
    val nombre: String,
    val iniciales: String,
    val sala: String,
    val ninosEnSala: Int,
    val horaChecada: String,
    val avatarBg: Color = Color(0xFFEDE9FE),
    val avatarFg: Color = Color(0xFF6D28D9)
)
