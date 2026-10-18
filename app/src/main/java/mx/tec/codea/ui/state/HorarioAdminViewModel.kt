package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.CentroApiRepository
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.HorarioCentro
import mx.tec.codea.domain.HorarioRules

// Ajustes → Horario: a qué hora empieza y termina el día, a qué hora se cierran solas las entradas
// que siguen abiertas y qué días se trabaja. vale para todo el centro.
class HorarioAdminViewModel : ViewModel() {

    private val repository = CentroApiRepository()

    // lo que dice el servidor; null mientras carga.
    private var guardado by mutableStateOf<HorarioCentro?>(null)

    // lo que el admin está editando.
    var editado by mutableStateOf<HorarioCentro?>(null)
        private set

    private var guardando by mutableStateOf(false)

    // el primer error que impide guardar; null si el horario está bien.
    val error: String?
        get() = editado?.let(HorarioRules::error)

    // se puede guardar si cargó, no tiene errores, cambió algo y no se está guardando ya.
    val canGuardar: Boolean
        get() = editado != null && error == null && editado != guardado && !guardando

    init {
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    private fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                val servidor = repository.horario()
                // si el admin ya está editando, no se le pisan sus cambios.
                if (editado == null || editado == guardado) editado = servidor
                guardado = servidor
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo cargar el horario: ${e.toUserMessage()}")
            }
        }
    }

    fun onEntrada(minutos: Int) = cambiar { it.copy(entradaMinutos = minutos) }

    fun onSalida(minutos: Int) = cambiar { it.copy(salidaMinutos = minutos) }

    fun onCierre(minutos: Int) = cambiar { it.copy(cierreMinutos = minutos) }

    fun onToggleDia(dia: Int) = cambiar {
        it.copy(diasDeTrabajo = if (dia in it.diasDeTrabajo) it.diasDeTrabajo - dia else it.diasDeTrabajo + dia)
    }

    private fun cambiar(cambio: (HorarioCentro) -> HorarioCentro) {
        editado = editado?.let(cambio)
    }

    // manda el horario; si falla, los cambios se quedan y el error sale en el snackbar.
    fun guardar(onSuccess: () -> Unit = {}) {
        val horario = editado ?: return
        if (!canGuardar) return
        guardando = true
        launchAction(onSuccess = {
            guardando = false
            onSuccess()
        }) {
            try {
                guardado = repository.guardarHorario(horario).also { editado = it }
            } catch (e: Exception) {
                guardando = false
                throw e
            }
        }
    }
}
