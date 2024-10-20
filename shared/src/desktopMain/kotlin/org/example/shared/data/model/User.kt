package org.example.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a collection of users.
 *
 * @property kind The type or category of users.
 * @property users The list of user objects.
 */
@Serializable
data class Users(
    val kind: String? = null,
    val users: List<User> = emptyList()
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
 * @property lastRefreshAt The timestamp of the last refresh of the user's information.
 */
@Serializable
actual data class User(
    val createdAt: String? = null,
    val customAuth: Boolean? = null,
    val disabled: Boolean? = null,
    actual val displayName: String? = null,
    actual val email: String? = null,
    val salt: String? = null,
    actual val emailVerified: Boolean? = null,
    val lastLoginAt: String? = null,
    val localId: String? = null,
    val passwordHash: String? = null,
    val passwordUpdatedAt: Long? = null,
    actual val photoUrl: String? = null,
    val providerUserInfo: List<ProviderUserInfo>? = null,
    val validSince: String? = null,
    val lastRefreshAt: String? = null
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
 * @property lastRefreshAt The timestamp of the last refresh of the user's information.
 */
@Serializable
data class ProviderUserInfo(
    val displayName: String? = null,
    val email: String? = null,
    val federatedId: String? = null,
    val photoUrl: String? = null,
    val providerId: String? = null,
    val rawId: String? = null,
    val screenName: String? = null,
    val lastRefreshAt: String? = null
)