package mx.tec.codea.domain

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

// lo que trae un enlace de invitación: el correo con el que el admin dio de alta a la persona y su código de acceso.
data class InviteLink(val email: String, val code: String)

// las reglas de la invitación por WhatsApp. Kotlin puro para poder probarlo con JUnit.
object Invite {

    private val CODE = Regex("^[A-Z0-9]{4}-?[A-Z0-9]{4}$")
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    // lo que se escribe o se pega en el campo del código: solo letras y números, en mayúsculas y hasta 8.
    // así no estorban el guion, los espacios ni los caracteres invisibles que a veces trae un texto copiado de WhatsApp.
    fun cleanCode(raw: String): String =
        raw.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }.take(CODE_LENGTH)

    // el código limpio como se ve en pantalla: "YZ5E" -> "YZ5E", "YZ5EPE" -> "YZ5E-PE".
    fun formatCode(clean: String): String =
        if (clean.length <= 4) clean else clean.take(4) + "-" + clean.drop(4)

    // el correo sin espacios ni caracteres invisibles (de formato, como U+200B o U+2068) y en minúsculas.
    fun cleanEmail(raw: String): String =
        raw.filterNot { it.isWhitespace() || Character.getType(it) == Character.FORMAT.toInt() }.lowercase()

    const val CODE_LENGTH = 8

    // el teléfono como lo pide wa.me: solo dígitos y con lada de país. un número de 10 dígitos es de México (52).
    // null si no alcanza a ser un teléfono, y entonces el mensaje se manda con el selector de compartir.
    fun whatsAppPhone(phone: String?): String? {
        val digits = phone.orEmpty().filter { it.isDigit() }
        return when {
            digits.length == 10 -> "52$digits"
            // el formato viejo de celular mexicano llevaba un 1 después del 52; WhatsApp ya no lo usa.
            digits.length == 13 && digits.startsWith("521") -> "52" + digits.takeLast(10)
            digits.length in 11..15 -> digits
            else -> null
        }
    }

    // https://wa.me/5281...?text=... ; sin teléfono válido regresa null.
    fun whatsAppUrl(phone: String?, text: String): String? {
        val number = whatsAppPhone(phone) ?: return null
        // URLEncoder pone "+" por los espacios; WhatsApp los quiere como %20.
        val encoded = URLEncoder.encode(text, "UTF-8").replace("+", "%20")
        return "https://wa.me/$number?text=$encoded"
    }

    // lee el correo y el código de "https://servidor/invitar?email=…&code=…" o "codea://invitar?email=…&code=…".
    // cualquier otra cosa (otro sitio, otra ruta, un código o correo mal formados) regresa null.
    fun parseLink(raw: String?): InviteLink? {
        val uri = try {
            URI(raw?.trim().orEmpty())
        } catch (e: Exception) {
            return null
        }
        val isCustom = uri.scheme.equals("codea", ignoreCase = true) && uri.host.equals("invitar", ignoreCase = true)
        val isWeb = uri.scheme.equals("https", ignoreCase = true) && uri.path == "/invitar"
        if (!isCustom && !isWeb) return null

        val params = uri.rawQuery.orEmpty().split("&").mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0] to URLDecoder.decode(parts[1], "UTF-8") else null
        }.toMap()

        val email = params["email"]?.trim()?.lowercase().orEmpty()
        val code = params["code"]?.trim()?.uppercase().orEmpty()
        if (!EMAIL.matches(email) || !CODE.matches(code)) return null
        // con o sin guion, el código queda como XXXX-XXXX.
        return InviteLink(email, formatCode(cleanCode(code)))
    }
}
