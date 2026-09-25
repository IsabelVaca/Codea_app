package mx.tec.codea.data

import androidx.compose.ui.graphics.Color
import mx.tec.codea.model.Sala

val fakeSalas = listOf(
    Sala(
        nombre = "Maternal",
        cupoTexto = "8/10 niños",
        ocupacion = 0.8f,
        asistente = "Fernando Ríos",
        color = Color(0xFF5B3FE0)
    ),
    Sala(
        nombre = "Preescolar 1",
        cupoTexto = "10/12 niños",
        ocupacion = 0.83f,
        asistente = "Gabriela Ortiz",
        color = Color(0xFF0F9D8A)
    ),
    Sala(
        nombre = "Preescolar 2",
        cupoTexto = "12/12 niños",
        ocupacion = 1f,
        asistente = "Paola Sánchez",
        color = Color(0xFFC4432A)
    )
)
