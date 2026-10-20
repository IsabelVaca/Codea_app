package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.MenuBody
import mx.tec.codea.data.api.MenuDto
import mx.tec.codea.domain.MenuDelDia

// el menú del día: el admin lo captura y la docente y las familias lo ven.
class MenuApiRepository {

    // los menús capturados entre dos fechas, por fecha. los días sin menú no aparecen.
    suspend fun entre(desde: String, hasta: String): Map<String, MenuDelDia> =
        ApiClient.api.menus(desde, hasta).associate { it.date to it.toMenu() }

    // el de hoy; null si todavía no lo capturan.
    suspend fun hoy(): MenuDelDia? = ApiClient.api.menus(null, null).firstOrNull()?.toMenu()

    // crea o reemplaza el menú de ese día. una comida en blanco se manda como vacía.
    suspend fun guardar(fecha: String, menu: MenuDelDia): MenuDelDia =
        ApiClient.api.saveMenu(
            fecha,
            MenuBody(
                breakfast = menu.desayuno.trim().ifBlank { null },
                lunch = menu.comida.trim().ifBlank { null },
                snack = menu.colacion.trim().ifBlank { null },
            ),
        ).toMenu()

    suspend fun quitar(fecha: String) {
        ApiClient.api.deleteMenu(fecha)
    }
}

fun MenuDto.toMenu() = MenuDelDia(desayuno = breakfast.orEmpty(), comida = lunch.orEmpty(), colacion = snack.orEmpty())
