package mx.tec.codea.data

import mx.tec.codea.domain.Sala

class SalaRepository {

    private val salas = listOf(
        Sala(
            id = "sala-1",
            nombre = "Maternal",
            cupoMaximo = 10,
            asistente = "Fernando Ríos"
        ),
        Sala(
            id = "sala-2",
            nombre = "Preescolar 1",
            cupoMaximo = 12,
            asistente = "Gabriela Ortiz"
        ),
        Sala(
            id = "sala-3",
            nombre = "Preescolar 2",
            cupoMaximo = 12,
            asistente = "Paola Sánchez"
        )
    )

    fun getAll(): List<Sala> = salas

    fun getById(id: String): Sala? = salas.firstOrNull { it.id == id }
}
