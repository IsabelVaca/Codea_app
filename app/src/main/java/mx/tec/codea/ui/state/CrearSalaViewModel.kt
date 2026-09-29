package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.domain.SalaError
import mx.tec.codea.domain.SalaValidator

data class CrearSalaUiState(
    val nombre: String = "",
    val cupo: String = "",
    val asistenteElegida: String? = null
) {
    val nombreError: SalaError?
        get() = if (nombre.isEmpty()) null else SalaValidator.validateNombre(nombre)

    val cupoError: SalaError?
        get() = if (cupo.isEmpty()) null else SalaValidator.validateCupo(cupo)

    val canGuardar: Boolean
        get() = SalaValidator.isValid(nombre, cupo)
}

class CrearSalaViewModel : ViewModel() {

    var uiState by mutableStateOf(CrearSalaUiState())
        private set

    fun onNombreChange(nombre: String) {
        uiState = uiState.copy(nombre = nombre)
    }

    fun onCupoChange(cupo: String) {
        uiState = uiState.copy(cupo = cupo)
    }

    fun onElegirAsistente(nombre: String) {
        uiState = uiState.copy(
            asistenteElegida = if (uiState.asistenteElegida == nombre) null else nombre
        )
    }
}