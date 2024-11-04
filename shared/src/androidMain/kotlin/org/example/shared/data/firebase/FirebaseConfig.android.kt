package org.example.shared.data.firebase

/**
 * Object for Firebase configuration specific to the Android platform.
 */
actual object FirebaseConfig
{
    /**
     * The host address for the Firebase emulator.
     */
    actual var emulatorHost: String = "10.0.2.2"

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
}