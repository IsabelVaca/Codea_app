package mx.tec.codea.domain

// dominio puro, sin androidx. los colores van en ui/theme.
data class Docente(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val sala: String,
    // la hora en que checó hoy ("7:52"); null si todavía no checa.
    val horaChecada: String?,
    val baja: Boolean = false,
    // para reenviarle la invitación por WhatsApp.
    val telefono: String? = null,
    val correo: String? = null,
    // ya entró a la app con su invitación (si no, el código que se le mande es para crear su cuenta).
    val tieneCuenta: Boolean = false,
)
