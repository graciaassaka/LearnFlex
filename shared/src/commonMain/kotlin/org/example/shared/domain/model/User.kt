package org.example.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a user.
 */
@Serializable
data class User(
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val emailVerified: Boolean? = null,
    val localId: String? = null
)
