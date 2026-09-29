package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.domain.InfanteError
import mx.tec.codea.domain.InfanteValidator

data class InscribirInfanteUiState(
    val nombre: String = "",
    val edad: String = "",
    val alergias: String = "",
    val tutor: String = "",
    val tituloTutor: String = "",
    val telefono: String = "",
    val salaElegida: String? = null
) {
    val nombreError: InfanteError?
        get() = if (nombre.isEmpty()) null else InfanteValidator.validateNombre(nombre)

    val edadError: InfanteError?
        get() = if (edad.isEmpty()) null else InfanteValidator.validateEdad(edad)

    val tutorError: InfanteError?
        get() = if (tutor.isEmpty()) null else InfanteValidator.validateTutor(tutor)

    val telefonoError: InfanteError?
        get() = if (telefono.isEmpty()) null else InfanteValidator.validateTelefono(telefono)

    val canGuardar: Boolean
        get() = InfanteValidator.isValid(nombre, edad, tutor, telefono)
            && tituloTutor.isNotBlank()
            && salaElegida != null
}

// vive solo en esta pantalla.
class InscribirInfanteViewModel : ViewModel() {

    var uiState by mutableStateOf(InscribirInfanteUiState())
        private set

    fun onNombreChange(v: String) { uiState = uiState.copy(nombre = v) }
    fun onEdadChange(v: String) { uiState = uiState.copy(edad = v) }
    fun onAlergiasChange(v: String) { uiState = uiState.copy(alergias = v) }
    fun onTutorChange(v: String) { uiState = uiState.copy(tutor = v) }
    fun onTituloTutorChange(v: String) { uiState = uiState.copy(tituloTutor = v) }
    fun onTelefonoChange(v: String) { uiState = uiState.copy(telefono = v) }

    fun onElegirSala(nombre: String) {
        uiState = uiState.copy(salaElegida = if (uiState.salaElegida == nombre) null else nombre)
    }
}
