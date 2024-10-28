package org.example.shared.data.model

import dev.gitlive.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.Serializable

/**
 * Data class representing a user profile.
 */
@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val email: String,
    val photoUrl: String,
    val preferences: LearningPreferences
)
