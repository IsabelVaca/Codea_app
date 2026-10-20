package mx.tec.codea.ui.state

import android.app.Application
import android.net.Uri
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
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.ImageCompressor
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.Aviso

// vive solo en la pantalla Centro.
class CentroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AvisoApiRepository()

    var avisoTexto by mutableStateOf("")
        private set

    // la imagen que se va a publicar con el aviso; null = aviso solo de texto.
    var avisoFoto by mutableStateOf<Uri?>(null)
        private set

    // id de la sala a la que va el aviso; null = todas las salas.
    var destinoSalaId by mutableStateOf<String?>(null)
        private set

    // el historial, el más reciente primero.
    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    // mientras se manda, el botón se apaga para no publicar dos veces y dice «Publicando…».
    var publicando by mutableStateOf(false)
        private set

    // se recalcula solo, no se guarda.
    val canPublicar: Boolean
        get() = (avisoTexto.isNotBlank() || avisoFoto != null) && !publicando

    init {
        // cada vez que algo cambia en el servidor (por ejemplo, un aviso nuevo), se vuelve a pedir.
        viewModelScope.launch {
            DataEvents.changed.collect { cargarAvisos() }
        }
        cargarAvisos()
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

    fun onFotoChange(uri: Uri?) {
        avisoFoto = uri
    }

    fun onDestinoChange(salaId: String?) {
        destinoSalaId = salaId
    }

    // manda el aviso (texto, imagen o ambos) a la sala elegida (o a todo el centro). si falla, el texto y la imagen se quedan
    // y el error sale en el snackbar.
    fun publicarAviso(onSuccess: () -> Unit = {}) {
        if (!canPublicar) return
        val texto = avisoTexto
        val destino = destinoSalaId
        val foto = avisoFoto
        publicando = true
        launchAction(onSuccess = {
            publicando = false
            avisoTexto = ""
            avisoFoto = null
            onSuccess()
        }) {
            try {
                // la imagen se baja de tamaño y se endereza antes de subirla (el marco pide comprimir).
                val jpeg = foto?.let { ImageCompressor.compress(getApplication<Application>().contentResolver, it) }
                repository.publicar(texto, destino, jpeg)
            } catch (e: Exception) {
                publicando = false
                throw e
            }
        }
    }
}
