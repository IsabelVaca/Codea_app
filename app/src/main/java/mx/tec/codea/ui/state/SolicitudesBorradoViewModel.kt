package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.SolicitudBorradoApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.SolicitudBorrado

// vive en la pantalla de solicitudes de borrado del admin.
class SolicitudesBorradoViewModel : ViewModel() {

    private val repository = SolicitudBorradoApiRepository()

    private val _solicitudes = MutableStateFlow<List<SolicitudBorrado>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudBorrado>> = _solicitudes.asStateFlow()

    // para no mostrar "no hay solicitudes" mientras todavía se está pidiendo la lista.
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        // cada vez que algo cambia en el servidor (por ejemplo, una decisión), se vuelve a pedir.
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                _solicitudes.value = repository.pendientes()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar las solicitudes: ${e.toUserMessage()}")
            } finally {
                _cargando.value = false
            }
        }
    }

    // la foto se borra para siempre del servidor.
    fun aprobar(id: String, onSuccess: () -> Unit = {}) {
        launchAction(onSuccess) { repository.aprobar(id) }
    }

    fun rechazar(id: String, onSuccess: () -> Unit = {}) {
        launchAction(onSuccess) { repository.rechazar(id) }
    }
}
