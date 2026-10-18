package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResumenDelDiaTest {

    @Test
    fun `cada animo y cada comida se reconoce por su codigo de la API`() {
        for (animo in Animo.entries) assertEquals(animo, Animo.deApi(animo.api))
        for (comida in Comida.entries) assertEquals(comida, Comida.deApi(comida.api))
    }

    @Test
    fun `un codigo desconocido o vacio no rompe, se ignora`() {
        assertNull(Animo.deApi("EUFORICO"))
        assertNull(Animo.deApi(null))
        assertNull(Comida.deApi(""))
    }

    @Test
    fun `los codigos coinciden con los de la API`() {
        assertEquals(listOf("HAPPY", "CALM", "TIRED", "SAD", "UPSET"), Animo.entries.map { it.api })
        assertEquals(listOf("ALL", "MOST", "HALF", "LITTLE", "NONE"), Comida.entries.map { it.api })
    }

    @Test
    fun `panales y siesta se quedan dentro de sus limites`() {
        assertEquals(0, ResumenDelDiaReglas.panales(-3))
        assertEquals(20, ResumenDelDiaReglas.panales(99))
        assertEquals(0, ResumenDelDiaReglas.siesta(-15))
        assertEquals(360, ResumenDelDiaReglas.siesta(400))
    }

    @Test
    fun `la siesta se lee como horas y minutos`() {
        assertEquals("Sin siesta", ResumenDelDiaReglas.textoSiesta(0))
        assertEquals("45 min", ResumenDelDiaReglas.textoSiesta(45))
        assertEquals("1 h 30 min", ResumenDelDiaReglas.textoSiesta(90))
        assertEquals("2 h", ResumenDelDiaReglas.textoSiesta(120))
    }
}
