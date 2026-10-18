package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InfanteValidatorTest {

    // domingo 18 de octubre de 2026.
    private val hoy = Fecha(2026, 10, 18)

    @Test
    fun `una fecha real se convierte al formato de la API`() {
        assertEquals("2022-03-14", InfanteValidator.parseNacimiento("14/03/2022", hoy))
    }

    @Test
    fun `acepta dia y mes de un digito, guiones y espacios`() {
        assertEquals("2022-03-04", InfanteValidator.parseNacimiento("4/3/2022", hoy))
        assertEquals("2022-03-04", InfanteValidator.parseNacimiento(" 04-03-2022 ", hoy))
    }

    @Test
    fun `no acepta fechas que no existen`() {
        assertNull(InfanteValidator.parseNacimiento("31/04/2022", hoy))
        assertNull(InfanteValidator.parseNacimiento("30/02/2022", hoy))
        assertNull(InfanteValidator.parseNacimiento("00/05/2022", hoy))
        assertNull(InfanteValidator.parseNacimiento("10/13/2022", hoy))
    }

    @Test
    fun `el 29 de febrero solo existe en anio bisiesto`() {
        assertEquals("2020-02-29", InfanteValidator.parseNacimiento("29/02/2020", hoy))
        assertNull(InfanteValidator.parseNacimiento("29/02/2021", hoy))
    }

    @Test
    fun `no acepta fechas futuras, pero hoy si`() {
        assertEquals("2026-10-18", InfanteValidator.parseNacimiento("18/10/2026", hoy))
        assertNull(InfanteValidator.parseNacimiento("19/10/2026", hoy))
        assertNull(InfanteValidator.parseNacimiento("01/01/2027", hoy))
    }

    @Test
    fun `solo de 0 a 12 anios cumplidos`() {
        // el 18/10/2013 cumple 13 hoy: ya no entra. un dia despues, todavia tiene 12.
        assertNull(InfanteValidator.parseNacimiento("18/10/2013", hoy))
        assertEquals("2013-10-19", InfanteValidator.parseNacimiento("19/10/2013", hoy))
    }

    @Test
    fun `el anio va completo y todo debe ser numerico`() {
        assertNull(InfanteValidator.parseNacimiento("14/03/22", hoy))
        assertNull(InfanteValidator.parseNacimiento("14/03", hoy))
        assertNull(InfanteValidator.parseNacimiento("catorce/03/2022", hoy))
        assertNull(InfanteValidator.parseNacimiento("", hoy))
    }

    @Test
    fun `el formulario necesita nombre y fecha`() {
        assertTrue(InfanteValidator.isValid("Luna", "14/03/2022", hoy))
        assertEquals(InfanteError.NombreVacio, InfanteValidator.validateNombre("  "))
        assertEquals(InfanteError.NacimientoInvalido, InfanteValidator.validateNacimiento("mañana", hoy))
        assertEquals(false, InfanteValidator.isValid("", "14/03/2022", hoy))
        assertEquals(false, InfanteValidator.isValid("Luna", "", hoy))
    }
}
