package org.example.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a User.
 */
@Serializable
actual data class User(
    val displayName : String,
    val email : String,
    val photoUrl : String,
    val emailVerified: Boolean,
    val uid: String
)