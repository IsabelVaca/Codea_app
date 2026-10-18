package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.TeacherApiRepository
import mx.tec.codea.data.api.DailyEntryBody
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Animo
import mx.tec.codea.domain.Comida
import mx.tec.codea.domain.ResumenDelDiaReglas

// el resumen del día de un niño que captura la docente (lo que el papá ve en Hoy).
// vive solo en esa pantalla; se llena con lo que ya se capturó hoy, si hay algo.
class ResumenDelDiaViewModel : ViewModel() {

    private val repository = TeacherApiRepository()
    private var childId: Int? = null

    var cargando by mutableStateOf(true)
        private set
    var guardando by mutableStateOf(false)
        private set

    var nombre by mutableStateOf("")
        private set
    var animo by mutableStateOf<Animo?>(null)
        private set
    var comida by mutableStateOf<Comida?>(null)
        private set
    var panales by mutableStateOf(0)
        private set
    var siesta by mutableStateOf(0)
        private set
    var nota by mutableStateOf("")
        private set

    // lo que la familia le dejó a la asistente hoy.
    var notasFamilia by mutableStateOf<List<String>>(emptyList())
        private set

    // si el servidor ya tenía algo de panales o siesta; si no, no se manda un 0 que pise un dato real al guardar.
    private var panalesTocados = false
    private var siestaTocada = false

    fun cargar(id: Int) {
        if (childId == id) return
        childId = id
        cargando = true
        viewModelScope.launch {
            try {
                val dia = repository.day(id)
                nombre = dia.child.name
                val s = dia.summary
                animo = Animo.deApi(s?.mood)
                comida = Comida.deApi(s?.meals)
                panales = ResumenDelDiaReglas.panales(s?.diaperChanges ?: 0)
                siesta = ResumenDelDiaReglas.siesta(s?.napMinutes ?: 0)
                panalesTocados = s?.diaperChanges != null
                siestaTocada = s?.napMinutes != null
                nota = s?.generalNote ?: ""
                notasFamilia = dia.notes.map { it.text }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                childId = null // para poder reintentar al volver a abrir
                DataEvents.notice("No se pudo cargar el resumen: ${e.toUserMessage()}")
            } finally {
                cargando = false
            }
        }
    }

    fun onAnimo(valor: Animo) { animo = if (animo == valor) null else valor }
    fun onComida(valor: Comida) { comida = if (comida == valor) null else valor }
    fun onNota(texto: String) { nota = texto }

    fun masPanales() { panales = ResumenDelDiaReglas.panales(panales + 1); panalesTocados = true }
    fun menosPanales() { panales = ResumenDelDiaReglas.panales(panales - 1); panalesTocados = true }

    fun masSiesta() { siesta = ResumenDelDiaReglas.siesta(siesta + ResumenDelDiaReglas.SIESTA_PASO); siestaTocada = true }
    fun menosSiesta() { siesta = ResumenDelDiaReglas.siesta(siesta - ResumenDelDiaReglas.SIESTA_PASO); siestaTocada = true }

    // manda solo lo que se llenó: la API no toca lo demás.
    fun guardar(onSuccess: () -> Unit) {
        val id = childId ?: return
        if (guardando) return
        guardando = true
        val cuerpo = DailyEntryBody(
            mood = animo?.api,
            meals = comida?.api,
            diaperChanges = if (panalesTocados) panales else null,
            napMinutes = if (siestaTocada) siesta else null,
            generalNote = nota.trim().ifBlank { null },
        )
        launchAction(onSuccess = {
            guardando = false
            onSuccess()
        }) {
            try {
                repository.saveDailyEntry(id, cuerpo)
            } catch (e: Exception) {
                guardando = false
                throw e
            }
        }
    }
}
