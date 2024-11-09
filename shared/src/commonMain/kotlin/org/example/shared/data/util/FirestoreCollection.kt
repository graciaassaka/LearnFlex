package org.example.shared.data.util

/**
 * Enum representing Firestore collections.
 *
 * @property value The name of the Firestore collection.
 */
enum class FirestoreCollection(val value: String) {
    LEARNING_STYLES("learningStyles"),
    USERS("users")
}