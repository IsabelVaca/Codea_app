package mx.tec.codea.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.tec.codea.data.api.ActivateBody
import mx.tec.codea.data.api.AuthApi
import mx.tec.codea.data.api.LoginBody
import mx.tec.codea.data.api.RefreshBody
import mx.tec.codea.data.api.TokenDto
import mx.tec.codea.data.local.SessionStore
import mx.tec.codea.domain.Invite
import mx.tec.codea.domain.Session
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

// qué sabe la app de la sesión: todavía no la lee del disco (Loading), no hay (Anonymous) o hay una (Active).
sealed interface SessionState {
    data object Loading : SessionState
    data object Anonymous : SessionState
    data class Active(val session: Session) : SessionState
}

// qué pasó al intentar renovar la sesión.
enum class RefreshResult {
    // hay tokens nuevos.
    Renewed,

    // la API rechazó el refresh token (vencido, revocado o reusado): la sesión terminó y ya se borró.
    SessionEnded,

    // no se pudo saber (sin red o error del servidor): la sesión se conserva.
    Unavailable,
}

// la única puerta a la sesión. guarda los tokens en disco (SessionStore), los tiene en memoria para que el
// interceptor los lea sin esperar, y expone el estado como un flujo para que la interfaz reaccione.
class SessionRepository(
    private val store: SessionStore,
    private val authApi: AuthApi,
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    // una renovación a la vez: cinco peticiones con el token vencido hacen un solo refresh, no cinco.
    private val refreshLock = Mutex()

    fun current(): Session? = (_state.value as? SessionState.Active)?.session

    // se llama una vez al abrir la app.
    suspend fun load() {
        val saved = store.read()
        _state.value = if (saved != null) SessionState.Active(saved) else SessionState.Anonymous
    }

    suspend fun login(email: String, password: String) {
        save(authApi.login(LoginBody(Invite.cleanEmail(email), password)))
    }

    suspend fun activate(email: String, code: String, password: String) {
        save(authApi.activate(ActivateBody(Invite.cleanEmail(email), Invite.cleanCode(code), password)))
    }

    // guarda los tokens nuevos. si la respuesta no trae `me` (refrescar), se conservan el nombre y los roles.
    suspend fun save(tokens: TokenDto) {
        val previous = current()
        val me = tokens.me
        val session = Session(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            userId = me?.id ?: previous?.userId.orEmpty(),
            name = me?.fullName ?: previous?.name.orEmpty(),
            roles = me?.roles?.toSet() ?: previous?.roles.orEmpty(),
        )
        store.write(session)
        _state.value = SessionState.Active(session)
    }

    // cierra la sesión: primero se borra lo local y luego se avisa al servidor, ignorando si falla.
    suspend fun logout() {
        val refresh = current()?.refreshToken
        clear()
        if (refresh != null) {
            try {
                authApi.logout(RefreshBody(refresh))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // sin red o ya vencido: da igual, el refresh token muere solo.
            }
        }
    }

    suspend fun clear() {
        store.clear()
        _state.value = SessionState.Anonymous
    }

    // renueva los tokens. `staleAccessToken` es el que falló: si ya cambió, otra petición ya renovó y no
    // hace falta volver a hacerlo.
    suspend fun refresh(staleAccessToken: String?): RefreshResult = refreshLock.withLock {
        val session = current() ?: return RefreshResult.SessionEnded
        if (staleAccessToken != null && session.accessToken != staleAccessToken) return RefreshResult.Renewed
        try {
            save(authApi.refresh(RefreshBody(session.refreshToken)))
            RefreshResult.Renewed
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            // 401: el refresh token ya no sirve. otro código (5xx): el servidor falló, la sesión puede seguir viva.
            if (e.code() == 401 || e.code() == 403) {
                clear()
                RefreshResult.SessionEnded
            } else {
                RefreshResult.Unavailable
            }
        } catch (e: IOException) {
            RefreshResult.Unavailable
        }
    }
}
