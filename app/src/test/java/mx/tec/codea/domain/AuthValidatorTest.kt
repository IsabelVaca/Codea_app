package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// las mismas reglas que la API (guarderia-api/app/core/rules.py, check_password). las orillas son las que importan:
// RNF01 dice "mayores a 8 caracteres", así que 8 no pasa y 9 sí.
class AuthValidatorTest {

    @Test
    fun login_pide_correo_y_contrasena() {
        assertEquals(AuthError.EmailRequired, AuthValidator.login("  ", "x"))
        assertEquals(AuthError.PasswordRequired, AuthValidator.login("a@b.mx", ""))
        assertNull(AuthValidator.login("a@b.mx", "x"))
    }

    @Test
    fun ocho_caracteres_no_bastan_y_nueve_si() {
        assertEquals(AuthError.PasswordTooShort, AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "a".repeat(8), "a".repeat(8)))
        assertNull(AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "a".repeat(9), "a".repeat(9)))
    }

    @Test
    fun ciento_veintiocho_caracteres_pasan_y_ciento_veintinueve_no() {
        assertNull(AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "a".repeat(128), "a".repeat(128)))
        assertEquals(
            AuthError.PasswordTooLong,
            AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "a".repeat(129), "a".repeat(129)),
        )
    }

    @Test
    fun la_contrasena_no_puede_ser_el_correo_aunque_cambien_las_mayusculas() {
        assertEquals(
            AuthError.PasswordIsEmail,
            AuthValidator.activate("Laura@Oruga.test", "K7QX-M4PZ", "laura@oruga.test", "laura@oruga.test"),
        )
    }

    @Test
    fun las_dos_contrasenas_deben_coincidir() {
        assertEquals(
            AuthError.PasswordsDontMatch,
            AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "una-clave-larga", "una-clave-larga-"),
        )
    }

    @Test
    fun activar_pide_el_codigo() {
        assertEquals(AuthError.CodeRequired, AuthValidator.activate("a@b.mx", " ", "una-clave-larga", "una-clave-larga"))
    }

    @Test
    fun cambiar_la_contrasena_pide_la_actual_y_aplica_las_mismas_reglas() {
        assertEquals(AuthError.CurrentPasswordRequired, AuthValidator.changePassword("", "una-clave-larga", "una-clave-larga", null))
        assertEquals(AuthError.PasswordTooShort, AuthValidator.changePassword("actual", "corta", "corta", null))
        assertNull(AuthValidator.changePassword("actual", "una-clave-larga", "una-clave-larga", "a@b.mx"))
    }
}
