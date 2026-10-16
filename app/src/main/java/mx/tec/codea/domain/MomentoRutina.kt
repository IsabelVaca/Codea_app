package mx.tec.codea.domain

data class MomentoRutina(
    val id: String,
    val nombre: String,
    val registro: String,
    // la que manda a la API: ARRIVAL, MEAL, ACTIVITY, NAP, HYGIENE o PICKUP.
    val categoria: String = categoriaDeNombre(nombre),
)
