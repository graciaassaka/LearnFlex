package org.example.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun createUserProfile(userProfile: UserProfile): Result<Unit>
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit>
    fun getUserProfile(id: String): Flow<Result<UserProfile>>
    suspend fun deleteUserProfile(userProfile: UserProfile): Result<Unit>
}