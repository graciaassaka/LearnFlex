package org.example.shared.data.remote.firebase.util

import org.example.shared.FirebaseInit

// TestFirebaseUtil.kt
object TestFirebaseUtil {
    private var isInitialized = false
    private lateinit var firebaseInit: FirebaseInit

    fun getFirebaseInit(): FirebaseInit {
        if (!isInitialized) {
            try {
                firebaseInit = FirebaseInit().apply {
                    idToken = "testIdToken"
                }
                isInitialized = true
            } catch (e: IllegalStateException) {
                firebaseInit = FirebaseInit(skipInit = true)
            }
        }
        return firebaseInit
    }

    fun cleanup() {
        if (isInitialized) {
            isInitialized = false
        }
    }
}