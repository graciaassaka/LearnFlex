package org.example.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * User data class representing a user in the system.
 *
 * @property displayName The display name of the user.
 * @property email The email address of the user.
 * @property photoUrl The URL of the user's profile photo.
 * @property emailVerified Whether the user's email is verified.
 * @property localId The local identifier of the user.
 */
@Serializable
data class User(
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val emailVerified: Boolean? = null,
    val localId: String? = null
)
