package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentRulesTest {

    @Test
    fun parseAmount_acceptsPesosAndCents() {
        assertEquals(350_000, PaymentRules.parseAmountToCents("3500"))
        assertEquals(350_050, PaymentRules.parseAmountToCents("3500.5"))
        assertEquals(350_050, PaymentRules.parseAmountToCents("$3,500.50"))
        assertEquals(50, PaymentRules.parseAmountToCents(".50"))
        assertEquals(100, PaymentRules.parseAmountToCents(" 1 "))
    }

    @Test
    fun parseAmount_rejectsWrongValues() {
        assertNull(PaymentRules.parseAmountToCents(""))
        assertNull(PaymentRules.parseAmountToCents("0"))
        assertNull(PaymentRules.parseAmountToCents("-5"))
        assertNull(PaymentRules.parseAmountToCents("abc"))
        assertNull(PaymentRules.parseAmountToCents("1.234"))
        assertNull(PaymentRules.parseAmountToCents("1.2.3"))
        // more than one million pesos
        assertNull(PaymentRules.parseAmountToCents("1000001"))
        assertNotNull(PaymentRules.parseAmountToCents("1000000"))
    }

    @Test
    fun formatMoney_groupsThousands() {
        assertEquals("$3,500.00", PaymentRules.formatMoney(350_000))
        assertEquals("$0.05", PaymentRules.formatMoney(5))
        assertEquals("$1,000,000.00", PaymentRules.formatMoney(100_000_000))
        assertEquals("$12.30", PaymentRules.formatMoney(1230))
    }

    @Test
    fun isValidDate_checksTheCalendar() {
        assertTrue(PaymentRules.isValidDate("2026-10-31"))
        assertTrue(PaymentRules.isValidDate("2028-02-29"))
        assertFalse(PaymentRules.isValidDate("2026-02-29"))
        assertFalse(PaymentRules.isValidDate("2026-04-31"))
        assertFalse(PaymentRules.isValidDate("2026-13-01"))
        assertFalse(PaymentRules.isValidDate("31/10/2026"))
    }

    @Test
    fun validate_returnsTheFirstProblem() {
        assertEquals("Escribe el concepto", PaymentRules.validate(" ", "100", ""))
        assertNotNull(PaymentRules.validate("Colegiatura", "cien", ""))
        assertNotNull(PaymentRules.validate("Colegiatura", "100", "mañana"))
        assertNull(PaymentRules.validate("Colegiatura", "3500", ""))
        assertNull(PaymentRules.validate("Colegiatura", "3500", "2026-11-05"))
    }
}
