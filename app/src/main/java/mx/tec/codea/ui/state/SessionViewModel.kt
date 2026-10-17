package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.data.SessionState
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.AuthError
import mx.tec.codea.domain.AuthValidator

// lo que la pantalla de entrada muestra: si está trabajando y qué salió mal. el error puede venir de las
// reglas locales (AuthError, que la pantalla traduce a un texto) o de la API (ya es un texto).
data class AuthFormState(
    val loading: Boolean = false,
    val localError: AuthError? = null,
    val remoteError: String? = null,
)

// la sesión de toda la app: Cargando, Anónimo o Activa. CodeaApp decide qué dibujar con ella,
// así que cerrar sesión no navega a ningún lado: solo cambia el estado.
class SessionViewModel(private val sessions: SessionRepository) : ViewModel() {

    val state: StateFlow<SessionState> = sessions.state

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    fun login(email: String, password: String) {
        val error = AuthValidator.login(email, password)
        if (error != null) {
            _form.value = AuthFormState(localError = error)
            return
        }
        run { sessions.login(email, password) }
    }

    fun activate(email: String, code: String, password: String, confirmation: String) {
        val error = AuthValidator.activate(email, code, password, confirmation)
        if (error != null) {
            _form.value = AuthFormState(localError = error)
            return
        }
        run { sessions.activate(email, code, password) }
    }

    fun clearError() {
        _form.value = AuthFormState()
    }

    fun logout() {
        viewModelScope.launch { sessions.logout() }
    }

    // al terminar bien, la sesión pasa a Activa sola (el repositorio la guarda) y CodeaApp cambia de pantalla.
    private fun run(block: suspend () -> Unit) {
        if (_form.value.loading) return
        _form.value = AuthFormState(loading = true)
        viewModelScope.launch {
            try {
                block()
                _form.value = AuthFormState()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _form.value = AuthFormState(remoteError = e.toUserMessage())
            }
        }
    }

    class Factory(private val sessions: SessionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SessionViewModel(sessions) as T
    }
}
