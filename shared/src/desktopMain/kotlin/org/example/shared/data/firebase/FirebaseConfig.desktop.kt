package org.example.shared.data.firebase

/**
 * Configuration object for Firebase settings.
 * This object provides methods to retrieve emulator settings for various Firebase services.
 */
actual object FirebaseConfig
{

    /**
     * Indicates whether to use the Firebase emulator.
     * @return `true` if the emulator should be used, `false` otherwise.
     */
    fun useEmulator(): Boolean = true

    /**
     * Retrieves the host address for the Firebase emulator.
     * @return The host address as a `String`.
     */
    actual fun getEmulatorHost(): String = "127.0.0.1"

    /**
     * Retrieves the port number for the Firebase Authentication emulator.
     * @return The port number as an `Int`.
     */
    actual fun getAuthEmulatorPort(): Int = 9099

    /**
     * Retrieves the port number for the Firebase Firestore emulator.
     * @return The port number as an `Int`.
     */
    actual fun getFirestoreEmulatorPort(): Int = 8080

    /**
     * Retrieves the port number for the Firebase Storage emulator.
     * @return The port number as an `Int`.
     */
    actual fun getStorageEmulatorPort(): Int = 9199

    /**
     * Retrieves the port number for the Firebase Functions emulator.
     * @return The port number as an `Int`.
     */
    actual fun getFunctionsEmulatorPort(): Int = 5001

    /**
     * Retrieves the Firebase project ID.
     * @return The project ID as a `String`.
     */
    fun getStorageBucket(): String = "learnflexkmp.appspot.com"
}