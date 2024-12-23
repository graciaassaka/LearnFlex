package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ValidateUsernameUseCaseTest {
    private lateinit var validateUsernameUseCase: ValidateUsernameUseCase

    @Before
    fun setUp() {
        validateUsernameUseCase = ValidateUsernameUseCase()
    }

    @Test
    fun `validateUsername should return Invalid when username is blank`() {
        val result = validateUsernameUseCase("")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username contains special characters`() {
        val result = validateUsernameUseCase("user@name")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_FORMAT.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username contains spaces`() {
        val result = validateUsernameUseCase("user name")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_FORMAT.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username length is less than 3`() {
        val result = validateUsernameUseCase("us")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_LENGTH.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username length is more than 20`() {
        val result = validateUsernameUseCase("usernameusernameusername")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_LENGTH.message), result)
    }

    @Test
    fun `validateUsername should return Valid when username meets all criteria`() {
        val result = validateUsernameUseCase("username")
        assertEquals(ValidationResult.Valid("username"), result)
    }
}