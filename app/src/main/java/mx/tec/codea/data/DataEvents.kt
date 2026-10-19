package mx.tec.codea.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// el código que hay que mostrarle al admin para que lo comparta (se muestra una sola vez).
data class AccessCodeNotice(
    val personName: String,
    val code: String,
    val expiresAt: String,
    // para mandar la invitación por WhatsApp: el teléfono y el correo de la persona, y el enlace que arma el servidor
    // (null mientras el servidor no tenga URL pública; entonces el mensaje solo lleva el código).
    val phone: String? = null,
    val email: String? = null,
    val link: String? = null,
)

// tres avisos globales entre los ViewModels:
// - changed: algo cambió en el servidor, cada lista se vuelve a pedir.
// - notices: textos para el snackbar (errores de la API, por ejemplo).
// - accessCodes: el código de acceso de alguien recién dado de alta; CodeaNavHost lo muestra en un diálogo.
object DataEvents {

    private val _changed = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val changed: SharedFlow<Unit> = _changed.asSharedFlow()

    private val _notices = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val notices: SharedFlow<String> = _notices.asSharedFlow()

    private val _accessCodes = MutableSharedFlow<AccessCodeNotice>(extraBufferCapacity = 8)
    val accessCodes: SharedFlow<AccessCodeNotice> = _accessCodes.asSharedFlow()

    fun accessCode(notice: AccessCodeNotice) {
        _accessCodes.tryEmit(notice)
    }

    fun notifyChanged() {
        _changed.tryEmit(Unit)
    }

    fun notice(text: String) {
        _notices.tryEmit(text)
    }
}
