package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.tec.codea.data.RutinaRepository
import mx.tec.codea.domain.MomentoCatalogo
import mx.tec.codea.domain.MomentoRutina
import mx.tec.codea.domain.RutinaItem

class RutinasAdminViewModel : ViewModel() {

    private val repository = RutinaRepository()

    private val _rutinas = MutableStateFlow(repository.getRutinasIniciales())
    val rutinas: StateFlow<List<RutinaItem>> = _rutinas.asStateFlow()

    private val _momentos = MutableStateFlow(repository.getMomentosIniciales())
    val momentos: StateFlow<Map<String, List<MomentoRutina>>> = _momentos.asStateFlow()

    private var contadorMomentos = 0
    private var contadorRutinas = 0

    fun crearRutina(nombre: String, momentos: List<MomentoRutina>): String {
        contadorRutinas += 1
        val id = "rutina-$contadorRutinas"
        _rutinas.value = _rutinas.value + RutinaItem(id = id, nombre = nombre, cantidadMomentos = momentos.size)
        _momentos.value = _momentos.value + (id to momentos)
        return id
    }

    fun momentosDe(rutinaId: String): List<MomentoRutina> = _momentos.value[rutinaId].orEmpty()

    fun catalogoDisponible(rutinaId: String): List<MomentoCatalogo> {
        val nombresActuales = momentosDe(rutinaId).map { it.nombre }.toSet()
        return repository.getCatalogo().filter { it.nombre !in nombresActuales }
    }

    fun reordenar(rutinaId: String, from: Int, to: Int) {
        val actuales = momentosDe(rutinaId)
        if (from !in actuales.indices || to !in actuales.indices) return
        val reordenados = actuales.toMutableList().apply { add(to, removeAt(from)) }
        _momentos.value = _momentos.value + (rutinaId to reordenados)
    }

    fun agregarMomento(rutinaId: String, nombre: String, registro: String) {
        if (nombre.isBlank()) return
        contadorMomentos += 1
        val nuevo = MomentoRutina(id = "momento-$contadorMomentos", nombre = nombre, registro = registro)
        val actuales = momentosDe(rutinaId) + nuevo
        _momentos.value = _momentos.value + (rutinaId to actuales)
        _rutinas.value = _rutinas.value.map {
            if (it.id == rutinaId) it.copy(cantidadMomentos = actuales.size) else it
        }
    }
}
