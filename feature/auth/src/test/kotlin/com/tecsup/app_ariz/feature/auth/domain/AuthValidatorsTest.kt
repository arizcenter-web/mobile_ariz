package com.tecsup.app_ariz.feature.auth.domain

import com.tecsup.app_ariz.feature.auth.domain.validation.AuthValidators
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorsTest {

    @Test
    fun `email vacio es requerido`() {
        assertEquals(FieldError.REQUIRED, AuthValidators.validateEmail("  "))
    }

    @Test
    fun `email sin arroba es invalido`() {
        assertEquals(FieldError.INVALID_EMAIL, AuthValidators.validateEmail("ana.correo.com"))
    }

    @Test
    fun `email valido con espacios alrededor pasa`() {
        assertNull(AuthValidators.validateEmail("  ana@correo.com "))
    }

    @Test
    fun `telefono fuera de 7 a 20 caracteres es invalido`() {
        assertEquals(FieldError.INVALID_PHONE, AuthValidators.validatePhone("123456"))
        assertEquals(FieldError.INVALID_PHONE, AuthValidators.validatePhone("1".repeat(21)))
        assertNull(AuthValidators.validatePhone("987654321"))
    }

    @Test
    fun `dni debe tener exactamente 8 digitos`() {
        assertEquals(FieldError.REQUIRED, AuthValidators.validateDni(""))
        assertEquals(FieldError.INVALID_DNI, AuthValidators.validateDni("1234567"))
        assertEquals(FieldError.INVALID_DNI, AuthValidators.validateDni("1234567a"))
        assertNull(AuthValidators.validateDni("12345678"))
    }

    @Test
    fun `ruc debe tener exactamente 11 digitos`() {
        assertEquals(FieldError.INVALID_RUC, AuthValidators.validateRuc("2012345678"))
        assertNull(AuthValidators.validateRuc("20123456789"))
    }

    @Test
    fun `password debe tener letras numeros y entre 8 y 72 caracteres`() {
        assertEquals(FieldError.REQUIRED, AuthValidators.validateNewPassword(""))
        assertEquals(FieldError.WEAK_PASSWORD, AuthValidators.validateNewPassword("abc123"))
        assertEquals(FieldError.WEAK_PASSWORD, AuthValidators.validateNewPassword("sololetras"))
        assertEquals(FieldError.WEAK_PASSWORD, AuthValidators.validateNewPassword("12345678"))
        assertEquals(FieldError.WEAK_PASSWORD, AuthValidators.validateNewPassword("a1".repeat(37)))
        assertNull(AuthValidators.validateNewPassword("clave1234"))
    }

    @Test
    fun `confirmacion debe coincidir con la contraseña`() {
        assertEquals(FieldError.PASSWORDS_MISMATCH, AuthValidators.validatePasswordConfirmation("clave1234", "clave12345"))
        assertNull(AuthValidators.validatePasswordConfirmation("clave1234", "clave1234"))
    }

    @Test
    fun `terminos no aceptados es error`() {
        assertEquals(FieldError.TERMS_NOT_ACCEPTED, AuthValidators.validateTerms(false))
        assertNull(AuthValidators.validateTerms(true))
    }
}
