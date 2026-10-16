package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.RutinaApiRepository
import mx.tec.codea.data.RutinaRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.MomentoCatalogo
import mx.tec.codea.domain.MomentoRutina
import mx.tec.codea.domain.RutinaItem

class RutinasAdminViewModel : ViewModel() {

    // el catálogo de momentos que se ofrece al agregar uno (local); las rutinas vienen de la API.
    private val catalogo = RutinaRepository()
    private val repository = RutinaApiRepository()

    private val _rutinas = MutableStateFlow<List<RutinaItem>>(emptyList())
    val rutinas: StateFlow<List<RutinaItem>> = _rutinas.asStateFlow()

    // reordenar y agregar momentos se hacen aquí, en la app; la API se entera al darle "Guardar rutina".
    private val _momentos = MutableStateFlow<Map<String, List<MomentoRutina>>>(emptyMap())
    val momentos: StateFlow<Map<String, List<MomentoRutina>>> = _momentos.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private var contadorMomentos = 0

    init {
        // cada vez que algo cambia en el servidor, se vuelve a pedir la lista.
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    // vuelve a pedir las rutinas a la API (también descarta lo que no se guardó).
    fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            _cargando.value = true
            try {
                val (rutinas, momentos) = repository.getAll()
                _rutinas.value = rutinas
                _momentos.value = momentos
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar las rutinas: ${e.toUserMessage()}")
            } finally {
                _cargando.value = false
            }
        }
    }

    fun crearRutina(nombre: String, momentos: List<MomentoRutina>, onSuccess: () -> Unit = {}) {
        launchAction(onSuccess) { repository.crear(nombre, momentos) }
    }

    // manda a la API la rutina como está ahora (orden y momentos).
    fun guardar(rutinaId: String, onSuccess: () -> Unit = {}) {
        val rutina = _rutinas.value.firstOrNull { it.id == rutinaId } ?: return
        launchAction(onSuccess) { repository.guardar(rutina, momentosDe(rutinaId)) }
    }

    // asignar = false deja a la sala con la rutina predeterminada.
    fun asignarSala(rutinaId: String, salaId: String, asignar: Boolean) {
        launchAction { repository.asignarASala(rutinaId, salaId, asignar) }
    }

    fun momentosDe(rutinaId: String): List<MomentoRutina> = _momentos.value[rutinaId].orEmpty()

    fun catalogoDisponible(rutinaId: String): List<MomentoCatalogo> {
        val nombresActuales = momentosDe(rutinaId).map { it.nombre }.toSet()
        return catalogo.getCatalogo().filter { it.nombre !in nombresActuales }
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
        val nuevo = MomentoRutina(id = "nuevo-$contadorMomentos", nombre = nombre, registro = registro)
        val actuales = momentosDe(rutinaId) + nuevo
        _momentos.value = _momentos.value + (rutinaId to actuales)
        _rutinas.value = _rutinas.value.map {
            if (it.id == rutinaId) it.copy(cantidadMomentos = actuales.size) else it
        }
    }
}
