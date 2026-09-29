package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.tec.codea.data.InfanteRepository
import mx.tec.codea.domain.Infante

class InfantesAdminViewModel : ViewModel() {

    private val repository = InfanteRepository()

    private val _infantes = MutableStateFlow(repository.getAll())
    val infantes: StateFlow<List<Infante>> = _infantes.asStateFlow()

    // busca en el estado actual, no en el repository fijo.
    fun getInfante(id: String): Infante? = _infantes.value.firstOrNull { it.id == id }

    fun cambiarSala(infanteId: String, nuevaSala: String) {
        _infantes.value = _infantes.value.map { infante ->
            if (infante.id == infanteId) infante.copy(sala = nuevaSala) else infante
        }
    }

    fun darDeBaja(infanteId: String) {
        _infantes.value = _infantes.value.map { infante ->
            if (infante.id == infanteId) infante.copy(baja = !infante.baja) else infante
        }
    }

    fun inscribirInfante(nombre: String, edad: Int, tutor: String, tituloTutor: String, sala: String) {
        val iniciales = nombre.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        val nuevo = Infante(
            id = "infante-${_infantes.value.size + 1}",
            nombre = nombre,
            iniciales = iniciales,
            edad = edad,
            tutor = tutor,
            tituloTutor = tituloTutor,
            sala = sala
        )
        _infantes.value = _infantes.value + nuevo
    }
}
