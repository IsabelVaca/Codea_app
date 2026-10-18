package mx.tec.codea.ui.state

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.AvisoApiRepository
import mx.tec.codea.data.CentroApiRepository
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.DeviceLocation
import mx.tec.codea.data.SolicitudBorradoApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Aviso
import mx.tec.codea.domain.UbicacionCentro

// vive solo en la pantalla Centro.
class CentroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AvisoApiRepository()
    private val centroRepository = CentroApiRepository()
    private val location = DeviceLocation(application)

    // dónde cree el servidor que está la estancia; null mientras carga.
    var ubicacion by mutableStateOf<UbicacionCentro?>(null)
        private set

    // mientras se busca la ubicación del teléfono, el botón se apaga.
    var ubicando by mutableStateOf(false)
        private set
    private val borradoRepository = SolicitudBorradoApiRepository()

    // cuántas fotos esperan la decisión del admin (el contador de Centro).
    private val _pendientesBorrado = MutableStateFlow(0)
    val pendientesBorrado: StateFlow<Int> = _pendientesBorrado.asStateFlow()

    var avisoTexto by mutableStateOf("")
        private set

    // id de la sala a la que va el aviso; null = todas las salas.
    var destinoSalaId by mutableStateOf<String?>(null)
        private set

    // el historial, el más reciente primero.
    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    // mientras se manda, el botón se apaga para no publicar dos veces.
    private var publicando by mutableStateOf(false)

    // se recalcula solo, no se guarda.
    val canPublicar: Boolean
        get() = avisoTexto.isNotBlank() && !publicando

    init {
        // cada vez que algo cambia en el servidor (por ejemplo, un aviso nuevo), se vuelve a pedir.
        viewModelScope.launch {
            DataEvents.changed.collect {
                cargarAvisos()
                cargarPendientesBorrado()
                cargarUbicacion()
            }
        }
        cargarAvisos()
        cargarPendientesBorrado()
        cargarUbicacion()
    }

    private fun cargarUbicacion() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                ubicacion = centroRepository.ubicacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // si falla se queda sin mostrar; el resto de Centro sigue sirviendo.
            }
        }
    }

    fun hasLocationPermission(): Boolean = location.hasPermission()

    // toma la ubicación del teléfono y la manda como la ubicación de la estancia.
    // el aviso de cómo salió va al snackbar.
    fun fijarUbicacionActual() {
        if (ubicando) return
        ubicando = true
        viewModelScope.launch {
            try {
                val punto = location.current()
                if (punto == null) {
                    DataEvents.notice("No pudimos ubicarte: activa el GPS y vuelve a intentar")
                    return@launch
                }
                ubicacion = centroRepository.fijarUbicacion(punto.latitude, punto.longitude)
                DataEvents.notice("Ubicación de la estancia actualizada")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            } finally {
                ubicando = false
            }
        }
    }

    private fun cargarPendientesBorrado() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                _pendientesBorrado.value = borradoRepository.pendientes().size
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // el contador es un detalle: si falla no se molesta con un aviso, se queda como estaba.
            }
        }
    }

    fun cargarAvisos() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                _avisos.value = repository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo cargar el historial de avisos: ${e.toUserMessage()}")
            }
        }
    }

    fun onAvisoTextoChange(texto: String) {
        avisoTexto = texto
    }

    fun onDestinoChange(salaId: String?) {
        destinoSalaId = salaId
    }

    // manda el aviso a la sala elegida (o a todo el centro). si falla, el texto se queda
    // y el error sale en el snackbar.
    fun publicarAviso(onSuccess: () -> Unit = {}) {
        if (!canPublicar) return
        val texto = avisoTexto
        val destino = destinoSalaId
        publicando = true
        launchAction(onSuccess = {
            publicando = false
            avisoTexto = ""
            onSuccess()
        }) {
            try {
                repository.publicar(texto, destino)
            } catch (e: Exception) {
                publicando = false
                throw e
            }
        }
    }
}
