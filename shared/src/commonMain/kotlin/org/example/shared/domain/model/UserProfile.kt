package org.example.shared.domain.model

import kotlinx.serialization.Serializable
import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * A data class representing a user profile.
 *
 * @property id The unique identifier of the user.
 * @property username The username of the user.
 * @property email The email of the user.
 * @property photoUrl The URL of the user's photo.
 * @property preferences The learning preferences of the user.
 * @property createdAt The timestamp when the user was created.
 * @property lastUpdated The timestamp when the user was last updated.
 */
@Serializable
open class UserProfile(
    override val id: String,
    open val username: String,
    open val email: String,
    open val photoUrl: String,
    open val preferences: LearningPreferences,
    override val createdAt: Long = System.currentTimeMillis(),
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord