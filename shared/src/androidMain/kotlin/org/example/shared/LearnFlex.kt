package org.example.shared

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.google.firebase.storage.storage
import org.example.shared.data.remote.firebase.FirebaseConfig
import org.example.shared.injection.initKoin

/**
 * Application class for the LearnFlex app.
 */
class LearnFlex : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)
        setupFirebaseEmulators()


        initKoin(this)
    }

    private fun setupFirebaseEmulators() {
        if (BuildConfig.DEBUG) {
            val emulatorHost = FirebaseConfig.emulatorHost
            try {
                Log.d("LearnFlex", "Using Firebase Emulators")
                Firebase.auth.useEmulator(emulatorHost, FirebaseConfig.authEmulatorPort)
                Firebase.firestore.useEmulator(emulatorHost, FirebaseConfig.firestoreEmulatorPort)
                Firebase.storage.useEmulator(emulatorHost, FirebaseConfig.storageEmulatorPort)
            } catch (e: Exception) {
                Log.e("LearnFlex", "Error setting up Firebase emulators", e)
            }
        }
    }
}