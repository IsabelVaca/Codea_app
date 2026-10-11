package mx.tec.codea.data

import mx.tec.codea.domain.Padre

class PadreRepository {

    private val padres = listOf(
        Padre(
            id = "padre-1",
            nombre = "Laura Iglesias",
            iniciales = "LI",
            parentesco = "Mamá",
            hijo = "Mateo Iglesias",
            telefono = "55 1234 5678",
            correo = "laura.iglesias@correo.com"
        ),
        Padre(
            id = "padre-2",
            nombre = "Jorge Solís",
            iniciales = "JS",
            parentesco = "Papá",
            hijo = "Renata Solís",
            telefono = "55 2345 6789",
            correo = "jorge.solis@correo.com"
        ),
        Padre(
            id = "padre-3",
            nombre = "Paula Ruiz",
            iniciales = "PR",
            parentesco = "Mamá",
            hijo = "Emiliano Ruiz",
            telefono = "55 3456 7890",
            correo = "paula.ruiz@correo.com"
        ),
        Padre(
            id = "padre-4",
            nombre = "Ana Fuentes",
            iniciales = "AF",
            parentesco = "Mamá",
            hijo = "Diego Fuentes",
            telefono = "55 4567 8901",
            correo = "ana.fuentes@correo.com"
        ),
        Padre(
            id = "padre-5",
            nombre = "Marco Peña",
            iniciales = "MP",
            parentesco = "Papá",
            hijo = "Aitana Peña",
            telefono = "55 5678 9012",
            correo = "marco.pena@correo.com"
        )
    )

    fun getAll(): List<Padre> = padres

    fun getById(id: String): Padre? = padres.firstOrNull { it.id == id }
}
