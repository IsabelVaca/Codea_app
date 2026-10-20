package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InviteTest {

    @Test
    fun phone_tenDigitsAreMexican() {
        assertEquals("528112345678", Invite.whatsAppPhone("81 1234 5678"))
        assertEquals("528112345678", Invite.whatsAppPhone("(81) 1234-5678"))
    }

    @Test
    fun phone_keepsTheCountryCode() {
        assertEquals("528112345678", Invite.whatsAppPhone("+52 81 1234 5678"))
        assertEquals("14155550123", Invite.whatsAppPhone("+1 415 555 0123"))
    }

    @Test
    fun phone_dropsTheOldMexicanMobileOne() {
        assertEquals("528112345678", Invite.whatsAppPhone("+52 1 81 1234 5678"))
    }

    @Test
    fun phone_tooShortOrMissing_isNull() {
        assertNull(Invite.whatsAppPhone(null))
        assertNull(Invite.whatsAppPhone(""))
        assertNull(Invite.whatsAppPhone("123 456"))
    }

    @Test
    fun whatsAppUrl_encodesTheText() {
        val url = Invite.whatsAppUrl("81 1234 5678", "Hola María: toca https://x.mx/i?a=1&b=2")
        assertEquals(
            "https://wa.me/528112345678?text=Hola%20Mar%C3%ADa%3A%20toca%20https%3A%2F%2Fx.mx%2Fi%3Fa%3D1%26b%3D2",
            url,
        )
    }

    @Test
    fun whatsAppUrl_withoutPhone_isNull() {
        assertNull(Invite.whatsAppUrl("12", "hola"))
    }

    @Test
    fun link_https() {
        val link = Invite.parseLink("https://api.codea.mx/invitar?email=Marisol%40oruga.test&code=k7qx-m4pz")
        assertEquals(InviteLink("marisol@oruga.test", "K7QX-M4PZ"), link)
    }

    @Test
    fun link_customScheme() {
        val link = Invite.parseLink("codea://invitar?email=marisol@oruga.test&code=K7QXM4PZ")
        assertEquals(InviteLink("marisol@oruga.test", "K7QX-M4PZ"), link)
    }

    @Test
    fun link_otherSitesAndPathsAreIgnored() {
        assertNull(Invite.parseLink("http://api.codea.mx/invitar?email=a@b.co&code=K7QX-M4PZ")) // sin https
        assertNull(Invite.parseLink("https://api.codea.mx/otra?email=a@b.co&code=K7QX-M4PZ"))
        assertNull(Invite.parseLink("codea://otra?email=a@b.co&code=K7QX-M4PZ"))
        assertNull(Invite.parseLink("mailto:a@b.co"))
        assertNull(Invite.parseLink(null))
        assertNull(Invite.parseLink("no es un enlace"))
    }

    @Test
    fun link_badEmailOrCode_isNull() {
        assertNull(Invite.parseLink("codea://invitar?email=sin-arroba&code=K7QX-M4PZ"))
        assertNull(Invite.parseLink("codea://invitar?email=a@b.co&code=corto"))
        assertNull(Invite.parseLink("codea://invitar?email=a@b.co&code=K7QX-M4P%3Cscript%3E"))
        assertNull(Invite.parseLink("codea://invitar?email=a@b.co"))
    }

    @Test
    fun cleanCode_keepsOnlyLettersAndNumbers() {
        assertEquals("YZ5EPEKV", Invite.cleanCode("YZ5E-PEKV"))
        assertEquals("YZ5EPEKV", Invite.cleanCode(" yz5e pekv "))
        // copiado de WhatsApp: aislantes de dirección, espacio de ancho cero y guion largo.
        assertEquals("YZ5EPEKV", Invite.cleanCode("\u2068YZ5E\u2013PEKV\u200B\u2069"))
        assertEquals("YZ5EPEKV", Invite.cleanCode("YZ5EPEKVXX"))
    }

    @Test
    fun formatCode_putsTheDashAfterFour() {
        assertEquals("", Invite.formatCode(""))
        assertEquals("YZ5E", Invite.formatCode("YZ5E"))
        assertEquals("YZ5E-P", Invite.formatCode("YZ5EP"))
        assertEquals("YZ5E-PEKV", Invite.formatCode("YZ5EPEKV"))
    }

    @Test
    fun cleanEmail_dropsSpacesInvisiblesAndCase() {
        assertEquals("paola@correo.mx", Invite.cleanEmail(" Paola@Correo.mx "))
        assertEquals("paola@correo.mx", Invite.cleanEmail("\u2068paola@correo.mx\u2069\u200B"))
    }
}
