package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.tec.codea.data.PadreRepository
import mx.tec.codea.domain.Padre

class PadresAdminViewModel : ViewModel() {

    private val repository = PadreRepository()

    private val _padres = MutableStateFlow(repository.getAll().sortedBy { it.nombre })
    val padres: StateFlow<List<Padre>> = _padres.asStateFlow()

    fun getPadre(id: String): Padre? = _padres.value.firstOrNull { it.id == id }

    fun toggleAcceso(padreId: String) {
        _padres.value = _padres.value.map { padre ->
            if (padre.id == padreId) padre.copy(conAcceso = !padre.conAcceso) else padre
        }
    }

    fun inscribirPadre(nombre: String, telefono: String, correo: String, parentesco: String, hijos: List<String>) {
        val iniciales = nombre.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        val nuevo = Padre(
            id = "padre-${_padres.value.size + 1}",
            nombre = nombre,
            iniciales = iniciales,
            parentesco = parentesco,
            hijo = hijos.joinToString(", "),
            telefono = telefono,
            correo = correo
        )
        _padres.value = (_padres.value + nuevo).sortedBy { it.nombre }
    }
}
