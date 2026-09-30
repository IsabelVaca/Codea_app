package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// vive solo en la pantalla Centro.
class CentroViewModel : ViewModel() {

    var avisoTexto by mutableStateOf("")
        private set

    // se recalcula solo, no se guarda.
    val canPublicar: Boolean
        get() = avisoTexto.isNotBlank()

    fun onAvisoTextoChange(texto: String) {
        avisoTexto = texto
    }

    fun publicarAviso() {
        if (!canPublicar) return
        // aquí se mandaría el aviso a la API
        avisoTexto = ""
    }
}
