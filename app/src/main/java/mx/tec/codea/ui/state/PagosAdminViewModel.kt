package mx.tec.codea.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.PaymentApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.PaymentLog
import mx.tec.codea.domain.PaymentMethod

// la bitácora de pagos del niño que el admin tiene abierto.
class PagosAdminViewModel : ViewModel() {

    private val repository = PaymentApiRepository()

    private val _log = MutableStateFlow<PaymentLog?>(null)
    val log: StateFlow<PaymentLog?> = _log.asStateFlow()

    private var childId: String? = null

    init {
        // cualquier escritura (pagar, cancelar, registrar) avisa por aquí y se vuelve a pedir la lista.
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
    }

    // llamar al abrir la pantalla de un niño; con otro niño se empieza sin lo que se veía antes.
    fun abrir(id: String) {
        if (childId != id) _log.value = null
        childId = id
        cargar()
    }

    private fun cargar() {
        val id = childId ?: return
        if (!ApiConfig.isAdmin) return
        viewModelScope.launch {
            try {
                _log.value = repository.log(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudieron cargar los pagos: ${e.toUserMessage()}")
            }
        }
    }

    // method = null: un cargo pendiente; con método: un pago que ya se recibió.
    fun registrar(
        concept: String,
        amountCents: Int,
        dueDate: String?,
        method: PaymentMethod?,
        onSuccess: () -> Unit = {},
    ) {
        val id = childId ?: return
        launchAction(onSuccess) { repository.create(id, concept, amountCents, dueDate, method) }
    }

    fun marcarPagado(paymentId: Int, method: PaymentMethod) {
        launchAction { repository.pay(paymentId, method) }
    }

    fun cancelar(paymentId: Int, motivo: String) {
        launchAction { repository.cancel(paymentId, motivo) }
    }
}
