package org.example.shared.data.remote.firebase

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
 */
@Serializable
data class UsernameUpdatePayload(
    val idToken: String,
    val displayName: String
)

/**
 * Data class representing the payload for updating a user's photo URL.
 *
 * @property idToken The ID token of the user making the request.
 * @property photoUrl The new photo URL for the user.
 */
@Serializable
data class PhotoUrlUpdatePayload(
    val idToken: String,
    val photoUrl: String,
)

/**
 * Data class representing the metadata of a file stored in Firebase Storage.
 *
 * @property contentType The content type of the file.
 * @property name The name of the file.
 */
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