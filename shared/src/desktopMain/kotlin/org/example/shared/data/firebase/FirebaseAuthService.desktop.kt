package org.example.shared.data.firebase

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.example.shared.FirebaseInitializer
import org.example.shared.data.model.*
import org.example.shared.domain.service.AuthService

/**
 * Service for handling Firebase Authentication.
 *
 * @constructor Creates a FirebaseAuthService instance with the given FirebaseInitializer.
 * @param firebaseInitializer The Firebase initializer to configure Firebase services.
 */
actual class FirebaseAuthService(
    private val firebaseInitializer: FirebaseInitializer
) : AuthService
{
    /**
     * Signs up a new user with the given email and password.
     *
     * @param email The email address of the user to sign up.
     * @param password The password for the new user.
     * @return A [Result] containing [Unit] if the sign-up was successful, or an exception if an error occurred.
     */
    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        firebaseInitializer.httpClient.post {
            setupAuthRequest("/v1/accounts:signUp")
            contentType(ContentType.Application.Json)
            setBody(Credentials(email = email, password = password, returnSecureToken = true))
        }.apply {
            if (status.isSuccess())
            {
                handleSuccess<FirebaseSignUpResponse>()
                sendVerificationEmail()
            } else
            {
                throw Exception("Sign-up failed: $status")
            }
        }
    }

    /**
     * Sends an email verification request to the Firebase authentication service.
     *
     * This method posts a request to Firebase to send a verification email to the currently authenticated user.
     * The request is configured with the necessary authentication parameters and headers.
     * If the request fails, an exception is thrown indicating the failure reason.
     *
     * @throws Exception if the HTTP request is unsuccessful.
     */
    override suspend fun sendVerificationEmail(): Result<Unit> = runCatching {
        firebaseInitializer.httpClient.post {
            setupAuthRequest("/v1/accounts:sendOobCode")
            contentType(ContentType.Application.Json)
            setBody(mapOf("requestType" to "VERIFY_EMAIL", "idToken" to firebaseInitializer.idToken))
        }.apply {
            if (status.isSuccess().not()) throw Exception("Send email verification failed: $status")
        }
    }

    /**
     * Signs in a user with the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password for the user.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        firebaseInitializer.httpClient.post {
            setupAuthRequest("/v1/accounts:signInWithPassword")
            contentType(ContentType.Application.Json)
            setBody(Credentials(email = email, password = password, returnSecureToken = true))
        }.apply {
            if (status.isSuccess()) handleSuccess<FirebaseSignInResponse>()
            else throw Exception("Sign-in failed: $status")
        }
    }

    /**
     * Signs out the current user by clearing the stored authentication tokens.
     *
     * This method updates the `idToken` and `refreshToken` to empty strings,
     * effectively signing the user out of the application.
     */
    override fun signOut()
    {
        firebaseInitializer.idToken = ""
        firebaseInitializer.refreshToken = ""
    }

    /**
     * Retrieves the current user's data from the Firebase authentication service.
     *
     * @return A [Result] containing a [User] object if successful, or an exception if an error occurs.
     */
    override suspend fun getUserData(): Result<User> = runCatching {
        firebaseInitializer.httpClient.post {
            setupAuthRequest("/v1/accounts:lookup")
            contentType(ContentType.Application.Json)
            setBody(mapOf("idToken" to firebaseInitializer.idToken))
        }.run {
            if (status.isSuccess())
                Json.decodeFromString<Users>(bodyAsText()).users.firstOrNull() ?: throw Exception("No user data found")
            else
                throw Exception("Get user data failed: $status")
        }
    }

    /**
     * Sends a password reset email to the provided email address.
     *
     * @param email The email address to send the password reset email to.
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        firebaseInitializer.httpClient.post {
            setupAuthRequest("/v1/accounts:sendOobCode")
            contentType(ContentType.Application.Json)
            setBody(mapOf("requestType" to "PASSWORD_RESET", "email" to email))
        }.apply {
            if (status.isSuccess().not()) throw Exception("Send password reset email failed: $status")
        }
    }

    /**
     * Deletes the current user's account from the Firebase authentication service.
     *
     * @return A [Result] containing [Unit] if successful, or an exception if an error occurs.
     */
    override suspend fun deleteUser(): Result<Unit> = runCatching {
        firebaseInitializer.httpClient.post {
            setupAuthRequest("/v1/accounts:delete")
            contentType(ContentType.Application.Json)
            setBody(mapOf("idToken" to firebaseInitializer.idToken))
        }.apply {
            if (status.isSuccess().not()) throw Exception("Delete user failed: $status")
        }
    }

    /**
     * Configures the HTTP request for authentication operations.
     *
     * Sets up the URL and parameters required for the request to either the Firebase Emulator
     * or the Firebase production server, based on whether the emulator is being used.
     *
     * @param path The endpoint path for the request.
     */
    private fun HttpRequestBuilder.setupAuthRequest(path: String)
    {
        url {
            if (firebaseInitializer.isUsingEmulator())
            {
                protocol = URLProtocol.HTTP
                host = firebaseInitializer.getEmulatorHost()
                port = firebaseInitializer.getAuthEmulatorPort()
                encodedPath = "/identitytoolkit.googleapis.com$path"
            } else
            {
                protocol = URLProtocol.HTTPS
                host = "identitytoolkit.googleapis.com"
                encodedPath = path
            }
            parameter("key", firebaseInitializer.apiKey)
        }
    }

    /**
     * Handles a successful HTTP response by parsing and updating Firebase tokens.
     *
     * This method is called when an HTTP response indicates a successful request.
     * It parses the response body to extract the `idToken` and `refreshToken`,
     * then updates these tokens in the `firebaseInitializer`.
     *
     * @throws Exception if the response does not contain the required tokens.
     */
    private suspend inline fun <reified T : FirebaseAuthResponse> HttpResponse.handleSuccess(): T = try
    {
        Json.decodeFromString<T>(bodyAsText()).also { response ->
            firebaseInitializer.idToken = response.idToken
            firebaseInitializer.refreshToken = response.refreshToken
        }
    } catch (e: Exception)
    {
        throw Exception("Failed to parse Firebase auth response: ${e.message}")
    }
}