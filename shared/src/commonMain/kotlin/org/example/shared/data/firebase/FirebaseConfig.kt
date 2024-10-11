package org.example.shared.data.firebase

/**
 * Object for Firebase configuration.
 */
expect object FirebaseConfig {

    /**
     * Checks if the Firebase emulator should be used.
     *
     * @return `true` if the Firebase emulator should be used, `false` otherwise.
     */
    fun useEmulator(): Boolean

    /**
     * Retrieves the host address for the Firebase emulator.
     *
     * @return The host address as a string.
     */
    fun getEmulatorHost(): String

    /**
     * Retrieves the port number for the Firebase Authentication emulator.
     *
     * @return The port number as an integer.
     */
    fun getAuthEmulatorPort(): Int

    /**
     * Retrieves the port number for the Firebase Firestore emulator.
     *
     * @return The port number as an integer.
     */
    fun getFirestoreEmulatorPort(): Int

    /**
     * Retrieves the port number for the Firebase Storage emulator.
     *
     * @return The port number as an integer.
     */
    fun getStorageEmulatorPort(): Int

    /**
     * Retrieves the port number for the Firebase Functions emulator.
     *
     * @return The port number as an integer.
     */
    fun getFunctionsEmulatorPort(): Int
}