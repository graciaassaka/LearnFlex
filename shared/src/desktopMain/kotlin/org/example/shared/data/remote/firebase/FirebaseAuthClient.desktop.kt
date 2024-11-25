package org.example.shared.data.remote.firebase

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.shared.FirebaseInit
import org.example.shared.data.remote.util.HttpResponseHandler
import org.example.shared.data.util.FirebaseConstants
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.model.User

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
            HttpResponseHandler<Unit>(this).invoke {
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
            HttpResponseHandler<Unit>(this).invoke {
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
            HttpResponseHandler<User>(this).invoke {
                body<GetUserDataResponse>().users.first()
            }
        }
    }

    /**
     * Updates the current user's username.
     *
     * @param username The new username to set.
     * @return Result of the username update operation.
     */
    override suspend fun updateUsername(username: String) = runCatching {
        client.post {
            setUpAuthRequest("update")
            setBody(UsernameUpdatePayload(firebaseInit.idToken, username))
        }.run {
            HttpResponseHandler<Unit>(this).invoke { }
        }
    }

    /**
     * Updates the current user's photo URL.
     *
     * @param photoUrl The new photo URL to set.
     * @return Result of the photo URL update operation.
     */
    override suspend fun updatePhotoUrl(photoUrl: String) = runCatching {
        client.post {
            setUpAuthRequest("update")
            setBody(PhotoUrlUpdatePayload(firebaseInit.idToken, photoUrl))
        }.run {
            HttpResponseHandler<Unit>(this).invoke { }
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
            HttpResponseHandler<Unit>(this).invoke { }
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
            HttpResponseHandler<Unit>(this).invoke { }
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
            HttpResponseHandler<Unit>(this).invoke { }
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
}