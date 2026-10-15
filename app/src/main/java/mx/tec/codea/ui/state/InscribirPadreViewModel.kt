package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.domain.PadreError
import mx.tec.codea.domain.PadreValidator

data class InscribirPadreUiState(
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val correo: String = "",
    val parentesco: String? = null,
    val hijosSeleccionados: List<String> = emptyList()
) {
    val nombreError: PadreError?
        get() = if (nombre.isEmpty()) null else PadreValidator.validateNombre(nombre)

    val telefonoError: PadreError?
        get() = if (telefono.isEmpty()) null else PadreValidator.validateTelefono(telefono)

    val correoError: PadreError?
        get() = if (correo.isEmpty()) null else PadreValidator.validateCorreo(correo)

    val canGuardar: Boolean
        get() = PadreValidator.isValid(nombre, telefono, correo)
            && apellido.isNotBlank()
            && parentesco != null
            && hijosSeleccionados.isNotEmpty()
}

// vive solo en esta pantalla.
class InscribirPadreViewModel : ViewModel() {

    var uiState by mutableStateOf(InscribirPadreUiState())
        private set

    fun onNombreChange(v: String) { uiState = uiState.copy(nombre = v) }
    fun onApellidoChange(v: String) { uiState = uiState.copy(apellido = v) }
    fun onTelefonoChange(v: String) { uiState = uiState.copy(telefono = v) }
    fun onCorreoChange(v: String) { uiState = uiState.copy(correo = v) }
    fun onElegirParentesco(v: String) { uiState = uiState.copy(parentesco = v) }

    fun onToggleHijo(nombre: String) {
        val actuales = uiState.hijosSeleccionados
        uiState = uiState.copy(
            hijosSeleccionados = if (nombre in actuales) actuales - nombre else actuales + nombre
        )
    }
}
