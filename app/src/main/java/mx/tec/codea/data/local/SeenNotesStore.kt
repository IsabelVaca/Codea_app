package mx.tec.codea.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.data.SessionState

// los avisos de casa que la docente ya marcó como vistos ("Ya lo vi"). se guardan solo en el teléfono,
// sin pasar por la API, y cada persona tiene su propia lista. solo se guardan ids, nunca el texto del aviso.
class SeenNotesStore(
    private val dataStore: DataStore<Preferences>,
    private val sessions: SessionRepository,
) {

    // se vuelve a calcular cuando cambia la sesión: al entrar otra persona, la lista es la suya.
    @OptIn(ExperimentalCoroutinesApi::class)
    fun seen(): Flow<Set<Int>> = sessions.state.flatMapLatest { state ->
        if (state is SessionState.Active) {
            val key = keyOf(state.session.userId)
            dataStore.data.map { prefs -> prefs[key].orEmpty().mapNotNull(String::toIntOrNull).toSet() }
        } else {
            flowOf(emptySet())
        }
    }

    suspend fun markSeen(noteId: Int) {
        val key = keyOf(sessions.current()?.userId ?: return)
        dataStore.edit { it[key] = it[key].orEmpty() + noteId.toString() }
    }

    // los avisos son solo del día: lo que ya no viene en la lista de hoy se olvida, para que esto no crezca.
    suspend fun keepOnly(todayIds: Set<Int>) {
        val key = keyOf(sessions.current()?.userId ?: return)
        dataStore.edit { prefs ->
            val kept = prefs[key].orEmpty().filter { it.toIntOrNull() in todayIds }.toSet()
            if (kept != prefs[key].orEmpty()) prefs[key] = kept
        }
    }

    private fun keyOf(userId: String) = stringSetPreferencesKey("seen_notes_$userId")
}

// lo guardado en el teléfono que no es sesión ni cola de asistencia. se crea una vez, en AppContainer.
object Local {

    lateinit var seenNotes: SeenNotesStore
        private set

    fun init(context: Context, sessions: SessionRepository) {
        seenNotes = SeenNotesStore(
            dataStore = PreferenceDataStoreFactory.create { context.applicationContext.preferencesDataStoreFile("seen_notes") },
            sessions = sessions,
        )
    }
}
