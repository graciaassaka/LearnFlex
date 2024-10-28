package org.example.shared.data.firebase

import dev.gitlive.firebase.auth.FirebaseAuth
import org.example.shared.data.model.User
import org.example.shared.domain.service.AuthService

/**
 * Service class for Firebase Authentication.
 *
 * @property auth The FirebaseAuth instance used for authentication operations.
 */
actual class FirebaseAuthService(private val auth: FirebaseAuth) : AuthService {

    /**
     * Signs up a new user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password)
        result.user?.sendEmailVerification()
    }

    /**
     * Signs in a user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password)
    }

    /**
     * Signs out the currently signed-in user.
     */
    override suspend fun signOut() {
        auth.signOut()
    }

    /**
     * Retrieves the current user's data.
     *
     * @return A Result containing the User data if successful, or an exception if failed.
     */
    override suspend fun getUserData(): Result<User> = runCatching {
        auth.currentUser?.run {
            reload()
            User(
                displayName = displayName ?: "",
                email = email ?: "",
                photoUrl = (photoURL ?: "").toString(),
                emailVerified = isEmailVerified,
                uid = uid
            )
        } ?: throw Exception("No signed in user")
    }

    /**
     * Sends email verification to the currently signed-in user.
     *
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        auth.currentUser?.sendEmailVerification() ?: throw Exception("No signed in user")
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the password reset email to.
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email)
    }

    /**
     * Deletes the currently signed-in user.
     *
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun deleteUser(): Result<Unit> = runCatching {
        auth.currentUser?.delete() ?: throw Exception("No signed in user")
    }
}