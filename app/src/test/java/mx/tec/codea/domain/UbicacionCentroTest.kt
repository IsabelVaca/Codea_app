package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UbicacionCentroTest {

    @Test
    fun `el radio dentro de los limites de la API se acepta`() {
        assertEquals(60, UbicacionCentro.radioDesdeTexto("60"))
        assertEquals(10, UbicacionCentro.radioDesdeTexto("10"))
        assertEquals(1000, UbicacionCentro.radioDesdeTexto(" 1000 "))
    }

    @Test
    fun `el radio fuera de los limites no se puede guardar`() {
        assertNull(UbicacionCentro.radioDesdeTexto("9"))
        assertNull(UbicacionCentro.radioDesdeTexto("1001"))
        assertNull(UbicacionCentro.radioDesdeTexto("0"))
    }

    @Test
    fun `un radio que no es numero no se puede guardar`() {
        assertNull(UbicacionCentro.radioDesdeTexto(""))
        assertNull(UbicacionCentro.radioDesdeTexto("abc"))
        assertNull(UbicacionCentro.radioDesdeTexto("12.5"))
    }
}
