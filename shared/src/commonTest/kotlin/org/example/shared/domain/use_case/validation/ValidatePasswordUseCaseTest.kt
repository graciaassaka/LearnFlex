package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ValidatePasswordUseCaseTest {

    private lateinit var validatePasswordUseCase: ValidatePasswordUseCase

    @Before
    fun setUp() {
        validatePasswordUseCase = ValidatePasswordUseCase()
    }

    @Test
    fun `validatePassword should return Invalid when password is blank`() {
        val result = validatePasswordUseCase("")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password contains spaces`() {
        val result = validatePasswordUseCase("pass word")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONTAINS_SPACES.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no letters`() {
        val result = validatePasswordUseCase("12345678")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LETTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no numbers`() {
        val result = validatePasswordUseCase("Password")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_NUMBER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no uppercase letters`() {
        val result = validatePasswordUseCase("password1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_UPPERCASE_LETTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no lowercase letters`() {
        val result = validatePasswordUseCase("PASSWORD1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LOWER_CASE_LETTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no special characters`() {
        val result = validatePasswordUseCase("Password1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_SPECIAL_CHARACTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password length is less than 8`() {
        val result = validatePasswordUseCase("P@ss1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_LENGTH.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password length is more than 20`() {
        val result = validatePasswordUseCase("P@ssword123456789012345")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_LENGTH.message), result)
    }

    @Test
    fun `validatePassword should return Valid when password meets all criteria`() {
        val result = validatePasswordUseCase("P@ssword1")
        assertEquals(ValidationResult.Valid("P@ssword1"), result)
    }
}