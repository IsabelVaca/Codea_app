package mx.tec.codea.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.data.SessionState
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.offline.Offline
import mx.tec.codea.data.local.KeystoreTokenCipher
import mx.tec.codea.data.local.SessionStore

// el contenedor de dependencias, a mano (DI manual): crea una sola vez lo que viven mientras viva la app.
// los ViewModels lo reciben por una fábrica (ver SessionViewModel.Factory) en lugar de crearse sus piezas solos.
class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val store = SessionStore(
        dataStore = PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("session") },
        cipher = KeystoreTokenCipher(),
    )

    val sessionRepository = SessionRepository(store, ApiClient.createAuthApi())

    init {
        Offline.init(context, sessionRepository)
        ApiClient.init(sessionRepository)
        // lee la sesión guardada; mientras tanto el estado es Loading y la app muestra el arranque.
        appScope.launch { sessionRepository.load() }
        // sin sesión (se cerró o terminó) no debe quedar en el teléfono la lista de niños de la última sala.
        appScope.launch {
            sessionRepository.state.collect { state ->
                if (state is SessionState.Anonymous) runCatching { Offline.cache.clear() }
            }
        }
    }
}
