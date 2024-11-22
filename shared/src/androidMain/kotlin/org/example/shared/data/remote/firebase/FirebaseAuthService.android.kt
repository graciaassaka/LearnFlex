package org.example.shared.data.remote.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import org.example.shared.domain.model.User
import org.example.shared.domain.service.AuthClient

/**
 * Service class for Firebase Authentication.
 *
 * @property auth The FirebaseAuth instance used for authentication operations.
 */
actual class FirebaseAuthClient(
    private val auth: FirebaseAuth
) : AuthClient {
    /**
     * Signs up a new user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.sendEmailVerification()?.await()
    }

    /**
     * Signs in a user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
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
            reload().await()
            User(
                displayName = displayName ?: "",
                email = email ?: "",
                photoUrl = photoUrl?.toString() ?: "",
                emailVerified = isEmailVerified,
                localId = uid
            )
        } ?: throw Exception("No signed in user")
    }

    /**
     * Updates the current user's display name and photo URL.
     *
     * @param user The updated user data.
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun updateUserData(user: User): Result<Unit> = runCatching {
        auth.currentUser?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(user.displayName)
                .setPhotoUri(user.photoUrl?.let { Uri.parse(it) })
                .build()
        )?.await()
    }

    /**
     * Sends email verification to the currently signed-in user.
     *
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the password reset email to.
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * Deletes the currently signed-in user.
     *
     * @return A Result containing Unit if successful, or an exception if failed.
     */
    override suspend fun deleteUser(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
    }
}
