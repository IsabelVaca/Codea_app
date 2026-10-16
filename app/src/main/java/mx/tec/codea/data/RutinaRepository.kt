package mx.tec.codea.data

import mx.tec.codea.domain.MomentoCatalogo

// solo los catálogos de momentos que se ofrecen al armar una rutina.
// las rutinas en sí vienen de la API (RutinaApiRepository).
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
}
