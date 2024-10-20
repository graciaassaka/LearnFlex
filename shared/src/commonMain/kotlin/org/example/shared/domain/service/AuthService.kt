package org.example.shared.domain.service

import org.example.shared.data.model.User

/**
 * Interface defining authentication-related operations.
 */
interface AuthService {

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
    fun signOut()

    /**
     * Retrieves the current user's data.
     *
     * @return A [Result] containing a [User] object if successful, or an exception if an error occurs.
     */
    suspend fun getUserData(): Result<User>

    /**
     * Sends a verification email to the current user.
     *
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    suspend fun sendVerificationEmail(): Result<Unit>

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