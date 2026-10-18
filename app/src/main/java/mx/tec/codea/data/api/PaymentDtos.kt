package mx.tec.codea.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- pagos ----------

@Serializable
data class PaymentDto(
    val id: Int,
    val concept: String,
    @SerialName("amount_cents") val amountCents: Int,
    val status: String,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("paid_on") val paidOn: String? = null,
    val method: String? = null,
    val notes: String? = null,
    @SerialName("is_overdue") val isOverdue: Boolean = false,
)

@Serializable
data class PaymentListDto(
    val items: List<PaymentDto> = emptyList(),
    @SerialName("pending_cents") val pendingCents: Int = 0,
    @SerialName("overdue_cents") val overdueCents: Int = 0,
    @SerialName("paid_cents") val paidCents: Int = 0,
)

@Serializable
data class PaymentBody(
    val concept: String,
    @SerialName("amount_cents") val amountCents: Int,
    val status: String,
    @SerialName("due_date") val dueDate: String? = null,
    val method: String? = null,
    val notes: String? = null,
)

@Serializable
data class PayBody(val method: String)

@Serializable
data class CancelBody(val reason: String? = null)
