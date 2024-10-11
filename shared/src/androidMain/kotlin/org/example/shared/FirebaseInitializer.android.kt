package org.example.shared

import android.content.Context
import com.google.firebase.FirebaseApp

/**
 * Class responsible for initializing Firebase in an Android context.
 *
 * @property context The application context used for Firebase initialization.
 */
actual class FirebaseInitializer(private val context: Context)
{

    /**
     * Initializes Firebase with the provided application context.
     */
    actual fun initialize()
    {
        FirebaseApp.initializeApp(context)
    }
}