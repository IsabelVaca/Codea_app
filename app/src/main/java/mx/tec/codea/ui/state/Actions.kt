package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.api.toUserMessage

// corre una escritura a la API. si sale bien: avisa a todas las listas que se vuelvan a pedir
// y llama onSuccess (cerrar la pantalla, mostrar el snackbar). si falla: manda el error al snackbar.
fun ViewModel.launchAction(onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
    viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataEvents.notice(e.toUserMessage())
            return@launch
        }
        DataEvents.notifyChanged()
        onSuccess()
    }
}
