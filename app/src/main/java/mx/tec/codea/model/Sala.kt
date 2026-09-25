package mx.tec.codea.model

import androidx.compose.ui.graphics.Color

data class Sala(
    val nombre: String,
    val cupoTexto: String,
    val ocupacion: Float,
    val asistente: String,
    val color: Color
)
