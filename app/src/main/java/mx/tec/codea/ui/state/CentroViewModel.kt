package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.data.AvisoApiRepository

// vive solo en la pantalla Centro.
class CentroViewModel : ViewModel() {

    private val repository = AvisoApiRepository()

    var avisoTexto by mutableStateOf("")
        private set

    // mientras se manda, el botón se apaga para no publicar dos veces.
    private var publicando by mutableStateOf(false)

    // se recalcula solo, no se guarda.
    val canPublicar: Boolean
        get() = avisoTexto.isNotBlank() && !publicando

    fun onAvisoTextoChange(texto: String) {
        avisoTexto = texto
    }

    // manda el aviso a todo el centro. si falla, el texto se queda y el error sale en el snackbar.
    fun publicarAviso(onSuccess: () -> Unit = {}) {
        if (!canPublicar) return
        val texto = avisoTexto
        publicando = true
        launchAction(onSuccess = {
            publicando = false
            avisoTexto = ""
            onSuccess()
        }) {
            try {
                repository.publicar(texto)
            } catch (e: Exception) {
                publicando = false
                throw e
            }
        }
    }
}
