package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult

/**
 * Use case class for validating a username. Ensures that the username meets specific criteria
 * including non-emptiness, appropriate length, and valid characters.
 */
class ValidateUsernameUseCase {
    /**
     * Validates a username according to specific criteria: non-blank, appropriate length, and correct format.
     *
     * @param username The username to be validated.
     * @return A ValidationResult, which is Valid if the username meets all criteria, or Invalid
     *         with an appropriate error message if any criteria are not met.
     */
    operator fun invoke(username: String) = when {
        username.isBlank() -> ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message)
        username.length !in 3..20 -> ValidationResult.Invalid(InvalidInputMessage.USERNAME_LENGTH.message)
        !username.matches("""^[a-zA-Z0-9_]*$""".toRegex()) -> ValidationResult.Invalid(InvalidInputMessage.USERNAME_FORMAT.message)
        else -> ValidationResult.Valid(username)
    }
}