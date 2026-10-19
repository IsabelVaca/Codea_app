package mx.tec.codea.domain

// alguien que el admin registró para recoger a un niño. parentesco y teléfono pueden quedar vacíos.
data class PersonaAutorizada(
    val id: String,
    val nombre: String,
    val parentesco: String = "",
    val telefono: String = "",
)

object PersonaAutorizadaRules {

    // el nombre es lo único obligatorio (la API responde 422 NAME_REQUIRED sin él).
    fun puedeAgregar(nombre: String): Boolean = nombre.isNotBlank()

    // quién va primero en la lista: por nombre, sin importar mayúsculas.
    fun ordenadas(personas: List<PersonaAutorizada>): List<PersonaAutorizada> =
        personas.sortedBy { it.nombre.lowercase() }
}
