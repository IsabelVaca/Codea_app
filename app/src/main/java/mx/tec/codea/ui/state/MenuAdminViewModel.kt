package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.MenuApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.data.hoyIso
import mx.tec.codea.domain.MenuDelDia
import mx.tec.codea.domain.MenuRules

// Ajustes → Menú del día: el admin captura lo que se come cada día, de lunes a viernes, una semana a la vez.
class MenuAdminViewModel : ViewModel() {

    private val repository = MenuApiRepository()

    // el lunes de la semana que se está viendo ("aaaa-mm-dd").
    var lunes by mutableStateOf(MenuRules.lunesDe(hoyIso()))
        private set

    // los menús capturados de esa semana, por fecha. un día sin menú no aparece.
    var menus by mutableStateOf<Map<String, MenuDelDia>>(emptyMap())
        private set

    var cargando by mutableStateOf(true)
        private set

    val dias: List<String>
        get() = MenuRules.diasHabiles(lunes)

    private var cargaEnCurso: Job? = null

    init {
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    fun cargar() {
        if (!ApiConfig.isAdmin) return
        val semana = lunes
        // si el admin cambia de semana antes de que llegue la respuesta, la anterior se descarta.
        cargaEnCurso?.cancel()
        cargaEnCurso = viewModelScope.launch {
            cargando = true
            try {
                menus = repository.entre(semana, MenuRules.sumarDias(semana, 4))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo cargar el menú: ${e.toUserMessage()}")
            }
            cargando = false
        }
    }

    fun semanaAnterior() = irA(MenuRules.sumarDias(lunes, -7))

    fun semanaSiguiente() = irA(MenuRules.sumarDias(lunes, 7))

    fun estaSemana() = irA(MenuRules.lunesDe(hoyIso()))

    private fun irA(nuevoLunes: String) {
        lunes = nuevoLunes
        menus = emptyMap()
        cargar()
    }

    // crea o reemplaza el menú de ese día. si falla, el error sale en el snackbar y el diálogo se queda abierto.
    fun guardar(fecha: String, menu: MenuDelDia, onSuccess: () -> Unit = {}) {
        if (!MenuRules.puedeGuardar(menu)) return
        launchAction(onSuccess = onSuccess) { repository.guardar(fecha, menu) }
    }

    fun quitar(fecha: String, onSuccess: () -> Unit = {}) {
        launchAction(onSuccess = onSuccess) { repository.quitar(fecha) }
    }
}
