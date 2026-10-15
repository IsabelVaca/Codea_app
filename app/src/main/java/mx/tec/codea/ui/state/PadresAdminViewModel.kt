package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.PadreApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Padre

class PadresAdminViewModel : ViewModel() {

    private val repository = PadreApiRepository()

    private val _padres = MutableStateFlow<List<Padre>>(emptyList())
    val padres: StateFlow<List<Padre>> = _padres.asStateFlow()

    init {
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                _padres.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar los padres: ${e.toUserMessage()}")
            }
        }
    }

    fun getPadre(id: String): Padre? = _padres.value.firstOrNull { it.id == id }

    // alterna: con acceso -> lo quita; sin acceso -> lo devuelve.
    fun toggleAcceso(padreId: String) {
        val actual = getPadre(padreId) ?: return
        launchAction { repository.setAcceso(padreId, conAcceso = !actual.conAcceso) }
    }

    fun inscribirPadre(
        nombre: String,
        telefono: String,
        correo: String,
        parentesco: String,
        hijoIds: List<String>,
        apellido: String = "",
        onSuccess: () -> Unit = {},
    ) {
        launchAction(onSuccess) { repository.inscribir(nombre, apellido, telefono, correo, parentesco, hijoIds) }
    }

    fun reenviarInvitacion(padreId: String, onSuccess: () -> Unit = {}) {
        launchAction(onSuccess) { repository.reenviarInvitacion(padreId) }
    }
}
