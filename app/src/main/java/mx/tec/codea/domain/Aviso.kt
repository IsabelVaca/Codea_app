package mx.tec.codea.domain

// un aviso del historial. destino = nombre de la sala; null = todas las salas.
data class Aviso(
    val id: String,
    val destino: String?,
    val fecha: String,
    val texto: String,
    // URL de la imagen del aviso, si trae.
    val imagenUrl: String? = null,
)
