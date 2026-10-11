package mx.tec.codea.domain

data class Padre(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val parentesco: String,
    val hijo: String,
    val telefono: String,
    val correo: String,
    val conAcceso: Boolean = true
)
