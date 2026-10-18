package mx.tec.codea.domain

// pure rules of the payment form and the money format, so they can be tested without Android.
object PaymentRules {

    private const val MAX_CENTS = 100_000_000 // one million pesos, the same limit as the API

    // "350", "350.5", "1,200.50", "$1200" -> cents. null if it is not a valid amount.
    fun parseAmountToCents(text: String): Int? {
        val clean = text.trim().removePrefix("$").replace(",", "").replace(" ", "")
        if (clean.isEmpty()) return null
        val parts = clean.split(".")
        if (parts.size > 2) return null
        val pesos = parts[0].ifEmpty { "0" }
        val cents = parts.getOrNull(1).orEmpty()
        if (!pesos.all { it.isDigit() } || !cents.all { it.isDigit() } || cents.length > 2) return null
        val total = pesos.toLongOrNull()?.times(100)?.plus(cents.padEnd(2, '0').toLong()) ?: return null
        return if (total in 1..MAX_CENTS) total.toInt() else null
    }

    // 350000 -> "$3,500.00"
    fun formatMoney(cents: Int): String {
        val pesos = cents / 100
        val rest = cents % 100
        val grouped = pesos.toString().reversed().chunked(3).joinToString(",").reversed()
        return "$$grouped.${rest.toString().padStart(2, '0')}"
    }

    // "2026-10-31". the day must exist (no 2026-02-30).
    fun isValidDate(text: String): Boolean {
        val m = Regex("""(\d{4})-(\d{2})-(\d{2})""").matchEntire(text.trim()) ?: return false
        val (year, month, day) = m.destructured.toList().map { it.toInt() }
        if (month !in 1..12 || day < 1) return false
        val leap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        val days = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        return day <= days[month - 1]
    }

    // the error to show under the form, or null if it can be sent.
    fun validate(concept: String, amountText: String, dueDate: String): String? = when {
        concept.isBlank() -> "Escribe el concepto"
        concept.trim().length > 120 -> "El concepto es demasiado largo"
        parseAmountToCents(amountText) == null -> "Escribe un monto válido, por ejemplo 3500 o 3500.50"
        dueDate.isNotBlank() && !isValidDate(dueDate) -> "La fecha debe ser AAAA-MM-DD"
        else -> null
    }
}
