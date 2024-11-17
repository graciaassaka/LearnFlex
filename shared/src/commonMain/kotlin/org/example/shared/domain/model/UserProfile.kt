package org.example.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
open class UserProfile(
    open val id: String,
    open val username: String,
    open val email: String,
    open val photoUrl: String,
    open val preferences: LearningPreferences,
    open val createdAt: Long,
    open val lastUpdated: Long
)