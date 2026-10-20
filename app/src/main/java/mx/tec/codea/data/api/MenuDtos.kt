package mx.tec.codea.data.api

import kotlinx.serialization.Serializable

// ---------- menú del día ----------

@Serializable
data class MenuDto(
    val date: String,
    val breakfast: String? = null,
    val lunch: String? = null,
    val snack: String? = null,
)

// lo que manda el admin: una comida vacía no se manda.
@Serializable
data class MenuBody(
    val breakfast: String? = null,
    val lunch: String? = null,
    val snack: String? = null,
)
