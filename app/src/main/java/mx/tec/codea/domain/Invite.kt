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
        val clean = code.replace("-", "")
        return InviteLink(email, "${clean.take(4)}-${clean.drop(4)}")
    }
}
