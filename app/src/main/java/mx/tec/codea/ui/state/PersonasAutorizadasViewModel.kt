package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.PersonaAutorizadaApiRepository
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.PersonaAutorizada
import mx.tec.codea.domain.PersonaAutorizadaRules

// Ajustes → Personas autorizadas: el admin ve y cambia quién puede recoger a un niño.
// la lista de niños es la de InfantesAdminViewModel; aquí solo vive la de un niño a la vez.
class PersonasAutorizadasViewModel : ViewModel() {

    private val repository = PersonaAutorizadaApiRepository()
    private var infanteId: String? = null

    var cargando by mutableStateOf(true)
        private set
    var fallo by mutableStateOf(false)
        private set
    var personas by mutableStateOf<List<PersonaAutorizada>>(emptyList())
        private set

    fun cargar(id: String) {
        infanteId = id
        cargando = true
        fallo = false
        viewModelScope.launch {
            try {
                personas = PersonaAutorizadaRules.ordenadas(repository.deNino(id))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fallo = true
                DataEvents.notice("No se pudieron cargar las personas autorizadas: ${e.toUserMessage()}")
            } finally {
                cargando = false
            }
        }
    }

    fun reintentar() {
        infanteId?.let(::cargar)
    }

    fun agregar(nombre: String, parentesco: String, telefono: String, onSuccess: () -> Unit = {}) {
        val id = infanteId ?: return
        if (!PersonaAutorizadaRules.puedeAgregar(nombre)) return
        launchAction(onSuccess = {
            onSuccess()
            cargar(id)
        }) { repository.agregar(id, nombre, parentesco, telefono) }
    }

    fun quitar(persona: PersonaAutorizada) {
        val id = infanteId ?: return
        launchAction(onSuccess = { cargar(id) }) { repository.quitar(id, persona.id) }
    }
}
