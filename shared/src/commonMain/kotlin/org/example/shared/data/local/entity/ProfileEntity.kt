package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.domain.model.Profile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    override val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "photo_url")
    val photoUrl: String,

    @ColumnInfo(name = "preferences")
    val preferences: Profile.LearningPreferences,

    @ColumnInfo(name = "learning_style")
    val learningStyle: Profile.LearningStyle,

    @ColumnInfo(
        name = "created_at",
        defaultValue = "CURRENT_TIMESTAMP"
    )
    override val createdAt: Long,

    @ColumnInfo(
        name = "last_updated",
        defaultValue = "CURRENT_TIMESTAMP",
    )
    override val lastUpdated: Long
) : RoomEntity