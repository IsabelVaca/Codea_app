package mx.tec.codea.domain

enum class PaymentStatus { PENDING, PAID, CANCELLED }

enum class PaymentMethod { CASH, TRANSFER, CARD, OTHER }

// one line of the payment log of a child. the money is in cents, always Mexican pesos.
data class Payment(
    val id: Int,
    val concept: String,
    val amountCents: Int,
    val status: PaymentStatus,
    val dueDate: String?,
    val paidOn: String?,
    val method: PaymentMethod?,
    val notes: String?,
    val isOverdue: Boolean,
)

// the payments of a child and the totals the API calculated (cancelled lines do not count).
data class PaymentLog(
    val items: List<Payment>,
    val pendingCents: Int,
    val overdueCents: Int,
    val paidCents: Int,
)
