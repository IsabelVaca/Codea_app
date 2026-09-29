package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.tec.codea.data.SalaRepository
import mx.tec.codea.domain.Sala

class SalasAdminViewModel : ViewModel() {

    private val repository = SalaRepository()

    private val _salas = MutableStateFlow(repository.getAll())
    val salas: StateFlow<List<Sala>> = _salas.asStateFlow()

    // busca en el estado actual, no en el repository fijo.
    fun getSala(id: String): Sala? = _salas.value.firstOrNull { it.id == id }

    fun crearSala(nombre: String, cupoMaximo: Int, asistente: String?) {
        val nueva = Sala(
            id = "sala-${_salas.value.size + 1}",
            nombre = nombre,
            cupoMaximo = cupoMaximo,
            asistente = asistente ?: "Sin asignar"
        )
        // lista nueva, no se muta la anterior.
        _salas.value = _salas.value + nueva
    }

    fun editarCupo(salaId: String, nuevoCupoMaximo: Int) {
        _salas.value = _salas.value.map { sala ->
            if (sala.id == salaId) sala.copy(cupoMaximo = nuevoCupoMaximo) else sala
        }
    }

    fun asignarAsistente(salaId: String, asistente: String) {
        _salas.value = _salas.value.map { sala ->
            if (sala.id == salaId) sala.copy(asistente = asistente) else sala
        }
    }

    fun eliminarSala(salaId: String) {
        _salas.value = _salas.value.filterNot { it.id == salaId }
    }
}
