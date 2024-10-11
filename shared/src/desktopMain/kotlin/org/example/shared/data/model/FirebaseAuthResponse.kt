package org.example.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Interface representing a Firebase authentication response.
 */
interface FirebaseAuthResponse
{
    val kind: String?
    val idToken: String
    val refreshToken: String
    val email: String?
    val expiresIn: String?
    val localId: String?
}

/**
 * Data class representing a Firebase sign-up response.
 *
 * @property email The email associated with the Firebase account.
 * @property expiresIn The expiration time of the ID token.
 * @property idToken The ID token received from Firebase.
 * @property localId The local ID of the Firebase account.
 * @property refreshToken The refresh token received from Firebase.
 */
@Serializable
data class FirebaseSignUpResponse(
    override val email: String? = null,
    override val expiresIn: String? = null,
    override val idToken: String,
    override val kind: String? = null,
    override val localId: String? = null,
    override val refreshToken: String
) : FirebaseAuthResponse

/**
 * Data class representing a Firebase sign-in response.
 *
 * @property displayName The display name of the user.
 * @property email The email associated with the Firebase account.
 * @property expiresIn The expiration time of the ID token.
 * @property idToken The ID token received from Firebase.
 * @property localId The local ID of the Firebase account.
 * @property refreshToken The refresh token received from Firebase.
 * @property registered Indicates whether the user is registered.
 */
@Serializable
data class FirebaseSignInResponse(
    val displayName: String? = null,
    override val email: String? = null,
    override val expiresIn: String? = null,
    override val idToken: String,
    override val kind: String? = null,
    override val localId: String? = null,
    override val refreshToken: String,
    val registered: Boolean = false
) : FirebaseAuthResponse