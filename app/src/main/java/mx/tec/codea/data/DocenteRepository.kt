package mx.tec.codea.data

import mx.tec.codea.domain.Docente

// lista fija por ahora; luego getAll()/getById() llaman a la API.
class DocenteRepository {

    private val docentes = listOf(
        Docente(
            id = "docente-1",
            nombre = "Paola Sánchez",
            iniciales = "PS",
            sala = "Preescolar 2",
            horaChecada = "7:52"
        ),
        Docente(
            id = "docente-2",
            nombre = "Fernando Ríos",
            iniciales = "FR",
            sala = "Maternal",
            horaChecada = "7:45"
        ),
        Docente(
            id = "docente-3",
            nombre = "Gabriela Ortiz",
            iniciales = "GO",
            sala = "Preescolar 1",
            horaChecada = "8:03"
        ),
        Docente(
            id = "docente-4",
            nombre = "Luis Medina",
            iniciales = "LM",
            sala = "Preescolar 2",
            horaChecada = "7:58"
        ),
        Docente(
            id = "docente-5",
            nombre = "Karla Vega",
            iniciales = "KV",
            sala = "Maternal",
            horaChecada = "7:50"
        ),
        Docente(
            id = "docente-6",
            nombre = "Andrés Cano",
            iniciales = "AC",
            sala = "Preescolar 1",
            horaChecada = "8:10"
        )
    )

    fun getAll(): List<Docente> = docentes

    fun getById(id: String): Docente? = docentes.firstOrNull { it.id == id }
}
