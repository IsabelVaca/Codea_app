package mx.tec.codea.domain

// alguien que puede recoger al niño: el tutor o una persona autorizada.
data class Contact(
    val name: String,
    val relationship: String?,
    // null cuando la familia no dejó teléfono: entonces no hay botón de llamar.
    val phone: String?,
)

// lo que la ficha del niño pide al abrirse: alergias, edad, tutores y personas autorizadas.
data class ChildProfile(
    val allergies: String?,
    // "2 años 4 meses"; null si la fecha de nacimiento no se conoce.
    val ageText: String?,
    val guardians: List<Contact>,
    val pickups: List<Contact>,
    // condiciones médicas, intolerancias, medicamentos y otras indicaciones que escribió la familia.
    val health: ChildHealth = ChildHealth(),
)

object ChildProfileRules {

    // "2 años 4 meses", "1 año", "8 meses", "Menos de 1 mes". null si falta o no se entiende la fecha ("aaaa-mm-dd").
    fun ageText(birthDate: String?, today: Fecha): String? {
        val parts = birthDate?.split("-") ?: return null
        val year = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return null
        val day = parts.getOrNull(2)?.toIntOrNull() ?: return null
        if (month !in 1..12 || day !in 1..31) return null

        var months = (today.anio - year) * 12 + (today.mes - month)
        if (today.dia < day) months--
        if (months < 0) return null

        val years = months / 12
        val rest = months % 12
        val yearsText = when (years) {
            0 -> null
            1 -> "1 año"
            else -> "$years años"
        }
        val monthsText = when (rest) {
            0 -> null
            1 -> "1 mes"
            else -> "$rest meses"
        }
        return when {
            yearsText != null && monthsText != null -> "$yearsText $monthsText"
            yearsText != null -> yearsText
            monthsText != null -> monthsText
            else -> "Menos de 1 mes"
        }
    }

    // los teléfonos se marcan sin espacios ni guiones; si no queda ningún dígito no hay a quién llamar.
    fun dialable(phone: String?): String? {
        val kept = phone?.filter { it.isDigit() || it == '+' }.orEmpty()
        return kept.takeIf { kept.count { it.isDigit() } >= 7 }
    }
}
