package org.example.shared.data.firebase

import android.content.Context
import org.example.shared.R

/**
 * Object for Firebase configuration specific to the Android platform.
 */
actual object FirebaseConfig {

    /**
     * Application context used for accessing resources.
     */
    private lateinit var appContext: Context

    /**
     * Initializes the Firebase configuration with the provided context.
     *
     * @param context The application context.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Retrieves the host address for the Firebase emulator.
     *
     * @return The host address as a string.
     */
    actual fun getEmulatorHost(): String = appContext.getString(R.string.firebase_emulator_host)

    /**
     * Retrieves the port number for the Firebase Authentication emulator.
     *
     * @return The port number as an integer.
     */
    actual fun getAuthEmulatorPort(): Int = appContext.resources.getInteger(R.integer.firebase_auth_emulator_port)

    /**
     * Retrieves the port number for the Firebase Firestore emulator.
     *
     * @return The port number as an integer.
     */
    actual fun getFirestoreEmulatorPort(): Int = appContext.resources.getInteger(R.integer.firebase_firestore_emulator_port)

    /**
     * Retrieves the port number for the Firebase Storage emulator.
     *
     * @return The port number as an integer.
     */
    actual fun getStorageEmulatorPort(): Int = appContext.resources.getInteger(R.integer.firebase_storage_emulator_port)

    /**
     * Retrieves the port number for the Firebase Functions emulator.
     *
     * @return The port number as an integer.
     */
    actual fun getFunctionsEmulatorPort(): Int = appContext.resources.getInteger(R.integer.firebase_functions_emulator_port)
}