package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ValidatePasswordConfirmationUseCaseTest {
    private lateinit var validatePasswordConfirmationUseCase: ValidatePasswordConfirmationUseCase

    @Before
    fun setUp() {
        validatePasswordConfirmationUseCase = ValidatePasswordConfirmationUseCase()
    }

    @Test
    fun `validatePasswordConfirmation should return Invalid when password confirmation is blank`() {
        val result = validatePasswordConfirmationUseCase("password", "")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validatePasswordConfirmation should return Invalid when passwords do not match`() {
        val result = validatePasswordConfirmationUseCase("password", "different")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONFIRMATION_DOES_NOT_MATCH.message), result)
    }

    @Test
    fun `validatePasswordConfirmation should return Valid when passwords match`() {
        val result = validatePasswordConfirmationUseCase("password", "password")
        assertEquals(ValidationResult.Valid("password"), result)
    }
}