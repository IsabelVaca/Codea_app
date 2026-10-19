package mx.tec.codea.domain

// uno de los hijos de la familia, para el selector de las pestañas del padre.
data class ChildOption(val id: Int, val firstName: String, val fullName: String)

object ChildChoice {

    // el hijo que se queda seleccionado cuando la lista se vuelve a cargar: el mismo de antes si sigue en la lista
    // (la lista se recarga con cada cambio y no debe regresar a la familia al primero); si no, el primero.
    fun pick(options: List<ChildOption>, current: Int?): ChildOption? =
        options.firstOrNull { it.id == current } ?: options.firstOrNull()

    // con un solo hijo no hay nada que elegir.
    fun needsSelector(options: List<ChildOption>): Boolean = options.size > 1
}
