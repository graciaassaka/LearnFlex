package org.example.shared.data.remote.firebase

/**
 * Configuration object for Firebase settings.
 * This object provides properties to retrieve and set emulator settings for various Firebase services.
 */
actual object FirebaseConfig {

    /**
     * Indicates whether to use the Firebase emulator.
     */
    var useEmulator: Boolean = false

    /**
     * The host address for the Firebase emulator.
     */
    actual var emulatorHost: String = "127.0.0.1"

    /**
     * The port number for the Firebase Authentication emulator.
     */
    actual var authEmulatorPort: Int = 9099

    /**
     * The port number for the Firebase Firestore emulator.
     */
    actual var firestoreEmulatorPort: Int = 8080

    /**
     * The port number for the Firebase Storage emulator.
     */
    actual var storageEmulatorPort: Int = 9199

    /**
     * The port number for the Firebase Functions emulator.
     */
    actual var functionsEmulatorPort: Int = 5001

    /**
     * The Firebase storage bucket name.
     */
    var storageBucket: String = "learnflexkmp.appspot.com"
}
