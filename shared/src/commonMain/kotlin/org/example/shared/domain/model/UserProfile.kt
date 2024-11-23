package org.example.shared.domain.model

import kotlinx.serialization.SerialName
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
 * @property learningStyle The learning style of the user.
 * @property createdAt The timestamp when the user was created.
 * @property lastUpdated The timestamp when the user was last updated.
 */
@Serializable
open class UserProfile(
    @SerialName("id")
    override val id: String,

    @SerialName("username")
    open val username: String,

    @SerialName("email")
    open val email: String,

    @SerialName("photo_url")
    open val photoUrl: String,

    @SerialName("preferences")
    open val preferences: LearningPreferences,

    @SerialName("learning_style")
    open val learningStyle: LearningStyle,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord