package mx.tec.codea.data

import mx.tec.codea.domain.Infante

class InfanteRepository {

    private val infantes = listOf(
        Infante(
            id = "infante-1",
            nombre = "Mateo Iglesias",
            iniciales = "MI",
            edad = 4,
            sala = "Preescolar 2"
        ),
        Infante(
            id = "infante-2",
            nombre = "Renata Gómez",
            iniciales = "RG",
            edad = 3,
            sala = "Maternal"
        ),
        Infante(
            id = "infante-3",
            nombre = "Emiliano Torres",
            iniciales = "ET",
            edad = 5,
            sala = "Preescolar 1"
        ),
        Infante(
            id = "infante-4",
            nombre = "Valentina Ruiz",
            iniciales = "VR",
            edad = 4,
            sala = "Preescolar 2"
        ),
        Infante(
            id = "infante-5",
            nombre = "Diego Hernández",
            iniciales = "DH",
            edad = 2,
            sala = "Maternal"
        ),
        Infante(
            id = "infante-6",
            nombre = "Camila Flores",
            iniciales = "CF",
            edad = 5,
            sala = "Preescolar 1"
        )
    )

    fun getAll(): List<Infante> = infantes

    fun getById(id: String): Infante? = infantes.firstOrNull { it.id == id }
}
