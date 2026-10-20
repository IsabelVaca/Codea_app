package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.DocenteApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Docente

class DocentesAdminViewModel : ViewModel() {

    private val repository = DocenteApiRepository()

    private val _docentes = MutableStateFlow<List<Docente>>(emptyList())
    val docentes: StateFlow<List<Docente>> = _docentes.asStateFlow()

    // true solo mientras dura un "jalar para recargar"; las recargas de fondo (al abrir la pestaña) no lo encienden.
    // va antes del init: el init llama a cargar(), que lo usa.
    var cargando by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    // se llama también al abrir la pestaña Docentes: la checada de una docente se hace desde su teléfono
    // y aquí no llega ningún DataEvents, así que sin esto la hora no aparecía hasta reiniciar la app.
    fun cargar(conIndicador: Boolean = false) {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            // lo guardado (ApiCache) sale al instante; lo de la API lo reemplaza al llegar.
            if (_docentes.value.isEmpty()) repository.cached()?.let { _docentes.value = it }
            if (conIndicador) cargando = true
            try {
                _docentes.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar las docentes: ${e.toUserMessage()}")
            } finally {
                cargando = false
            }
        }
    }

    // busca en el estado actual, no en el repository fijo.
    fun getDocente(id: String): Docente? = _docentes.value.firstOrNull { it.id == id }

    fun altaDocente(
        nombre: String,
        sala: String,
        apellido: String = "",
        telefono: String = "",
        correo: String = "",
        onSuccess: () -> Unit = {},
    ) {
        launchAction(onSuccess) { repository.alta(nombre, apellido, telefono, correo, sala) }
    }

    fun cambiarSala(docenteId: String, nuevaSala: String) {
        launchAction { repository.cambiarSala(docenteId, nuevaSala) }
    }

    // alterna: si está de baja la reactiva, si no la da de baja.
    fun darDeBaja(docenteId: String) {
        val actual = getDocente(docenteId) ?: return
        launchAction { repository.setBaja(docenteId, baja = !actual.baja) }
    }
}
