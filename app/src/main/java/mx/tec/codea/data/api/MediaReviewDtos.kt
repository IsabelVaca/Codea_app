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

@Serializable
data class DecisionBody(
    val note: String? = null,
)
