package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.model.UserProfile

/**
 * Entity class representing a user profile in the local database.
 *
 * @property id The unique identifier of the user profile.
 * @property username The username of the user.
 * @property email The email address of the user.
 * @property photoUrl The URL of the user's profile photo.
 * @property preferences The learning preferences of the user.
 * @property createdAt The timestamp when the user profile was created.
 * @property lastUpdated The timestamp when the user profile was last updated.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    override val id: String,

    @ColumnInfo(name = "username")
    override val username: String,

    @ColumnInfo(name = "email")
    override val email: String,

    @ColumnInfo(name = "photo_url")
    override val photoUrl: String,

    @ColumnInfo(name = "preferences")
    override val preferences: LearningPreferences,

    @ColumnInfo(name = "learning_style")
    override val learningStyle: LearningStyle,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long
) : UserProfile(id, username, email, photoUrl, preferences, learningStyle, createdAt, lastUpdated), RoomEntity