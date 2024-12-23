package org.example.shared.domain.use_case.validation

import org.example.shared.domain.use_case.validation.util.InvalidInputMessage
import org.example.shared.domain.use_case.validation.util.ValidationResult

/**
 * Use case for validating password confirmation.
 */
class ValidatePasswordConfirmationUseCase {

    /**
     * Validates that the password confirmation matches the password.
     *
     * @param password The original password.
     * @param passwordConfirmation The password confirmation to validate.
     * @return A [ValidationResult] indicating whether the password confirmation is valid or not.
     */
    operator fun invoke(password: String, passwordConfirmation: String) = when {
        passwordConfirmation.isBlank() -> ValidationResult.Invalid(InvalidInputMessage.EMPTY_FIELD.message)
        password != passwordConfirmation -> ValidationResult.Invalid(InvalidInputMessage.PASSWORD_CONFIRMATION_DOES_NOT_MATCH.message)
        else -> ValidationResult.Valid(passwordConfirmation)
    }
}
