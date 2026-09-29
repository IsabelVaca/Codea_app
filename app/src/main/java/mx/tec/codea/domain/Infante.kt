package mx.tec.codea.domain

data class Infante(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val edad: Int,
    val tutor: String,
    val tituloTutor: String,
    val sala: String,
    val baja: Boolean = false
)
