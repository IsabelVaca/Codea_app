package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.CancelBody
import mx.tec.codea.data.api.PayBody
import mx.tec.codea.data.api.PaymentBody
import mx.tec.codea.data.api.PaymentDto
import mx.tec.codea.domain.Payment
import mx.tec.codea.domain.PaymentLog
import mx.tec.codea.domain.PaymentMethod
import mx.tec.codea.domain.PaymentStatus

// la bitácora de pagos de un niño. el admin escribe; el papá solo lee.
class PaymentApiRepository {

    private val api get() = ApiClient.api

    suspend fun log(childId: String): PaymentLog {
        val dto = api.payments(childId.toInt())
        return PaymentLog(
            items = dto.items.map { it.toPayment() },
            pendingCents = dto.pendingCents,
            overdueCents = dto.overdueCents,
            paidCents = dto.paidCents,
        )
    }

    // un cargo por cobrar (method = null) o un pago que ya se recibió (method con valor).
    suspend fun create(childId: String, concept: String, amountCents: Int, dueDate: String?, method: PaymentMethod?) {
        api.createPayment(
            childId.toInt(),
            PaymentBody(
                concept = concept.trim(),
                amountCents = amountCents,
                status = if (method == null) PaymentStatus.PENDING.name else PaymentStatus.PAID.name,
                dueDate = dueDate?.trim()?.ifBlank { null },
                method = method?.name,
            ),
        )
    }

    suspend fun pay(paymentId: Int, method: PaymentMethod) {
        api.payPayment(paymentId, PayBody(method.name))
    }

    suspend fun cancel(paymentId: Int, reason: String) {
        api.cancelPayment(paymentId, CancelBody(reason.trim().ifBlank { null }))
    }
}

private fun PaymentDto.toPayment() = Payment(
    id = id,
    concept = concept,
    amountCents = amountCents,
    status = runCatching { PaymentStatus.valueOf(status) }.getOrDefault(PaymentStatus.PENDING),
    dueDate = dueDate,
    paidOn = paidOn,
    method = method?.let { runCatching { PaymentMethod.valueOf(it) }.getOrNull() },
    notes = notes,
    isOverdue = isOverdue,
)
