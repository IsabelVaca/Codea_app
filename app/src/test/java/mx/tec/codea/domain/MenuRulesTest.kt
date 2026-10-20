package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuRulesTest {

    @Test
    fun `el lunes de cualquier dia de la semana es el mismo`() {
        // el 14 de octubre de 2026 es miércoles.
        for (dia in listOf("2026-10-12", "2026-10-14", "2026-10-16", "2026-10-18")) {
            assertEquals("2026-10-12", MenuRules.lunesDe(dia))
        }
        // el domingo pertenece a la semana que termina, no a la que empieza.
        assertEquals("2026-10-12", MenuRules.lunesDe("2026-10-18"))
        assertEquals("2026-10-19", MenuRules.lunesDe("2026-10-19"))
    }

    @Test
    fun `sumar dias cruza el cambio de mes y de anio`() {
        assertEquals("2026-11-02", MenuRules.sumarDias("2026-10-26", 7))
        assertEquals("2027-01-01", MenuRules.sumarDias("2026-12-28", 4))
        assertEquals("2026-10-05", MenuRules.sumarDias("2026-10-12", -7))
    }

    @Test
    fun `los dias habiles son de lunes a viernes`() {
        assertEquals(
            listOf("2026-10-12", "2026-10-13", "2026-10-14", "2026-10-15", "2026-10-16"),
            MenuRules.diasHabiles("2026-10-12"),
        )
        assertEquals("2026-11-02", MenuRules.diasHabiles("2026-10-29").last())
    }

    @Test
    fun `un menu necesita al menos una comida`() {
        assertFalse(MenuRules.puedeGuardar(MenuDelDia()))
        assertFalse(MenuRules.puedeGuardar(MenuDelDia(desayuno = "   ", comida = "")))
        assertTrue(MenuRules.puedeGuardar(MenuDelDia(colacion = "Manzana")))
    }

    @Test
    fun `cada comida tiene un largo maximo`() {
        assertTrue(MenuRules.puedeGuardar(MenuDelDia(comida = "a".repeat(MenuRules.MAX_CARACTERES))))
        assertFalse(MenuRules.puedeGuardar(MenuDelDia(comida = "a".repeat(MenuRules.MAX_CARACTERES + 1))))
    }

    @Test
    fun `solo se muestran las comidas con texto y en orden`() {
        val menu = MenuDelDia(desayuno = "Avena", comida = " ", colacion = "Gelatina")
        assertEquals(listOf("Desayuno" to "Avena", "Colación" to "Gelatina"), menu.comidas)
        assertTrue(MenuDelDia().vacio)
        assertFalse(menu.vacio)
    }
}
