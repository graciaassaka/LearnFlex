package org.example.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey override val id: String,
    override val username: String,
    override val email: String,
    override val photoUrl: String,
    override val preferences: LearningPreferences,
    override val createdAt: Long,
    override val lastUpdated: Long
) : UserProfile(id, username, email, photoUrl, preferences, createdAt, lastUpdated) {
    companion object {
        fun fromUserProfile(userProfile: UserProfile) = UserProfileEntity(
            userProfile.id,
            userProfile.username,
            userProfile.email,
            userProfile.photoUrl,
            userProfile.preferences,
            userProfile.createdAt,
            userProfile.lastUpdated
        )
    }

    fun toUserProfile() = UserProfile(id, username, email, photoUrl, preferences, createdAt, lastUpdated)
}
