package org.example.shared

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize
import org.example.shared.data.firebase.FirebaseConfig

class FirebaseInit
{
    private val firebase = Firebase

    fun initialize() {
        firebase.initialize(
            FirebaseOptions(
                applicationId = EnvConfig.getOrThrow("FIREBASE_APP_ID"),
                apiKey = if (FirebaseConfig.useEmulator()) "fake-api-key-for-emulator"
                else EnvConfig.getOrThrow("FIREBASE_API_KEY"),
                projectId = EnvConfig.getOrThrow("FIREBASE_PROJECT_ID"),
            )
        )

        setupEmulator()
    }

    private fun setupEmulator()
    {
        if (FirebaseConfig.useEmulator())
        {
            Firebase.auth.useEmulator(
                host = FirebaseConfig.getEmulatorHost(),
                port = FirebaseConfig.getAuthEmulatorPort()
            )
        }
    }
}