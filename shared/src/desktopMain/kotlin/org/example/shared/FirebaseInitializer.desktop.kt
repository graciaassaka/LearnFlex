package org.example.shared

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.shared.data.firebase.FirebaseConfig
import org.example.shared.domain.TokenStorage
import org.koin.java.KoinJavaComponent.inject

/**
 * Class responsible for initializing Firebase settings and handling token storage.
 */
actual class FirebaseInitializer {

    companion object {
        /**
         * Token storage instance for handling encryption and decryption of tokens.
         */
        private val tokenStorage: TokenStorage by inject(TokenStorage::class.java)

        /**
         * Filename for storing the ID token.
         */
        private const val ID_TOKEN_FILENAME = "idToken.enc"

        /**
         * Filename for storing the refresh token.
         */
        private const val REFRESH_TOKEN_FILENAME = "refreshToken.enc"
    }

    /**
     * The ID token used for authentication.
     */
    var idToken: String = ""
        get() = tokenStorage.readAndDecrypt(ID_TOKEN_FILENAME) ?: ""
        set(it) {
            field = it
            tokenStorage.encryptAndSave(it, ID_TOKEN_FILENAME)
        }

    /**
     * The refresh token used for authentication.
     */
    var refreshToken: String = ""
        get() = tokenStorage.readAndDecrypt(REFRESH_TOKEN_FILENAME) ?: ""
        set(it) {
            field = it
            tokenStorage.encryptAndSave(it, REFRESH_TOKEN_FILENAME)
        }

    /**
     * The API key for Firebase.
     */
    lateinit var apiKey: String
        private set

    /**
     * The HTTP client used for making network requests.
     */
    lateinit var httpClient: HttpClient
        private set

    /**
     * Initializes the Firebase settings and token storage.
     */
    init {
        initialize()
    }

    /**
     * Initializes the Firebase settings, including setting up the HTTP client and reading tokens.
     */
    actual fun initialize() {
        idToken = tokenStorage.readAndDecrypt(ID_TOKEN_FILENAME) ?: ""
        refreshToken = tokenStorage.readAndDecrypt(REFRESH_TOKEN_FILENAME) ?: ""

        apiKey = if (FirebaseConfig.useEmulator()) "fake-api-key-for-emulator"
        else System.getenv("FIREBASE_API_KEY") ?: throw Exception("Firebase API key not found")

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        setupEmulator()
    }

    /**
     * Sets up the Firebase emulator if it is being used.
     */
    private fun setupEmulator() {
        if (FirebaseConfig.useEmulator()) {
            System.setProperty(
                "firebase.auth.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getAuthEmulatorPort()}"
            )
            System.setProperty(
                "firebase.firestore.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getFirestoreEmulatorPort()}"
            )
            System.setProperty(
                "firebase.storage.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getStorageEmulatorPort()}"
            )
            System.setProperty(
                "firebase.functions.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getFunctionsEmulatorPort()}"
            )
        }
    }

    /**
     * Checks if the Firebase emulator is being used.
     * @return `true` if the emulator is being used, `false` otherwise.
     */
    fun isUsingEmulator(): Boolean = FirebaseConfig.useEmulator()

    /**
     * Retrieves the host address for the Firebase emulator.
     * @return The host address as a `String`.
     */
    fun getEmulatorHost(): String = FirebaseConfig.getEmulatorHost()

    /**
     * Retrieves the port number for the Firebase Authentication emulator.
     * @return The port number as an `Int`.
     */
    fun getAuthEmulatorPort(): Int = FirebaseConfig.getAuthEmulatorPort()
}