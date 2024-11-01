package org.example.shared.util.validation

import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.InvalidInputMessage
import org.example.shared.presentation.util.validation.ValidationResult
import org.junit.Test
import kotlin.test.assertEquals

class InputValidatorTest {

    @Test
    fun `validateEmail should return Invalid when email is blank`() {
        val result = InputValidator.validateEmail("")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validateEmail should return Invalid when email format is incorrect`() {
        val result = InputValidator.validateEmail("invalid-email")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMAIL_FORMAT.message), result)
    }

    @Test
    fun `validateEmail should return Valid when email format is correct`() {
        val result = InputValidator.validateEmail("test@example.com")
        assertEquals(ValidationResult.Valid("test@example.com"), result)
    }

    @Test
    fun `validatePassword should return Invalid when password is blank`() {
        val result = InputValidator.validatePassword("")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password contains spaces`() {
        val result = InputValidator.validatePassword("pass word")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONTAINS_SPACES.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no letters`() {
        val result = InputValidator.validatePassword("12345678")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LETTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no numbers`() {
        val result = InputValidator.validatePassword("Password")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_NUMBER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no uppercase letters`() {
        val result = InputValidator.validatePassword("password1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_UPPERCASE_LETTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no lowercase letters`() {
        val result = InputValidator.validatePassword("PASSWORD1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LOWER_CASE_LETTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password has no special characters`() {
        val result = InputValidator.validatePassword("Password1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_SPECIAL_CHARACTER.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password length is less than 8`() {
        val result = InputValidator.validatePassword("P@ss1")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_LENGTH.message), result)
    }

    @Test
    fun `validatePassword should return Invalid when password length is more than 20`() {
        val result = InputValidator.validatePassword("P@ssword123456789012345")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_LENGTH.message), result)
    }

    @Test
    fun `validatePassword should return Valid when password meets all criteria`() {
        val result = InputValidator.validatePassword("P@ssword1")
        assertEquals(ValidationResult.Valid("P@ssword1"), result)
    }

    @Test
    fun `validatePasswordConfirmation should return Invalid when password confirmation is blank`() {
        val result = InputValidator.validatePasswordConfirmation("password", "")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validatePasswordConfirmation should return Invalid when passwords do not match`() {
        val result = InputValidator.validatePasswordConfirmation("password", "different")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONFIRMATION_DOES_NOT_MATCH.message), result)
    }

    @Test
    fun `validatePasswordConfirmation should return Valid when passwords match`() {
        val result = InputValidator.validatePasswordConfirmation("password", "password")
        assertEquals(ValidationResult.Valid("password"), result)
    }

    @Test
    fun `validateUsername should return Invalid when username is blank`() {
        val result = InputValidator.validateUsername("")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username contains special characters`() {
        val result = InputValidator.validateUsername("user@name")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_FORMAT.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username contains spaces`() {
        val result = InputValidator.validateUsername("user name")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_FORMAT.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username length is less than 3`() {
        val result = InputValidator.validateUsername("us")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_LENGTH.message), result)
    }

    @Test
    fun `validateUsername should return Invalid when username length is more than 20`() {
        val result = InputValidator.validateUsername("usernameusernameusername")
        assertEquals(ValidationResult.Invalid(InvalidInputMessage.USERNAME_LENGTH.message), result)
    }

    @Test
    fun `validateUsername should return Valid when username meets all criteria`() {
        val result = InputValidator.validateUsername("username")
        assertEquals(ValidationResult.Valid("username"), result)
    }
}