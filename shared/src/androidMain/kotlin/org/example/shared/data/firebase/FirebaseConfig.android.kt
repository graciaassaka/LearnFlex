package org.example.shared.data.firebase

import android.content.Context
import org.example.shared.R

/**
 * Object for Firebase configuration specific to the Android platform.
 */
actual object FirebaseConfig
{

    /**
     * Application context used for accessing resources.
     */
    private lateinit var appContext: Context

    /**
     * Initializes the Firebase configuration with the provided context.
     *
     * @param context The application context.
     */
    fun initialize(context: Context)
    {
        appContext = context.applicationContext
    }

    /**
     * The host address for the Firebase emulator.
     */
    actual var emulatorHost: String = appContext.getString(R.string.firebase_emulator_host)

    /**
     * The port number for the Firebase Authentication emulator.
     */
    actual var authEmulatorPort: Int = appContext.resources.getInteger(R.integer.firebase_auth_emulator_port)

    /**
     * The port number for the Firebase Firestore emulator.
     */
    actual var firestoreEmulatorPort: Int = appContext.resources.getInteger(R.integer.firebase_firestore_emulator_port)

    /**
     * The port number for the Firebase Storage emulator.
     */
    actual var storageEmulatorPort: Int = appContext.resources.getInteger(R.integer.firebase_storage_emulator_port)

    /**
     * The port number for the Firebase Functions emulator.
     */
    actual var functionsEmulatorPort: Int = appContext.resources.getInteger(R.integer.firebase_functions_emulator_port)
}