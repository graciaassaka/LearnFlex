package org.example.shared.domain.use_case.validation.util

/**
 * Represents the result of a validation.
 */
sealed class ValidationResult<T> {
    data class Valid<T>(val value: T) : ValidationResult<T>()
    data class Invalid<T>(val message: String) : ValidationResult<T>()
}