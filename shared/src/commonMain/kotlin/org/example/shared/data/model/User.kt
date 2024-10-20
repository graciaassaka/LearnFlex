package org.example.shared.data.model

/**
 * Represents a user in the system.
 */
expect class User
{
    val displayName: String?
    val email: String?
    val emailVerified: Boolean?
    val photoUrl: String?
}
