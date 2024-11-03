package org.example.shared.data.firebase

/**
 * Object for Firebase configuration.
 */
expect object FirebaseConfig
{

    /**
     * Retrieves the host address for the Firebase emulator.
     *
     * @return The host address as a string.
     */
    var emulatorHost: String

    /**
     * Retrieves the port number for the Firebase Authentication emulator.
     *
     * @return The port number as an integer.
     */
    var authEmulatorPort: Int

    /**
     * Retrieves the port number for the Firebase Firestore emulator.
     *
     * @return The port number as an integer.
     */
    var firestoreEmulatorPort: Int

    /**
     * Retrieves the port number for the Firebase Storage emulator.
     *
     * @return The port number as an integer.
     */
    var storageEmulatorPort: Int

    /**
     * Retrieves the port number for the Firebase Functions emulator.
     *
     * @return The port number as an integer.
     */
    var functionsEmulatorPort: Int
}