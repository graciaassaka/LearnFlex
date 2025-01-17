package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.interfaces.DatabaseRecord
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Profile(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String,

    @SerialName("photo_url")
    val photoUrl: String,

    @SerialName("preferences")
    val preferences: LearningPreferences,

    @SerialName("learning_style")
    val learningStyle: LearningStyle = LearningStyle(),

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord {
    /**
     * Represents the learning style.
     *
     * @property dominant The dominant style identified.
     * @property breakdown The breakdown of styles.
     */
    @Serializable
    data class LearningStyle(
        val dominant: String = "",
        val breakdown: LearningStyleBreakdown = LearningStyleBreakdown()
    ) {
        init {
            require(dominant.isEmpty() || dominant in Style.entries.map { it.name }) {
                "Invalid dominant style: $dominant"
            }
        }

        constructor(dominant: Style, breakdown: LearningStyleBreakdown) :
                this(dominant.name, breakdown)
    }

    /**
     * Represents the breakdown of different styles.
     *
     * @property reading The score for reading style.
     * @property kinesthetic The score for kinesthetic style.
     */
    @Serializable
    data class LearningStyleBreakdown(
        val reading: Int = 0,
        val kinesthetic: Int = 0
    ) {
        init {
            require(reading >= 0) { "Invalid reading score: $reading" }
            require(kinesthetic >= 0) { "Invalid kinesthetic score: $kinesthetic" }
        }
    }

    /**
     * A data class representing learning preferences.
     *
     * @property field The field of study.
     * @property level The level of expertise.
     * @property goal The learning goal.
     */
    @Serializable
    data class LearningPreferences(
        val field: String = "",
        val level: String = "",
        val goal: String = ""
    ) {
        init {
            require(field.isEmpty() || field in Field.entries.map { it.name }) { "Invalid field: $field" }
            require(level.isEmpty() || level in Level.entries.map { it.name }) { "Invalid level: $level" }
        }

        constructor(field: Field, level: Level, goal: String) :
                this(field.name, level.name, goal)
    }
}