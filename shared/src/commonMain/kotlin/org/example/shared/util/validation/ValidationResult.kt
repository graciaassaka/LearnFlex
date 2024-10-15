package org.example.shared.util.validation

sealed class ValidationResult<T>
{
    data class Valid<T>(val value: T) : ValidationResult<T>()

    data class Invalid<T>(val message: String) : ValidationResult<T>()
}