package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.data.RutinaRepository
import mx.tec.codea.domain.MomentoCatalogo
import mx.tec.codea.domain.MomentoRutina

data class NuevaRutinaUiState(
    val nombre: String = "",
    val etapa: String = "",
    val seleccionados: List<String> = emptyList(),
    val creando: Boolean = false,
    val nombrePropio: String = "",
    val registroPropio: String = "",
    val catalogoPropio: List<MomentoCatalogo> = emptyList(),
)

// comparte este formulario entre los 3 pasos del asistente de "nueva rutina",
// igual que ReportFormViewModel con sus tres pantallas.
class NuevaRutinaViewModel : ViewModel() {

    private val repository = RutinaRepository()

    var uiState by mutableStateOf(NuevaRutinaUiState())
        private set

    fun onNombreChange(v: String) { uiState = uiState.copy(nombre = v) }
    fun onElegirEtapa(etapa: String) { uiState = uiState.copy(etapa = etapa) }

    fun catalogoCompleto(): List<MomentoCatalogo> = repository.getCatalogoNuevaRutina() + uiState.catalogoPropio

    fun onAlternar(nombre: String) {
        val actuales = uiState.seleccionados
        uiState = uiState.copy(
            seleccionados = if (nombre in actuales) actuales - nombre else actuales + nombre
        )
    }

    fun onAbrirCrear() { uiState = uiState.copy(creando = true) }
    fun onNombrePropioChange(v: String) { uiState = uiState.copy(nombrePropio = v) }
    fun onRegistroPropioChange(v: String) { uiState = uiState.copy(registroPropio = v) }

    fun onGuardarPropio() {
        val nombre = uiState.nombrePropio.trim()
        if (nombre.isBlank()) return
        val registro = uiState.registroPropio.trim().ifBlank { "Sin detalle" }
        uiState = uiState.copy(
            catalogoPropio = uiState.catalogoPropio + MomentoCatalogo(nombre, registro),
            seleccionados = uiState.seleccionados + nombre,
            nombrePropio = "",
            registroPropio = "",
            creando = false
        )
    }

    fun momentosFinal(): List<MomentoRutina> {
        val catalogo = catalogoCompleto()
        return uiState.seleccionados.map { nombre ->
            MomentoRutina(id = nombre, nombre = nombre, registro = catalogo.firstOrNull { it.nombre == nombre }?.registro.orEmpty())
        }
    }

    fun reordenarMomentos(from: Int, to: Int) {
        val actuales = uiState.seleccionados
        if (from !in actuales.indices || to !in actuales.indices) return
        uiState = uiState.copy(seleccionados = actuales.toMutableList().apply { add(to, removeAt(from)) })
    }
}
