package mx.tec.codea.data

import android.content.Context
import android.content.SharedPreferences

// guarda en el teléfono qué avisos ya se vieron (por usuario). la API todavía no lleva
// la cuenta de "leídos", así que cambiar de teléfono los vuelve a marcar como nuevos.
object LeidosStore {

    private var prefs: SharedPreferences? = null

    // en memoria si nunca se llamó init (por ejemplo, en un preview).
    private val enMemoria = mutableMapOf<String, Set<String>>()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("leidos", Context.MODE_PRIVATE)
        }
    }

    fun get(clave: String): Set<String> =
        prefs?.getStringSet(clave, emptySet())?.toSet() ?: enMemoria[clave].orEmpty()

    fun agregar(clave: String, ids: Set<String>) {
        if (ids.isEmpty()) return
        val nuevos = get(clave) + ids
        enMemoria[clave] = nuevos
        // getStringSet devuelve un conjunto que no se debe modificar: se guarda una copia.
        prefs?.edit()?.putStringSet(clave, HashSet(nuevos))?.apply()
    }
}
