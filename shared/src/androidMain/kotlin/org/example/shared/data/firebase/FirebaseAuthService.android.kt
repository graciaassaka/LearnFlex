package org.example.shared.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import org.example.shared.data.model.User
import org.example.shared.domain.service.AuthService

/**
 * Implementation of [AuthService] using Firebase Authentication.
 *
 * @property auth The FirebaseAuth instance used for authentication operations.
 */
actual class FirebaseAuthService(private val auth: FirebaseAuth) : AuthService {

    /**
     * Signs up a new user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password for the user.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()?.user?.run {
            sendEmailVerification().await()
        } ?: throw Exception("No signed in user")
    }

    /**
     * Signs in a user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password for the user.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * Signs out the currently signed-in user.
     */
    override fun signOut() = auth.signOut()

    /**
     * Retrieves the current user's data.
     *
     * @return A [Result] containing a [User] object if successful, or an exception if an error occurs.
     */
    override suspend fun getUserData(): Result<User> = runCatching {
        auth.currentUser?.let { firebaseUser ->
            User(
                displayName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = (firebaseUser.photoUrl ?: "").toString(),
                emailVerified = firebaseUser.isEmailVerified,
                uid = firebaseUser.uid
            )
        } ?: throw Exception("No signed in user")
    }

    /**
     * Sends a password reset email to the provided email address.
     *
     * @param email The email address to send the password reset email to.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }
}