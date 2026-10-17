package mx.tec.codea.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// dos avisos globales entre los ViewModels de admin:
// - changed: algo cambió en el servidor, cada lista se vuelve a pedir.
// - notices: textos para el snackbar (errores de la API, por ejemplo).
object DataEvents {

    private val _changed = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val changed: SharedFlow<Unit> = _changed.asSharedFlow()

    private val _notices = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val notices: SharedFlow<String> = _notices.asSharedFlow()

    fun notifyChanged() {
        _changed.tryEmit(Unit)
    }

    fun notice(text: String) {
        _notices.tryEmit(text)
    }
}
