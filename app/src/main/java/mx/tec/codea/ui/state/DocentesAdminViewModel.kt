package mx.tec.codea.ui.state

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

    init {
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                _docentes.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar las docentes: ${e.toUserMessage()}")
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
        turno: String = "",
        onSuccess: () -> Unit = {},
    ) {
        launchAction(onSuccess) { repository.alta(nombre, apellido, telefono, correo, sala, turno) }
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
