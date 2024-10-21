package org.example.shared.presentation.util.validation

/**
 * Regular expression for validating email addresses.
 */
private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")

/**
 * Object responsible for validating user input.
 */
object InputValidator
{
    /**
     * Validates the given email.
     *
     * @param email The email to validate.
     * @return The result of the validation.
     */
    fun validateEmail(email: String) = when
    {
        email.isBlank() -> ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message)
        !EMAIL_REGEX.matches(email) -> ValidationResult.Invalid(InvalidInputMessage.EMAIL_FORMAT.message)
        else -> ValidationResult.Valid(email)
    }

    /**
     * Validates the given password.
     *
     * @param password The password to validate.
     * @return The result of the validation.
     */
    fun validatePassword(password: String) = when
    {
        password.isBlank() -> ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message)
        password.contains("""\s""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONTAINS_SPACES.message)
        !password.contains("""[a-zA-Z]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LETTER.message)
        !password.contains("""[0-9]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_NUMBER.message)
        !password.contains("""[A-Z]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_UPPERCASE_LETTER.message)
        !password.contains("""[a-z]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LOWER_CASE_LETTER.message)
        !password.contains("""[!@#$%^&*()_+\-=\[\]{};':\\|,.<>/?]+""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_SPECIAL_CHARACTER.message)
        password.length !in 8..20 -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_LENGTH.message)
        else -> ValidationResult.Valid(password)
    }

    /**
     * Validates the given password confirmation.
     *
     * @param password The original password.
     * @param passwordConfirmation The password confirmation to validate.
     * @return The result of the validation.
     */
    fun validatePasswordConfirmation(password: String, passwordConfirmation: String) = when
    {
        passwordConfirmation.isBlank() -> ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message)
        password != passwordConfirmation -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONFIRMATION_DOES_NOT_MATCH.message)
        else -> ValidationResult.Valid(passwordConfirmation)
    }
}