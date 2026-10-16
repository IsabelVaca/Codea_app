package mx.tec.codea.domain

data class RutinaItem(
    val id: String,
    val nombre: String,
    val cantidadMomentos: Int,
    // la que usan las salas que no tienen una propia.
    val esPredeterminada: Boolean = false,
    // ids de las salas que la tienen asignada.
    val salasIds: List<String> = emptyList(),
)
