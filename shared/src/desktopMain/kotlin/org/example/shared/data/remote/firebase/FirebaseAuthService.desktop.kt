package org.example.shared.data.remote.firebase

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.example.shared.FirebaseInit
import org.example.shared.data.remote.util.ApiError
import org.example.shared.data.remote.util.ErrorContainer
import org.example.shared.data.util.FirebaseConstants
import org.example.shared.domain.model.User
import org.example.shared.domain.service.AuthClient

/**
 * Service for handling Firebase authentication.
 *
 * @property client The HTTP client used for making requests.
 * @property firebaseInit The Firebase initialization object.
 * @property useEmulator Flag indicating whether to use the Firebase emulator.
 */
actual class FirebaseAuthClient(
    private val client: HttpClient,
    private val firebaseInit: FirebaseInit,
    private val useEmulator: Boolean
) : AuthClient {

    /**
     * Signs up a new user with the given email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @return Result of the sign-up operation.
     */
    override suspend fun signUp(email: String, password: String) = runCatching {
        client.post {
            setUpAuthRequest("signUp")
            setBody(Credentials(email, password))
        }.run {
            handleAuthResponse {
                with(body<AuthResponse>()) {
                    firebaseInit.idToken = idToken
                    firebaseInit.refreshToken = refreshToken
                    if (status.isSuccess()) sendEmailVerification()
                }
            }
        }
    }

    /**
     * Signs in a user with the given email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @return Result of the sign-in operation.
     */
    override suspend fun signIn(email: String, password: String) = runCatching {
        client.post {
            setUpAuthRequest("signInWithPassword")
            setBody(Credentials(email, password))
        }.run {
            handleAuthResponse {
                with(body<AuthResponse>()) {
                    firebaseInit.idToken = idToken
                    firebaseInit.refreshToken = refreshToken
                }
            }
        }
    }

    /**
     * Signs out the current user.
     */
    override suspend fun signOut() {
        firebaseInit.idToken = ""
        firebaseInit.refreshToken = ""
    }

    /**
     * Retrieves the current user's data.
     *
     * @return Result of the user data retrieval operation.
     */
    override suspend fun getUserData() = runCatching {
        client.post {
            setUpAuthRequest("lookup")
            setBody(AuthToken(firebaseInit.idToken))
        }.run {
            handleAuthResponse()
            return@run body<GetUserDataResponse>().users.first()
        }
    }

    /**
     * Updates the current user's data.
     *
     * @param user The updated user data.
     * @return Result of the user data update operation.
     */
    override suspend fun updateUserData(user: User) = runCatching {
        client.post {
            setUpAuthRequest("update")
            setBody(ProfileUpdatePayload(firebaseInit.idToken, user.displayName ?: "", user.photoUrl ?: ""))
        }.run {
            handleAuthResponse()
        }
    }

    /**
     * Sends an email verification to the current user.
     *
     * @return Result of the email verification operation.
     */
    override suspend fun sendEmailVerification() = runCatching {
        client.post {
            setUpAuthRequest("sendOobCode")
            setBody(VerificationPayload(RequestType.VERIFY_EMAIL.name, firebaseInit.idToken))
        }.run {
            handleAuthResponse()
        }
    }

    /**
     * Sends a password reset email to the given email address.
     *
     * @param email The email address to send the password reset email to.
     * @return Result of the password reset email operation.
     */
    override suspend fun sendPasswordResetEmail(email: String) = runCatching {
        client.post {
            setUpAuthRequest("sendOobCode")
            setBody(PasswordResetPayload(RequestType.PASSWORD_RESET.name, email))
        }.run {
            handleAuthResponse()
        }
    }

    /**
     * Deletes the current user.
     *
     * @return Result of the user deletion operation.
     */
    override suspend fun deleteUser() = runCatching {
        client.post {
            setUpAuthRequest("delete")
            setBody(AuthToken(firebaseInit.idToken))
        }.run {
            handleAuthResponse()
        }
    }

    /**
     * Sets up the authentication request with the given path.
     *
     * @param path The path for the authentication request.
     */
    private fun HttpRequestBuilder.setUpAuthRequest(path: String) {
        url {
            if (useEmulator) {
                protocol = URLProtocol.HTTP
                host = FirebaseConfig.emulatorHost
                port = FirebaseConfig.authEmulatorPort
                encodedPath = "/identitytoolkit.googleapis.com/v1/accounts:$path"
            } else {
                protocol = URLProtocol.HTTPS
                host = "identitytoolkit.googleapis.com"
                encodedPath = "/v1/accounts:$path"
            }
        }
        parameter("key", FirebaseConstants.API_KEY)
        contentType(ContentType.Application.Json)
    }

    /**
     * Handles the authentication response.
     *
     * @param handleSuccess Optional success handler.
     * @throws ApiError If the response indicates an error.
     */
    private suspend fun HttpResponse.handleAuthResponse(handleSuccess: (suspend () -> Unit)? = null) {
        val errorContent = if (!status.isSuccess()) body<ErrorContainer>() else null
        when (status.value) {
            200 -> handleSuccess?.invoke() ?: Unit
            400 -> throw ApiError.BadRequest(requestPath = request.url.encodedPath, errorContainer = errorContent)
            401 -> throw ApiError.Unauthorized(requestPath = request.url.encodedPath, errorContainer = errorContent)
            403 -> throw ApiError.Forbidden(requestPath = request.url.encodedPath, errorContainer = errorContent)
            404 -> throw ApiError.NotFound(requestPath = request.url.encodedPath, errorContainer = errorContent)
            429 -> throw ApiError.RateLimitExceeded(requestPath = request.url.encodedPath, errorContainer = errorContent)
            503 -> throw ApiError.NetworkError(requestPath = request.url.encodedPath, errorContainer = errorContent)
            else -> throw ApiError.ServerError(requestPath = request.url.encodedPath, status.value, errorContainer = errorContent)
        }
    }
}