package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.tec.codea.data.AvisoApiRepository
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.LeidosStore
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Aviso
import mx.tec.codea.ui.screens.AvisoFundacionUi
import mx.tec.codea.ui.screens.parent.notifications.NotifUi
import mx.tec.codea.ui.screens.parent.notifications.TipoNotif

// los avisos que le llegan a la asistente (Menú) y al papá (campana de Hoy).
// la API manda solo los que le tocan a cada quien. "leído" se guarda en el teléfono.
class AvisosViewModel : ViewModel() {

    private val repository = AvisoApiRepository()

    private val todos = MutableStateFlow<List<Aviso>>(emptyList())
    private val leidos = MutableStateFlow<Set<String>>(emptySet())

    // los ids que eran nuevos al abrir la pantalla; null = todavía no se abre.
    private val snapshot = MutableStateFlow<Set<String>?>(null)

    // cada usuario (correo) tiene sus propios leídos.
    private val clave get() = "avisos:${ApiConfig.devEmail.orEmpty()}"
    private var ultimaClave = ""

    // el número que va en el badge del Menú / la campana.
    val pendientes: StateFlow<Int> = combine(todos, leidos) { t, l -> t.count { it.id !in l } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val avisos: StateFlow<List<AvisoFundacionUi>> = combine(todos, leidos, snapshot) { t, l, snap ->
        t.map { it.toFundacionUi(nuevo = esNuevo(it, l, snap)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val notifs: StateFlow<List<NotifUi>> = combine(todos, leidos, snapshot) { t, l, snap ->
        t.map { it.toNotifUi(nueva = esNuevo(it, l, snap)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // se vuelve a pedir cuando algo cambia en el servidor o cuando cambia el rol.
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    private fun esNuevo(aviso: Aviso, leidos: Set<String>, snap: Set<String>?): Boolean =
        if (snap != null) aviso.id in snap else aviso.id !in leidos

    private fun hayUsuario() = ApiConfig.isTeacher || ApiConfig.isParent

    fun cargar() {
        if (!hayUsuario()) {
            todos.value = emptyList()
            return
        }
        viewModelScope.launch {
            // otro usuario: se empieza sin lo que el anterior estaba viendo.
            if (clave != ultimaClave) {
                snapshot.value = null
                ultimaClave = clave
            }
            leidos.value = LeidosStore.get(clave)
            try {
                todos.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar los avisos: ${e.toUserMessage()}")
            }
        }
    }

    // llamar al abrir la lista de avisos (asistente) o el panel (papá):
    // lo que no se había visto se muestra como "nuevo" en esta visita y se marca como leído.
    fun alAbrir() {
        if (!hayUsuario()) return
        viewModelScope.launch {
            // primero se pide la lista otra vez, para no perder avisos que llegaron hace poco.
            try {
                todos.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar los avisos: ${e.toUserMessage()}")
            }
            val ya = LeidosStore.get(clave)
            val ids = todos.value.map { it.id }.filter { it !in ya }.toSet()
            snapshot.value = ids
            LeidosStore.agregar(clave, ids)
            leidos.value = ya + ids
        }
    }
}

private fun Aviso.toFundacionUi(nuevo: Boolean) = AvisoFundacionUi(
    id = id, destino = destino, fecha = fecha, texto = texto, nuevo = nuevo,
)

private fun Aviso.toNotifUi(nueva: Boolean) = NotifUi(
    id = id, tipo = TipoNotif.AVISO, fecha = fecha, texto = texto, nueva = nueva,
)
