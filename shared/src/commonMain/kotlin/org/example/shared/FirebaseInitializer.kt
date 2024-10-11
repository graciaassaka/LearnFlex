package org.example.shared

/**
 * Expects a class responsible for initializing Firebase.
 */
expect class FirebaseInitializer {

    /**
     * Initializes Firebase.
     */
    fun initialize()
}