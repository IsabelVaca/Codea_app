package mx.tec.codea.data

import mx.tec.codea.domain.MomentoCatalogo
import mx.tec.codea.domain.MomentoRutina
import mx.tec.codea.domain.RutinaItem

class RutinaRepository {

    private val catalogo = listOf(
        MomentoCatalogo("Llegada y bienvenida", "Hora de llegada"),
        MomentoCatalogo("Desayuno", "Qué comió"),
        MomentoCatalogo("Higiene / cambio de pañal", "Hora"),
        MomentoCatalogo("Actividad guiada", "Foto + nota"),
        MomentoCatalogo("Recreo", "Hora"),
        MomentoCatalogo("Almuerzo", "Qué comió"),
        MomentoCatalogo("Siesta", "Hora inicio y fin"),
        MomentoCatalogo("Actividad libre", "Foto"),
        MomentoCatalogo("Merienda", "Qué comió"),
        MomentoCatalogo("Salida", "Quién lo recogió"),
    )

    fun getCatalogo(): List<MomentoCatalogo> = catalogo

    // catálogo que se ofrece al crear una rutina nueva desde cero.
    private val catalogoNuevaRutina = listOf(
        MomentoCatalogo("Bienvenida y filtro", "Asistencia · temperatura"),
        MomentoCatalogo("Desayuno", "Cuánto comió cada uno"),
        MomentoCatalogo("Comida", "Confirmación grupal"),
        MomentoCatalogo("Siesta", "Duración por niño"),
        MomentoCatalogo("Cambio de pañal", "Bitácora por evento"),
        MomentoCatalogo("Actividad dirigida", "Foto + nota"),
        MomentoCatalogo("Patio", "Juego libre supervisado"),
        MomentoCatalogo("Salida", "Entrega solo a autorizados"),
    )

    fun getCatalogoNuevaRutina(): List<MomentoCatalogo> = catalogoNuevaRutina

    fun getRutinasIniciales(): List<RutinaItem> = listOf(
        RutinaItem("preescolar", "Día completo · Preescolar", 9),
        RutinaItem("lactantes", "Día completo · Lactantes y maternal", 9),
        RutinaItem("lluvia", "Día de lluvia · sin patio", 8),
        RutinaItem("evento", "Día de evento o festival", 7),
    )

    fun getMomentosIniciales(): Map<String, List<MomentoRutina>> = mapOf(
        "preescolar" to momentosDe(
            "preescolar",
            "Llegada y bienvenida", "Desayuno", "Actividad guiada", "Recreo",
            "Almuerzo", "Siesta", "Actividad libre", "Merienda", "Salida"
        ),
        "lactantes" to momentosDe(
            "lactantes",
            "Llegada y bienvenida", "Desayuno", "Higiene / cambio de pañal", "Actividad guiada",
            "Almuerzo", "Siesta", "Actividad libre", "Merienda", "Salida"
        ),
        "lluvia" to momentosDe(
            "lluvia",
            "Llegada y bienvenida", "Desayuno", "Actividad guiada",
            "Almuerzo", "Siesta", "Actividad libre", "Merienda", "Salida"
        ),
        "evento" to momentosDe(
            "evento",
            "Llegada y bienvenida", "Desayuno", "Actividad guiada",
            "Almuerzo", "Actividad libre", "Merienda", "Salida"
        ),
    )

    private fun momentosDe(rutinaId: String, vararg nombres: String): List<MomentoRutina> =
        nombres.mapIndexed { i, nombre ->
            val registro = catalogo.firstOrNull { it.nombre == nombre }?.registro.orEmpty()
            MomentoRutina(id = "$rutinaId-${i + 1}", nombre = nombre, registro = registro)
        }
}
