package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.domain.DocenteError
import mx.tec.codea.domain.DocenteValidator

data class AltaAsistenteUiState(
    val nombre: String = "",
    val telefono: String = "",
    val correo: String = "",
    val salaElegida: String? = null,
    val turno: String = ""
) {
    val nombreError: DocenteError?
        get() = if (nombre.isEmpty()) null else DocenteValidator.validateNombre(nombre)

    val telefonoError: DocenteError?
        get() = if (telefono.isEmpty()) null else DocenteValidator.validateTelefono(telefono)

    val correoError: DocenteError?
        get() = if (correo.isEmpty()) null else DocenteValidator.validateCorreo(correo)

    val canGuardar: Boolean
        get() = DocenteValidator.isValid(nombre, telefono, correo)
            && salaElegida != null
            && turno.isNotBlank()
}

// vive solo en esta pantalla.
class AltaAsistenteViewModel : ViewModel() {

    var uiState by mutableStateOf(AltaAsistenteUiState())
        private set

    fun onNombreChange(v: String) { uiState = uiState.copy(nombre = v) }
    fun onTelefonoChange(v: String) { uiState = uiState.copy(telefono = v) }
    fun onCorreoChange(v: String) { uiState = uiState.copy(correo = v) }

    fun onElegirSala(nombre: String) {
        uiState = uiState.copy(salaElegida = if (uiState.salaElegida == nombre) null else nombre)
    }

    fun onElegirTurno(turno: String) {
        uiState = uiState.copy(turno = turno)
    }
}
