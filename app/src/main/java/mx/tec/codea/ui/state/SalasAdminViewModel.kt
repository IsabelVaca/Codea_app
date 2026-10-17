package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.SalaApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Sala

class SalasAdminViewModel : ViewModel() {

    private val repository = SalaApiRepository()

    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas: StateFlow<List<Sala>> = _salas.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // cada vez que algo cambia en el servidor, se vuelve a pedir la lista.
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                _salas.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.toUserMessage()
                DataEvents.notice("No se pudieron cargar las salas: ${e.toUserMessage()}")
            } finally {
                _cargando.value = false
            }
        }
    }

    // busca en el estado actual, no en el repository fijo.
    fun getSala(id: String): Sala? = _salas.value.firstOrNull { it.id == id }

    fun crearSala(nombre: String, cupoMaximo: Int, asistente: String?, onSuccess: () -> Unit = {}) {
        launchAction(onSuccess) { repository.crear(nombre, cupoMaximo, asistente) }
    }

    fun editarCupo(salaId: String, nuevoCupoMaximo: Int) {
        launchAction { repository.editarCupo(salaId, nuevoCupoMaximo) }
    }

    fun asignarAsistente(salaId: String, asistente: String) {
        launchAction { repository.asignarAsistente(salaId, asistente) }
    }

    fun eliminarSala(salaId: String, onSuccess: () -> Unit = {}) {
        launchAction(onSuccess) { repository.eliminar(salaId) }
    }
}
