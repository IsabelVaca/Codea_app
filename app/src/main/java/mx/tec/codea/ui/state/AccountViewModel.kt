package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.AccountApiRepository
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.AuthError
import mx.tec.codea.domain.AuthValidator

// "Mi cuenta": el perfil de quien tiene la sesión y el cambio de contraseña.
class AccountViewModel(sessions: SessionRepository) : ViewModel() {

    private val repository = AccountApiRepository(sessions)

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set

    // para un padre: el nombre de sus hijos.
    var children by mutableStateOf<List<String>>(emptyList())
        private set
    var loadError by mutableStateOf<String?>(null)
        private set

    // el formulario de cambiar contraseña.
    var changing by mutableStateOf(false)
        private set
    var localError by mutableStateOf<AuthError?>(null)
        private set
    var remoteError by mutableStateOf<String?>(null)
        private set

    // true justo después de cambiar la contraseña: la pantalla cierra el formulario y lo apaga con passwordChangedShown().
    var passwordChanged by mutableStateOf(false)
        private set

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            try {
                val me = repository.me()
                name = me.fullName
                email = me.email.orEmpty()
                children = me.parent?.children?.map { it.fullName }.orEmpty()
                loadError = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loadError = e.toUserMessage()
            }
        }
    }

    fun passwordChangedShown() {
        passwordChanged = false
    }

    fun clearPasswordErrors() {
        localError = null
        remoteError = null
    }

    fun changePassword(current: String, new: String, confirmation: String, onSuccess: () -> Unit) {
        val error = AuthValidator.changePassword(current, new, confirmation, email)
        if (error != null) {
            localError = error
            remoteError = null
            return
        }
        if (changing) return
        changing = true
        localError = null
        remoteError = null
        viewModelScope.launch {
            try {
                repository.changePassword(current, new)
                passwordChanged = true
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                remoteError = e.toUserMessage()
            } finally {
                changing = false
            }
        }
    }

    class Factory(private val sessions: SessionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AccountViewModel(sessions) as T
    }
}
