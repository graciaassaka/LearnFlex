package org.example.shared.data.remote.firebase

import kotlinx.serialization.Serializable
import org.example.shared.domain.model.User

/**
 * Data class representing the response from an authentication request.
 *
 * @property idToken The ID token received from the authentication request.
 * @property email The email associated with the authenticated user.
 * @property refreshToken The refresh token received from the authentication request.
 * @property expiresIn The expiration time of the ID token.
 * @property localId The local ID of the authenticated user.
 */
@Serializable
data class AuthResponse(
    val idToken: String,
    val email: String,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String
)

/**
 * Data class representing the response from a request to get user data.
 *
 * @property users A list of users retrieved from the request.
 */
@Serializable
data class GetUserDataResponse(
    val users: List<User>
)