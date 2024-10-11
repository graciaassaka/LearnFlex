package org.example.shared

import android.app.Application
import android.util.Log
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import com.google.firebase.storage.storage
import org.example.shared.data.firebase.FirebaseConfig
import org.example.shared.injection.initKoin

/**
 * Application class for the LearnFlex app.
 */
class LearnFlex: Application()
{
    override fun onCreate()
    {
        super.onCreate()
        FirebaseInitializer(this).initialize()
        FirebaseConfig.initialize(this)
        setupFirebaseEmulators()
        initKoin(this)
    }

    private fun setupFirebaseEmulators()
    {
        if (BuildConfig.DEBUG || FirebaseConfig.useEmulator())
        {
            val emulatorHost = FirebaseConfig.getEmulatorHost()
            try
            {
                Log.d("LearnFlex", "Using Firebase Emulators")
                Firebase.auth.useEmulator(emulatorHost, FirebaseConfig.getAuthEmulatorPort())
                Firebase.functions.useEmulator(emulatorHost, FirebaseConfig.getFunctionsEmulatorPort())
                Firebase.firestore.useEmulator(emulatorHost, FirebaseConfig.getFirestoreEmulatorPort())
                Firebase.storage.useEmulator(emulatorHost, FirebaseConfig.getStorageEmulatorPort())
            } catch (e: Exception)
            {
                Log.e("LearnFlex", "Error setting up Firebase emulators", e)
            }
        }
    }
}