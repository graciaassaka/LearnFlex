package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult

/**
 * Use case class for validating an email address. Employs regular expressions to verify the format of the email.
 */
class ValidateEmailUseCase {
    /**
     * A regular expression pattern for validating email addresses.
     * The pattern checks if the email consists of:
     * - a combination of letters (uppercase and lowercase), numbers, dots, underscores, percent signs, plus, hyphens before the '@' symbol.
     * - a domain name consisting of letters, numbers, and hyphens after the '@' symbol.
     * - a top-level domain that consists of 2 or more letters.
     */
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")

    /**
     * Validates an email address ensuring it is not blank and follows the correct format.
     *
     * @param email The email address to be validated.
     * @return A ValidationResult, which is Valid if the email is properly formatted, or Invalid
     *         with an appropriate error message if the email is blank or incorrectly formatted.
     */
    operator fun invoke(email: String) = when {
        email.isBlank() -> ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message)
        !emailRegex.matches(email) -> ValidationResult.Invalid(InvalidInputMessage.EMAIL_FORMAT.message)
        else -> ValidationResult.Valid(email)
    }
}