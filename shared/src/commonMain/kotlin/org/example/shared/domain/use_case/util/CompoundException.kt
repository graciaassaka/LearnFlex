package org.example.shared.domain.use_case.util

/**
 * Represents a failure that occurred during an operation and its subsequent rollback attempt.
 * This helps maintain a complete error trail when cleanup operations also fail.
 *
 * @property originalError The exception that triggered the rollback attempt
 * @property rollbackErrors The exception that occurred during rollback
 */
class CompoundException(
    val originalError: Throwable,
    vararg rollbackErrors: Throwable
) : Exception(
    """
       Original error: 
       ______________
       ${originalError.message}
       Rollback errors: 
       ________________
       ${rollbackErrors.joinToString("\n") { it.message.toString() }}
    """.trimIndent()
)