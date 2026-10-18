package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoMathTest {

    @Test
    fun `el mismo punto está a cero metros`() {
        assertEquals(0, GeoMath.distanceMeters(25.6516, -100.2896, 25.6516, -100.2896))
    }

    @Test
    fun `un grado de latitud son unos 111 kilometros`() {
        val meters = GeoMath.distanceMeters(25.0, -100.0, 26.0, -100.0)
        assertTrue("fueron $meters m", meters in 111_000..111_400)
    }

    @Test
    fun `la prueba del prototipo da unos 12 metros`() {
        // la escuela y el punto "a tiempo" del prototipo (0.000108 grados al norte).
        val meters = GeoMath.distanceMeters(25.651600, -100.289600, 25.651708, -100.289600)
        assertEquals(12, meters)
    }

    @Test
    fun `la distancia es la misma en los dos sentidos`() {
        val ida = GeoMath.distanceMeters(25.6516, -100.2896, 25.6642, -100.2896)
        val vuelta = GeoMath.distanceMeters(25.6642, -100.2896, 25.6516, -100.2896)
        assertEquals(ida, vuelta)
    }
}
