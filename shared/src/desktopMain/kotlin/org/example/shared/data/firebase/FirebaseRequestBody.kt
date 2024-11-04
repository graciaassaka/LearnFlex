package org.example.shared.data.firebase

import kotlinx.serialization.Serializable

/**
 * Data class representing user credentials.
 *
 * @property email The user's email address.
 * @property password The user's password.
 * @property returnSecureToken Whether to return a secure token.
 */
@Serializable
data class Credentials(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

/**
 * Data class representing an authentication token.
 *
 * @property idToken The ID token.
 */
@Serializable
data class AuthToken(
    val idToken: String
)

/**
 * Data class representing the payload for verification requests.
 *
 * @property requestType The type of verification request, e.g., VERIFY_EMAIL.
 * @property idToken The ID token of the user making the request.
 */
@Serializable
data class VerificationPayload(
    val requestType: String,
    val idToken: String
)

/**
 * Data class representing the payload for a password reset request.
 *
 * @property requestType The type of request being made, typically a string indicating "PASSWORD_RESET".
 * @property email The email address associated with the account for which the password reset is being requested.
 */
@Serializable
data class PasswordResetPayload(
    val requestType: String,
    val email: String,
)

/**
 * Data class representing the payload for updating a user's profile.
 *
 * @property idToken The ID token of the user making the request.
 * @property displayName The new display name for the user.
 * @property photoUrl The new photo URL for the user.
 */
@Serializable
data class ProfileUpdatePayload(
    val idToken: String,
    val displayName: String,
    val photoUrl: String,
)

@Serializable
data class StorageMetadata(
    val contentType: String,
    val name: String
)


/**
 * Enum class representing request types.
 */
@Serializable
enum class RequestType {
    VERIFY_EMAIL,
    PASSWORD_RESET,
}