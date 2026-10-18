package mx.tec.codea.ui.state

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.CentroApiRepository
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.DeviceLocation
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.UbicacionCentro

// Ajustes → Checador: dónde está la estancia y a qué distancia puede registrar su entrada la docente.
// antes vivía en Centro (CentroViewModel).
class AjustesChecadorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CentroApiRepository()
    private val location = DeviceLocation(application)

    // dónde cree el servidor que está la estancia; null mientras carga.
    var ubicacion by mutableStateOf<UbicacionCentro?>(null)
        private set

    // mientras se busca la ubicación del teléfono, el botón se apaga.
    var ubicando by mutableStateOf(false)
        private set

    // lo que el admin escribe en el campo de radio; arranca con el radio que ya tiene el servidor.
    var radioTexto by mutableStateOf("")
        private set

    // mientras se manda el radio, el botón se apaga para no guardar dos veces.
    private var guardandoRadio by mutableStateOf(false)

    // el radio escrito es un número dentro de los límites de la API y distinto del guardado.
    val canGuardarRadio: Boolean
        get() {
            val radio = UbicacionCentro.radioDesdeTexto(radioTexto) ?: return false
            return !guardandoRadio && radio != ubicacion?.radioMetros
        }

    // el campo tiene texto pero no sirve (vacío no cuenta como error).
    val radioInvalido: Boolean
        get() = radioTexto.isNotBlank() && UbicacionCentro.radioDesdeTexto(radioTexto) == null

    init {
        viewModelScope.launch {
            DataEvents.changed.collect { cargar() }
        }
        cargar()
    }

    private fun cargar() {
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                val cargada = repository.ubicacion()
                // si el admin ya está escribiendo, no se le pisa el campo.
                if (ubicacion == null || radioTexto == ubicacion?.radioMetros?.toString()) {
                    radioTexto = cargada.radioMetros.toString()
                }
                ubicacion = cargada
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // si falla se queda sin mostrar; el resto de Ajustes sigue sirviendo.
            }
        }
    }

    fun onRadioChange(texto: String) {
        // solo dígitos y a lo más cuatro (el máximo es 1000).
        radioTexto = texto.filter { it.isDigit() }.take(4)
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
                ubicacion = repository.fijarUbicacion(punto.latitude, punto.longitude)
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

    fun guardarRadio() {
        val radio = UbicacionCentro.radioDesdeTexto(radioTexto) ?: return
        if (!canGuardarRadio) return
        guardandoRadio = true
        launchAction(onSuccess = {
            guardandoRadio = false
            DataEvents.notice("Radio del checador actualizado")
        }) {
            try {
                ubicacion = repository.fijarRadio(radio)
            } catch (e: Exception) {
                guardandoRadio = false
                throw e
            }
        }
    }
}
