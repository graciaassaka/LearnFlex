package org.example.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a User.
 */
@Serializable
actual data class User(
    actual val displayName: String? = null,
    actual val email: String? = null,
    actual val photoUrl: String? = null,
    actual val emailVerified: Boolean? = null,
    val uid: String? = null
)