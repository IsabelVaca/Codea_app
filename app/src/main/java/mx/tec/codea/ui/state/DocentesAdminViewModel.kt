package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.tec.codea.data.DocenteRepository
import mx.tec.codea.domain.Docente

class DocentesAdminViewModel : ViewModel() {

    private val repository = DocenteRepository()

    private val _docentes = MutableStateFlow(repository.getAll())
    val docentes: StateFlow<List<Docente>> = _docentes.asStateFlow()

    // busca en el estado actual, no en el repository fijo.
    fun getDocente(id: String): Docente? = _docentes.value.firstOrNull { it.id == id }

    fun altaDocente(nombre: String, sala: String) {
        val iniciales = nombre.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        val nueva = Docente(
            id = "docente-${_docentes.value.size + 1}",
            nombre = nombre,
            iniciales = iniciales,
            sala = sala,
            horaChecada = "--:--"
        )
        _docentes.value = _docentes.value + nueva
    }

    fun cambiarSala(docenteId: String, nuevaSala: String) {
        _docentes.value = _docentes.value.map { docente ->
            if (docente.id == docenteId) docente.copy(sala = nuevaSala) else docente
        }
    }

    fun darDeBaja(docenteId: String) {
        _docentes.value = _docentes.value.map { docente ->
            if (docente.id == docenteId) docente.copy(baja = !docente.baja) else docente
        }
    }
}


