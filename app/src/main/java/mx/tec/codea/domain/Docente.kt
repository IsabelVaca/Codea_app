package mx.tec.codea.domain

// dominio puro, sin androidx. los colores van en ui/theme.
data class Docente(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val sala: String,
    val horaChecada: String,
    val baja: Boolean = false
)
