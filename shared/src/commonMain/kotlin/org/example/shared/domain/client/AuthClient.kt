package org.example.shared.domain.client

import org.example.shared.domain.model.User

/**
 * Interface defining authentication-related operations.
 */
interface AuthClient {

    /**
     * Signs up a new user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password for the user.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun signUp(email: String, password: String): Result<Unit>

    /**
     * Signs in a user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password for the user.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun signIn(email: String, password: String): Result<Unit>

    /**
     * Signs out the currently signed-in user.
     */
    suspend fun signOut()

    /**
     * Retrieves the current user's data.
     *
     * @return A [Result] containing a [User] object if successful, or an exception if an error occurs.
     */
    suspend fun getUserData(): Result<User>

    /**
     * Updates the current user's username.
     *
     * @param username The new username to set.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun updateUsername(username: String): Result<Unit>

    /**
     * Updates the current user's photo URL.
     *
     * @param photoUrl The new photo URL to set.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun updatePhotoUrl(photoUrl: String): Result<Unit>

    /**
     * Sends a verification email to the current user.
     *
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Sends a password reset email to the provided email address.
     *
     * @param email The email address to send the password reset email to.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Deletes the current user's account.
     *
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun deleteUser(): Result<Unit>
}