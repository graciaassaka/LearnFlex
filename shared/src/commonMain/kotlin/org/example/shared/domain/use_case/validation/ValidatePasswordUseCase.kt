package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult


/**
 * Use case class for validating a password. Ensures the password meets specific security criteria.
 */
class ValidatePasswordUseCase {
    /**
     * Validates a password based on several criteria, including presence of letters, numbers,
     * special characters, and length requirements. The password should not contain spaces.
     *
     * @param password The password string that needs validation.
     * @return A ValidationResult which is either Valid if all criteria are met, or Invalid
     *         with an appropriate error message.
     */
    operator fun invoke(password: String) = when {
        password.isBlank() -> ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message)
        password.contains("""\s""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONTAINS_SPACES.message)
        !password.contains("""[a-zA-Z]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LETTER.message)
        !password.contains("""[0-9]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_NUMBER.message)
        !password.contains("""[A-Z]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_UPPERCASE_LETTER.message)
        !password.contains("""[a-z]""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_NO_LOWER_CASE_LETTER.message)
        !password.contains("""[!@#$%^&*()_+\-=\[\]{};':\\|,.<>/?]+""".toRegex()) -> ValidationResult.Invalid(
            InvalidInputMessage.PASSWORD_NO_SPECIAL_CHARACTER.message
        )

        password.length !in 8..20 -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_LENGTH.message)
        else -> ValidationResult.Valid(password)
    }
}