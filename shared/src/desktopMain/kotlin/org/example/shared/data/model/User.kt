package org.example.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a collection of users.
 *
 * @property users A list of [User] objects that this collection contains.
 */
@Serializable
data class Users(
    val users: List<User>
)

/**
 * Data class representing a user in the system with detailed information.
 *
 * @property createdAt The timestamp when the user was created.
 * @property customAuth Indicates if the user was authenticated using a custom authentication mechanism.
 * @property disabled Indicates if the user account is disabled.
 * @property displayName The display name of the user.
 * @property email The email address of the user.
 * @property emailVerified Indicates if the user's email is verified.
 * @property lastLoginAt The timestamp of the user's last login.
 * @property localId The unique identifier for the user.
 * @property passwordHash The hashed representation of the user's password.
 * @property passwordUpdatedAt The timestamp when the user's password was last updated.
 * @property photoUrl The URL of the user's profile photo.
 * @property providerUserInfo A list of user information from identity providers.
 * @property validSince The timestamp since when the ID token is valid.
 */
@Serializable
actual data class User(
    val createdAt: String,
    val customAuth: Boolean,
    val disabled: Boolean,
    val displayName: String,
    val email: String,
    val emailVerified: Boolean,
    val lastLoginAt: String,
    val localId: String,
    val passwordHash: String,
    val passwordUpdatedAt: Long,
    val photoUrl: String,
    val providerUserInfo: List<ProviderUserInfo>,
    val validSince: String
)

/**
 * Data class representing provider-specific user information.
 *
 * @property displayName The display name of the user as provided by the authentication provider.
 * @property email The email address of the user as provided by the authentication provider.
 * @property federatedId The unique user identifier assigned by the authentication provider.
 * @property photoUrl The URL of the user's profile photo as provided by the authentication provider.
 * @property providerId The identifier of the authentication provider (e.g., "google.com", "facebook.com").
 * @property rawId The raw user identifier as provided by the authentication provider.
 * @property screenName The screen name or username of the user as provided by the authentication provider.
 */
@Serializable
data class ProviderUserInfo(
    val displayName: String,
    val email: String,
    val federatedId: String,
    val photoUrl: String,
    val providerId: String,
    val rawId: String,
    val screenName: String
)