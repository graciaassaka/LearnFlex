package org.example.shared.domain.data_source

import org.example.shared.domain.model.UserProfile

/**
 * Interface for user profile repository operations.
 */
interface UserProfileRemoteDataSource {
    /**
     * Creates a new user profile.
     * @param userProfile The user profile to create.
     * @return A [Result] indicating success or failure.
     */
    suspend fun setUserProfile(userProfile: UserProfile): Result<Unit>

    /**
     * Retrieves a user profile by ID.
     * @param id The ID of the user profile to retrieve.
     * @return A [Result] containing the user profile or an error.
     */
    suspend fun fetchUserProfile(id: String): Result<UserProfile>

    /**
     * Deletes a user profile by ID.
     * @param id The ID of the user profile to delete.
     * @return A [Result] indicating success or failure.
     */
    suspend fun deleteUserProfile(id: String): Result<Unit>
}