package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.data.hoyLocal
import mx.tec.codea.domain.InfanteError
import mx.tec.codea.domain.InfanteValidator

data class InscribirInfanteUiState(
    val nombre: String = "",
    val apellido: String = "",
    // "dd/mm/aaaa", como lo escribe quien inscribe.
    val nacimiento: String = "",
    val alergias: String = "",
    val salaElegida: String? = null
) {
    val nombreError: InfanteError?
        get() = if (nombre.isEmpty()) null else InfanteValidator.validateNombre(nombre)

    val nacimientoError: InfanteError?
        get() = if (nacimiento.isEmpty()) null else InfanteValidator.validateNacimiento(nacimiento, hoyLocal())

    // la fecha en el formato de la API ("aaaa-mm-dd"); null mientras no sea una fecha válida.
    val nacimientoIso: String?
        get() = InfanteValidator.parseNacimiento(nacimiento, hoyLocal())

    val canGuardar: Boolean
        get() = InfanteValidator.isValid(nombre, nacimiento, hoyLocal()) && salaElegida != null
}

// vive solo en esta pantalla.
class InscribirInfanteViewModel : ViewModel() {

    var uiState by mutableStateOf(InscribirInfanteUiState())
        private set

    fun onNombreChange(v: String) { uiState = uiState.copy(nombre = v) }
    fun onApellidoChange(v: String) { uiState = uiState.copy(apellido = v) }
    fun onNacimientoChange(v: String) { uiState = uiState.copy(nacimiento = v) }
    fun onAlergiasChange(v: String) { uiState = uiState.copy(alergias = v) }

    fun onElegirSala(nombre: String) {
        uiState = uiState.copy(salaElegida = if (uiState.salaElegida == nombre) null else nombre)
    }
}
