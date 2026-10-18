package mx.tec.codea.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import mx.tec.codea.domain.Session

// la sesión en disco, con DataStore. los dos tokens se guardan cifrados; el nombre y los roles no son secretos
// y sirven para dibujar la app aunque no haya internet al abrirla.
class SessionStore(
    private val dataStore: DataStore<Preferences>,
    private val cipher: TokenCipher,
) {

    suspend fun read(): Session? {
        val prefs = dataStore.data.first()
        val access = prefs[ACCESS]?.let(cipher::decrypt)
        val refresh = prefs[REFRESH]?.let(cipher::decrypt)
        val userId = prefs[USER_ID]
        if (access == null || refresh == null || userId == null) {
            // a medias o con una llave que ya no sirve: no se puede usar.
            if (prefs.asMap().isNotEmpty()) clear()
            return null
        }
        return Session(
            accessToken = access,
            refreshToken = refresh,
            userId = userId,
            name = prefs[NAME].orEmpty(),
            roles = prefs[ROLES].orEmpty(),
        )
    }

    suspend fun write(session: Session) {
        dataStore.edit {
            it[ACCESS] = cipher.encrypt(session.accessToken)
            it[REFRESH] = cipher.encrypt(session.refreshToken)
            it[USER_ID] = session.userId
            it[NAME] = session.name
            it[ROLES] = session.roles
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val ACCESS = stringPreferencesKey("access")
        val REFRESH = stringPreferencesKey("refresh")
        val USER_ID = stringPreferencesKey("user_id")
        val NAME = stringPreferencesKey("name")
        val ROLES = stringSetPreferencesKey("roles")
    }
}
