package mx.tec.codea.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- revisión de solicitudes de borrado de fotos (solo admin) ----------

@Serializable
data class DeletionReviewDto(
    val id: Int,
    @SerialName("media_id") val mediaId: Int,
    val status: String,
    val reason: String? = null,
    @SerialName("requested_by_name") val requestedByName: String = "",
    @SerialName("child_names") val childNames: List<String> = emptyList(),
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("created_at") val createdAt: String = "",
)

// ---------- centro (solo admin) ----------

@Serializable
data class CenterUpdateBody(
    // solo se manda lo que cambia: lo que queda en null no se serializa (encodeDefaults está apagado).
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("check_in_radius_m") val checkInRadiusM: Int? = null,
)

@Serializable
data class DecisionBody(
    val note: String? = null,
)
