package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class AgregarMomentoUiState(
    val elegido: String? = null,
    val nombrePropio: String = "",
    val registroPropio: String = ""
)

// vive solo en esta pantalla.
class AgregarMomentoViewModel : ViewModel() {

    var uiState by mutableStateOf(AgregarMomentoUiState())
        private set

    fun onElegir(nombre: String?) { uiState = uiState.copy(elegido = nombre) }
    fun onNombrePropioChange(v: String) { uiState = uiState.copy(nombrePropio = v) }
    fun onRegistroPropioChange(v: String) { uiState = uiState.copy(registroPropio = v) }
}
