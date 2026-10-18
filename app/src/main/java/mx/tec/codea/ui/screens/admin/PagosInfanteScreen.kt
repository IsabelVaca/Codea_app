package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Payment
import mx.tec.codea.domain.PaymentLog
import mx.tec.codea.domain.PaymentMethod
import mx.tec.codea.domain.PaymentRules
import mx.tec.codea.domain.PaymentStatus
import mx.tec.codea.ui.theme.CodeaTheme

// la bitácora de pagos de un niño, para el admin: resumen, lista y las tres acciones
// (registrar un cargo o un pago, marcar como pagado y cancelar). nada se borra.
@Composable
fun PagosInfanteScreen(
    nombre: String,
    log: PaymentLog?,
    onRegistrar: (concept: String, amountCents: Int, dueDate: String?, method: PaymentMethod?) -> Unit,
    onMarcarPagado: (paymentId: Int, method: PaymentMethod) -> Unit,
    onCancelar: (paymentId: Int, motivo: String) -> Unit,
    onBack: () -> Unit,
) {
    var mostrarRegistro by rememberSaveable { mutableStateOf(false) }
    var pagando by remember { mutableStateOf<Payment?>(null) }
    var cancelando by remember { mutableStateOf<Payment?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 14.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(red = 244, green = 241, blue = 250))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF2A2140))
            }
            Text(
                text = "Pagos de $nombre",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A2140),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            if (log == null) {
                Text("Cargando pagos…", fontSize = 14.sp, color = Color(0xFF6B6183))
            } else {
                ResumenPagos(log)
                AccionButton(
                    texto = "Registrar un cargo o un pago",
                    bg = Color(0xFF6C4BF6),
                    fg = Color(0xFFFFFDFA),
                    onClick = { mostrarRegistro = true }
                )
                SeccionLabel("Bitácora")
                if (log.items.isEmpty()) {
                    InfoCard(titulo = "Sin movimientos", valor = "Todavía no hay pagos ni cargos")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        log.items.forEach { pago ->
                            PagoCard(
                                pago = pago,
                                onPagar = { pagando = pago },
                                onCancelar = { cancelando = pago },
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarRegistro) {
        RegistrarPagoDialog(
            onDismiss = { mostrarRegistro = false },
            onConfirm = { concepto, centavos, fecha, metodo ->
                onRegistrar(concepto, centavos, fecha, metodo)
                mostrarRegistro = false
            }
        )
    }
    pagando?.let { pago ->
        MetodoDialog(
            titulo = "¿Cómo se pagó «${pago.concept}»?",
            onDismiss = { pagando = null },
            onConfirm = { metodo ->
                onMarcarPagado(pago.id, metodo)
                pagando = null
            }
        )
    }
    cancelando?.let { pago ->
        CancelarPagoDialog(
            pago = pago,
            onDismiss = { cancelando = null },
            onConfirm = { motivo ->
                onCancelar(pago.id, motivo)
                cancelando = null
            }
        )
    }
}

@Composable
private fun ResumenPagos(log: PaymentLog) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        MontoCard("Pendiente", log.pendingCents, Color(0xFF5B4B8A), Modifier.weight(1f))
        MontoCard("Vencido", log.overdueCents, Color(0xFFC4432A), Modifier.weight(1f))
        MontoCard("Pagado", log.paidCents, Color(0xFF0F7A6C), Modifier.weight(1f))
    }
}

@Composable
private fun MontoCard(titulo: String, centavos: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Text(titulo.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFFA79BBF))
        Text(PaymentRules.formatMoney(centavos), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun PagoCard(pago: Payment, onPagar: () -> Unit, onCancelar: () -> Unit) {
    val cancelado = pago.status == PaymentStatus.CANCELLED
    val (etiqueta, fondo, texto) = when {
        cancelado -> Triple("Cancelado", Color(0xFFEFEDF3), Color(0xFF6B6183))
        pago.status == PaymentStatus.PAID -> Triple("Pagado", Color(0xFFE8FBF6), Color(0xFF0F7A6C))
        pago.isOverdue -> Triple("Vencido", Color(0xFFFFEDE7), Color(0xFFC4432A))
        else -> Triple("Pendiente", Color(0xFFF4F1FB), Color(0xFF5B4B8A))
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(if (cancelado) Color(0xFFF7F6F9) else Color.White)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pago.concept,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cancelado) Color(0xFF6B6183) else Color(0xFF2A2140)
                )
                Text(detalleDe(pago), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B4B8A))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    PaymentRules.formatMoney(pago.amountCents),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A2140)
                )
                Text(
                    etiqueta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = texto,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(fondo)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        pago.notes?.let { Text(it, fontSize = 12.sp, color = Color(0xFF6B6183)) }
        if (!cancelado) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pago.status == PaymentStatus.PENDING) {
                    Button(
                        onClick = onPagar,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8FBF6), contentColor = Color(0xFF0F7A6C)),
                        shape = RoundedCornerShape(999.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) { Text("Marcar pagado", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Button(
                    onClick = onCancelar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEDE7), contentColor = Color(0xFFC4432A)),
                    shape = RoundedCornerShape(999.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) { Text("Cancelar", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private fun detalleDe(pago: Payment): String = when (pago.status) {
    PaymentStatus.PAID -> "Pagado el ${pago.paidOn ?: "—"} · ${pago.method?.let(::nombreMetodo) ?: "—"}"
    else -> pago.dueDate?.let { "Vence el $it" } ?: "Sin fecha límite"
}

private fun nombreMetodo(metodo: PaymentMethod) = when (metodo) {
    PaymentMethod.CASH -> "Efectivo"
    PaymentMethod.TRANSFER -> "Transferencia"
    PaymentMethod.CARD -> "Tarjeta"
    PaymentMethod.OTHER -> "Otro"
}

@Composable
private fun MetodoChips(elegido: PaymentMethod?, onElegir: (PaymentMethod) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PaymentMethod.entries.forEach { metodo ->
            FilterChip(
                selected = metodo == elegido,
                onClick = { onElegir(metodo) },
                label = { Text(nombreMetodo(metodo)) }
            )
        }
    }
}

@Composable
private fun RegistrarPagoDialog(
    onDismiss: () -> Unit,
    onConfirm: (concept: String, amountCents: Int, dueDate: String?, method: PaymentMethod?) -> Unit,
) {
    var concepto by rememberSaveable { mutableStateOf("") }
    var monto by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var yaPagado by rememberSaveable { mutableStateOf(false) }
    var metodo by remember { mutableStateOf<PaymentMethod?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto en pesos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !yaPagado, onClick = { yaPagado = false }, label = { Text("Cargo pendiente") })
                    FilterChip(selected = yaPagado, onClick = { yaPagado = true }, label = { Text("Ya se pagó") })
                }
                if (yaPagado) {
                    MetodoChips(elegido = metodo, onElegir = { metodo = it })
                } else {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha límite (AAAA-MM-DD, opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let { Text(it, fontSize = 12.sp, color = Color(0xFFC4432A)) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val problema = PaymentRules.validate(concepto, monto, if (yaPagado) "" else fecha)
                    ?: if (yaPagado && metodo == null) "Elige cómo se pagó" else null
                if (problema != null) {
                    error = problema
                } else {
                    onConfirm(
                        concepto,
                        PaymentRules.parseAmountToCents(monto) ?: return@TextButton,
                        if (yaPagado) null else fecha.trim().ifBlank { null },
                        if (yaPagado) metodo else null,
                    )
                }
            }) { Text("Guardar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun MetodoDialog(titulo: String, onDismiss: () -> Unit, onConfirm: (PaymentMethod) -> Unit) {
    var metodo by remember { mutableStateOf<PaymentMethod?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = { MetodoChips(elegido = metodo, onElegir = { metodo = it }) },
        confirmButton = {
            TextButton(enabled = metodo != null, onClick = { metodo?.let(onConfirm) }) {
                Text("Marcar pagado", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Volver") } }
    )
}

@Composable
private fun CancelarPagoDialog(pago: Payment, onDismiss: () -> Unit, onConfirm: (motivo: String) -> Unit) {
    var motivo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cancelar «${pago.concept}»?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("No se borra: queda en la bitácora como cancelado y deja de contar en los totales.", fontSize = 13.sp)
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(motivo) }) {
                Text("Cancelar cargo", fontWeight = FontWeight.Bold, color = Color(0xFFC4432A))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Volver") } }
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PagosInfanteScreenPreview() {
    CodeaTheme {
        PagosInfanteScreen(
            nombre = "Mateo Iglesias",
            log = PaymentLog(
                items = listOf(
                    Payment(3, "Colegiatura de octubre", 350_000, PaymentStatus.PENDING, "2026-10-05", null, null, null, true),
                    Payment(2, "Material", 25_000, PaymentStatus.PAID, null, "2026-10-02", PaymentMethod.CASH, null, false),
                    Payment(1, "Inscripción", 100_000, PaymentStatus.CANCELLED, null, null, null, "Cancelado: cobro doble", false),
                ),
                pendingCents = 350_000,
                overdueCents = 350_000,
                paidCents = 25_000,
            ),
            onRegistrar = { _, _, _, _ -> },
            onMarcarPagado = { _, _ -> },
            onCancelar = { _, _ -> },
            onBack = {},
        )
    }
}
