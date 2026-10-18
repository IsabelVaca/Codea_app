package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.InfanteApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Infante

class InfantesAdminViewModel : ViewModel() {

    private val repository = InfanteApiRepository()

    private val _infantes = MutableStateFlow<List<Infante>>(emptyList())
    val infantes: StateFlow<List<Infante>> = _infantes.asStateFlow()

    init {
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                _infantes.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar los infantes: ${e.toUserMessage()}")
            }
        }
    }

    // busca en el estado actual, no en el repository fijo.
    fun getInfante(id: String): Infante? = _infantes.value.firstOrNull { it.id == id }

    // nuevaSala vacía = sacar de la sala.
    fun cambiarSala(infanteId: String, nuevaSala: String) {
        launchAction { repository.cambiarSala(infanteId, nuevaSala) }
    }

    // alterna: si está de baja lo reactiva, si no lo da de baja.
    fun darDeBaja(infanteId: String) {
        val actual = getInfante(infanteId) ?: return
        launchAction { repository.setBaja(infanteId, baja = !actual.baja) }
    }

    fun inscribirInfante(
        nombre: String,
        // la fecha de nacimiento real, "aaaa-mm-dd".
        nacimiento: String,
        sala: String,
        alergias: String = "",
        apellido: String = "",
        onSuccess: () -> Unit = {},
    ) {
        launchAction(onSuccess) { repository.inscribir(nombre, apellido, nacimiento, sala, alergias) }
    }
}
