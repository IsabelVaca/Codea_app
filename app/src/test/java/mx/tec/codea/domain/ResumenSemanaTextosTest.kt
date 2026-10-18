package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenSemanaTextosTest {

    @Test
    fun `lo mas frecuente va primero y el uno no lleva multiplicador`() {
        assertEquals("Feliz ×2 · Tranquilo", ResumenSemanaTextos.animo(mapOf("CALM" to 1, "HAPPY" to 2)))
    }

    @Test
    fun `si empatan se respeta el orden de la lista`() {
        assertEquals("Feliz · Cansado", ResumenSemanaTextos.animo(mapOf("TIRED" to 1, "HAPPY" to 1)))
        assertEquals("Todo · Poco", ResumenSemanaTextos.comidas(mapOf("LITTLE" to 1, "ALL" to 1)))
    }

    @Test
    fun `sin registros se muestra una raya`() {
        assertEquals("—", ResumenSemanaTextos.animo(emptyMap()))
        assertEquals("—", ResumenSemanaTextos.comidas(mapOf("ALL" to 0)))
        assertEquals("—", ResumenSemanaTextos.siesta(null))
    }

    @Test
    fun `un valor desconocido de la API se ignora en lugar de romper`() {
        assertEquals("Feliz", ResumenSemanaTextos.animo(mapOf("HAPPY" to 1, "NUEVO" to 5)))
    }

    @Test
    fun `la siesta lleva minutos y el estado del dia se traduce`() {
        assertEquals("75 min", ResumenSemanaTextos.siesta(75))
        assertEquals(EstadoDia.ASISTIO, ResumenSemanaTextos.estado("ATTENDED"))
        assertEquals(EstadoDia.FALTO, ResumenSemanaTextos.estado("ABSENT"))
        assertEquals(EstadoDia.SIN_REGISTRO, ResumenSemanaTextos.estado("NO_RECORD"))
        assertEquals(EstadoDia.SIN_REGISTRO, ResumenSemanaTextos.estado("OTRA_COSA"))
    }
}
