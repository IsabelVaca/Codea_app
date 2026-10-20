package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// las mismas reglas de longitud que la API (guarderia-api/app/core/rules.py, check_password); la mayúscula y
// el número solo los pide la app. las orillas son las que importan: RNF01 dice "mínimo 6", así que 5 no pasa y 6 sí.
class AuthValidatorTest {

    @Test
    fun login_pide_correo_y_contrasena() {
        assertEquals(AuthError.EmailRequired, AuthValidator.login("  ", "x"))
        assertEquals(AuthError.PasswordRequired, AuthValidator.login("a@b.mx", ""))
        assertNull(AuthValidator.login("a@b.mx", "x"))
    }

    @Test
    fun cinco_caracteres_no_bastan_y_seis_si() {
        assertEquals(AuthError.PasswordTooShort, AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "Abc12", "Abc12"))
        assertNull(AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "Abc123", "Abc123"))
    }

    @Test
    fun pide_al_menos_una_mayuscula_y_un_numero() {
        // larga y sin mayúscula ni número: no basta solo con el tamaño.
        assertEquals(AuthError.PasswordWeak, AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "una-clave-larga", "una-clave-larga"))
        // le falta el número.
        assertEquals(AuthError.PasswordWeak, AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "Una-Clave", "Una-Clave"))
        // le falta la mayúscula.
        assertEquals(AuthError.PasswordWeak, AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "una-clave-1", "una-clave-1"))
        // con las dos cosas sí pasa.
        assertNull(AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "Una-Clave1", "Una-Clave1"))
    }

    @Test
    fun ciento_veintiocho_caracteres_pasan_y_ciento_veintinueve_no() {
        assertNull(AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "A1" + "a".repeat(126), "A1" + "a".repeat(126)))
        assertEquals(
            AuthError.PasswordTooLong,
            AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "A1" + "a".repeat(127), "A1" + "a".repeat(127)),
        )
    }

    @Test
    fun la_contrasena_no_puede_ser_el_correo_aunque_cambien_las_mayusculas() {
        assertEquals(
            AuthError.PasswordIsEmail,
            AuthValidator.activate("laura1@oruga.test", "K7QX-M4PZ", "LAURA1@ORUGA.TEST", "LAURA1@ORUGA.TEST"),
        )
    }

    @Test
    fun las_dos_contrasenas_deben_coincidir() {
        assertEquals(
            AuthError.PasswordsDontMatch,
            AuthValidator.activate("a@b.mx", "K7QX-M4PZ", "Una-Clave1", "Una-Clave1-"),
        )
    }

    @Test
    fun activar_pide_el_codigo() {
        assertEquals(AuthError.CodeRequired, AuthValidator.activate("a@b.mx", " ", "Una-Clave1", "Una-Clave1"))
    }

    @Test
    fun cambiar_la_contrasena_pide_la_actual_y_aplica_las_mismas_reglas() {
        assertEquals(AuthError.CurrentPasswordRequired, AuthValidator.changePassword("", "Una-Clave1", "Una-Clave1", null))
        assertEquals(AuthError.PasswordTooShort, AuthValidator.changePassword("actual", "corta", "corta", null))
        assertNull(AuthValidator.changePassword("actual", "Una-Clave1", "Una-Clave1", "a@b.mx"))
    }

    @Test
    fun medidor_de_seguridad_sube_con_cada_regla_cumplida() {
        assertEquals(PasswordStrength.WEAK, AuthValidator.strength("abc")) // corta, sin mayúscula ni número
        assertEquals(PasswordStrength.WEAK, AuthValidator.strength("una-clave-larga")) // larga, sin mayúscula ni número
        assertEquals(PasswordStrength.MEDIUM, AuthValidator.strength("Una-Clave")) // larga y con mayúscula, sin número
        assertEquals(PasswordStrength.STRONG, AuthValidator.strength("Una-Clave1")) // las tres reglas
    }
}
