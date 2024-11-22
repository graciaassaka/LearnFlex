package org.example.shared.data.remote.firebase

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.shared.data.remote.firebase.util.TestFirebaseUtil
import org.example.shared.data.remote.util.ApiError
import org.example.shared.data.util.FirebaseConstants
import org.example.shared.domain.model.User
import org.junit.After
import org.junit.Before
import kotlin.test.Test

@WireMockTest
@ExperimentalCoroutinesApi
actual class FirebaseAuthClientTest {
    private lateinit var firebaseAuthService: FirebaseAuthClient
    private lateinit var httpClient: HttpClient
    private lateinit var wireMockServer: WireMockServer

    companion object {
        private val firebaseInit = TestFirebaseUtil.getFirebaseInit()
    }

    @Before
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.options().port(9099))
        wireMockServer.start()
        configureFor(wireMockServer.port())

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.NONE
            }
        }

        firebaseAuthService = FirebaseAuthClient(httpClient, firebaseInit, true)
    }

    @After
    fun tearDown() {
        wireMockServer.stop()
        httpClient.close()
        TestFirebaseUtil.cleanup()
    }

    @Test
    fun `signUp should return success when status code is 200 and response is valid`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password"

        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signUp?key=${FirebaseConstants.API_KEY}"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "idToken": "idToken",
                                "email": "$email",
                                "refreshToken": "refreshToken",
                                "expiresIn": "3600",
                                "localId": "localId"
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.signUp(email, password)

        // Assert
        assert(result.isSuccess) { "Expected successful sign up" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signUp?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `signUp should fail when server returns error`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password"

        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signUp?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "code": 401,
                                    "message": "Invalid email",
                                    "errors": [
                                        {
                                            "message": "Invalid email",
                                            "domain": "global",
                                            "reason": "invalid"
                                        }
                                    ]
                                }
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act & Assert
        val result = firebaseAuthService.signUp(email, password)
        assert(result.isFailure) { "Expected failed sign up" }
        val error = result.exceptionOrNull()
        assert(error is ApiError.Unauthorized) { "Expected ApiError.Unauthorized but got ${error?.javaClass?.simpleName}" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signUp?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `signIn should return success when status code is 200 and response is valid`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password"

        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FirebaseConstants.API_KEY}"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "idToken": "idToken",
                                "email": "$email",
                                "refreshToken": "refreshToken",
                                "expiresIn": "3600",
                                "localId": "localId"
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.signIn(email, password)

        // Assert
        assert(result.isSuccess) { "Expected successful sign in" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `signIn should fail when server returns error`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password"

        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "code": 403,
                                    "message": "Invalid email",
                                    "errors": [
                                        {
                                            "message": "Invalid email",
                                            "domain": "global",
                                            "reason": "invalid"
                                        }
                                    ]
                                }
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.signIn(email, password)

        // Assert
        assert(result.isFailure) { "Expected failed sign in" }
        val error = result.exceptionOrNull()
        assert(error is ApiError.Forbidden) { "Expected ApiError.Forbidden but got ${error?.javaClass?.simpleName}" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `signOut should clear tokens`() = runTest {
        // Arrange
        firebaseInit.idToken = "idToken"
        firebaseInit.refreshToken = "refreshToken"

        // Act
        firebaseAuthService.signOut()

        // Assert
        assert(firebaseInit.idToken.isEmpty()) { "Expected empty idToken" }
        assert(firebaseInit.refreshToken.isEmpty()) { "Expected empty refreshToken" }
    }

    @Test
    fun `getUserData should return success when status code is 200 and response is valid`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:lookup?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "users": [
                                    {
                                        "localId": "localId",
                                        "email": "test@example.com",
                                        "emailVerified": true,
                                        "displayName": "Test User",
                                        "photoUrl": "https://example.com/photo.jpg"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.getUserData()

        // Assert
        assert(result.isSuccess) { "Expected successful get user data" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:lookup?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `getUserData should fail when server returns error`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:lookup?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "code": 404,
                                    "message": "Unauthorized",
                                    "errors": [
                                        {
                                            "message": "Unauthorized",
                                            "domain": "global",
                                            "reason": "unauthorized"
                                        }
                                    ]
                                }
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.getUserData()

        // Assert
        assert(result.isFailure) { "Expected failed get user data" }
        val error = result.exceptionOrNull()
        assert(error is ApiError.NotFound) { "Expected ApiError.NotFound but got ${error?.javaClass?.simpleName}" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:lookup?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `updateProfile should return success when status code is 200 and response is valid`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:update?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                )
        )

        // Act
        val result =
            firebaseAuthService.updateUserData(User(displayName = "test", photoUrl = "https://example.com/photo.jpg"))

        // Assert
        assert(result.isSuccess) { "Expected successful update profile" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:update?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `updateProfile should fail when server returns error`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:update?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "code": 403,
                                    "message": "Forbidden",
                                    "errors": [
                                        {
                                            "message": "Forbidden",
                                            "domain": "global",
                                            "reason": "forbidden"
                                        }
                                    ]
                                }
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result =
            firebaseAuthService.updateUserData(User(displayName = "test", photoUrl = "https://example.com/photo.jpg"))

        // Assert
        assert(result.isFailure) { "Expected failed update profile" }
        val error = result.exceptionOrNull()
        assert(error is ApiError.Forbidden) { "Expected ApiError.Forbidden but got ${error?.javaClass?.simpleName}" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:update?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `sendEmailVerification should return success when status code is 200 and response is valid`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "email": "test@example.com"
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.sendEmailVerification()

        // Assert
        assert(result.isSuccess) { "Expected successful send email verification" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `sendEmailVerification should fail when server returns error`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "code": 429,
                                    "message": "Forbidden",
                                    "errors": [
                                        {
                                            "message": "Forbidden",
                                            "domain": "global",
                                            "reason": "forbidden"
                                        }
                                    ]
                                }
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.sendEmailVerification()

        // Assert
        assert(result.isFailure) { "Expected failed send email verification" }
        val error = result.exceptionOrNull()
        assert(error is ApiError.RateLimitExceeded) { "Expected ApiError.RateLimitExceeded but got ${error?.javaClass?.simpleName}" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `sendPasswordResetEmail should return success when status code is 200 and response is valid`() = runTest {
        // Arrange
        val email = "test@example.com"

        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "email": "$email"
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.sendPasswordResetEmail(email)

        // Assert
        assert(result.isSuccess) { "Expected successful send password reset email" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `sendPasswordResetEmail should fail when server returns error`() = runTest {
        // Arrange
        val email = "test@example.com"

        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "code": 503,
                                    "message": "Network error",
                                    "errors": [
                                        {
                                            "message": "Network error",
                                            "domain": "global",
                                            "reason": "networkError"
                                        }
                                    ]
                                }
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.sendPasswordResetEmail(email)

        // Assert
        assert(result.isFailure) { "Expected failed send password reset email" }
        val error = result.exceptionOrNull()
        assert(error is ApiError.NetworkError) { "Expected ApiError.NetworkError but got ${error?.javaClass?.simpleName}" }

        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `deleteUser should return success when status code is 200 and response is valid`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:delete?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "kind": "identitytoolkit#DeleteAccountResponse"
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.deleteUser()

        // Assert
        assert(result.isSuccess) { "Expected successful delete user" }
        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:delete?key=${FirebaseConstants.API_KEY}")))
    }

    @Test
    fun `deleteUser should fail when server returns error`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:delete?key=${FirebaseConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "code": 503,
                                    "message": "Network error",
                                    "errors": [
                                        {
                                            "message": "Network error",
                                            "domain": "global",
                                            "reason": "networkError"
                                        }
                                    ]
                                }
                            }
                        """.trimIndent()
                        )
                )
        )

        // Act
        val result = firebaseAuthService.deleteUser()

        // Assert
        assert(result.isFailure) { "Expected failed delete user" }
        val error = result.exceptionOrNull()
        assert(error is ApiError.NetworkError) { "Expected ApiError.NetworkError but got ${error?.javaClass?.simpleName}" }

        wireMockServer.verify(postRequestedFor(urlEqualTo("/identitytoolkit.googleapis.com/v1/accounts:delete?key=${FirebaseConstants.API_KEY}")))
    }
}