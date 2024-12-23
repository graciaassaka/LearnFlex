package org.example.shared.domain.use_case.util

/**
 * Represents a failure that occurred during an operation and its subsequent rollback attempt.
 * This helps maintain a complete error trail when cleanup operations also fail.
 *
 * @property message A description of what went wrong
 * @property originalError The exception that triggered the rollback attempt
 * @property rollbackError The exception that occurred during rollback
 */
class CompoundException(
    message: String,
    val originalError: Throwable,
    val rollbackError: Throwable
) : Exception(
    """
       $message
       Original error: ${originalError.message}
       Rollback error: ${rollbackError.message}
    """.trimIndent()
)