package mx.tec.codea.data

import mx.tec.codea.domain.Padre

class PadreRepository {

    private val padres = listOf(
        Padre(
            id = "padre-1",
            nombre = "Laura Iglesias",
            iniciales = "LI",
            parentesco = "Mamá",
            hijoIds = listOf("infante-1"),
            telefono = "55 1234 5678",
            correo = "laura.iglesias@correo.com"
        ),
        Padre(
            id = "padre-2",
            nombre = "Carlos Gómez",
            iniciales = "CG",
            parentesco = "Papá",
            hijoIds = listOf("infante-2"),
            telefono = "55 2345 6789",
            correo = "carlos.gomez@correo.com"
        ),
        Padre(
            id = "padre-3",
            nombre = "Andrea Torres",
            iniciales = "AT",
            parentesco = "Mamá",
            hijoIds = listOf("infante-3"),
            telefono = "55 3456 7890",
            correo = "andrea.torres@correo.com"
        ),
        Padre(
            id = "padre-4",
            nombre = "Sofía Ruiz",
            iniciales = "SR",
            parentesco = "Tutora",
            hijoIds = listOf("infante-4"),
            telefono = "55 4567 8901",
            correo = "sofia.ruiz@correo.com"
        ),
        Padre(
            id = "padre-5",
            nombre = "Marisol Hernández",
            iniciales = "MH",
            parentesco = "Mamá",
            hijoIds = listOf("infante-5"),
            telefono = "55 5678 9012",
            correo = "marisol.hernandez@correo.com"
        )
    )

    fun getAll(): List<Padre> = padres

    fun getById(id: String): Padre? = padres.firstOrNull { it.id == id }
}
