package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ValidateEmailUseCaseTest {
    private lateinit var validateEmailUseCase: ValidateEmailUseCase

    @Before
    fun setUp() {
        validateEmailUseCase = ValidateEmailUseCase()
    }

    @Test
    fun `validateEmail should return Invalid when email is blank`() {
        val result = validateEmailUseCase("")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validateEmail should return Invalid when email format is incorrect`() {
        val result = validateEmailUseCase("invalid-email")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMAIL_FORMAT.message), result)
    }

    @Test
    fun `validateEmail should return Valid when email format is correct`() {
        val result = validateEmailUseCase("test@example.com")
        assertEquals(ValidationResult.Valid("test@example.com"), result)
    }
}