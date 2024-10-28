package org.example.shared

import android.app.Application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize
import io.ktor.client.*
import org.example.shared.data.firebase.FirebaseConfig
import org.example.shared.data.util.FirebaseConstants

class FirebaseInit {
    var idToken = ""
    var refreshToken = ""

   init {
        FirebasePlatform.initializeFirebasePlatform(
            object : FirebasePlatform() {
                val storage = mutableMapOf<String, String>()
                override fun store(key: String, value: String) = storage.set(key, value)
                override fun retrieve(key: String) = storage[key]
                override fun clear(key: String) {
                    storage.remove(key)
                }
                override fun log(msg: String) = println(msg)
            },
        )
        Firebase.initialize(
            Application(),
            options = FirebaseOptions(
                applicationId = FirebaseConstants.APP_ID,
                apiKey = FirebaseConstants.API_KEY,
                projectId = FirebaseConstants.PROJECT_ID
            )
        )

        setupEmulator()
    }

    private fun setupEmulator() {
        if (FirebaseConfig.useEmulator()) {
            System.setProperty(
                "firebase.auth.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getAuthEmulatorPort()}"
            )
            System.setProperty(
                "firebase.firestore.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getFirestoreEmulatorPort()}"
            )
            System.setProperty(
                "firebase.storage.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getStorageEmulatorPort()}"
            )
            System.setProperty(
                "firebase.functions.emulator.host",
                "${FirebaseConfig.getEmulatorHost()}:${FirebaseConfig.getFunctionsEmulatorPort()}"
            )
        }
    }
}