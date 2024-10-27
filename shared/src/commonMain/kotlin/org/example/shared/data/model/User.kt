package org.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a user.
 */
@Serializable
data class User(
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("photoUrl") val photoUrl: String? = null,
    @SerialName("emailVerified") val emailVerified: Boolean? = null,
    @SerialName("localId") val uid: String? = null
)
