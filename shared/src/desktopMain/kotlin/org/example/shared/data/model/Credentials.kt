package org.example.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Represents user credentials.
 */
@Serializable
data class Credentials(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)
