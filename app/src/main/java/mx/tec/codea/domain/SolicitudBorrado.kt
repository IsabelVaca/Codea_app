package mx.tec.codea.domain

// una familia pidió que se borre una foto y el administrador todavía no decide.
data class SolicitudBorrado(
    val id: String,
    // quién la pidió.
    val solicitante: String,
    // los niños que salen en la foto.
    val ninos: List<String>,
    val motivo: String?,
    // URL firmada de 10 minutos para verla antes de decidir.
    val fotoUrl: String?,
    val fecha: String,
)
