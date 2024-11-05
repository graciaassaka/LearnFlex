package org.example.shared.domain.repository

import org.example.shared.data.model.UserProfile

/**
 * Interface for user profile repository operations.
 */
interface UserProfileRepos {
    /**
     * Creates a new user profile.
     * @param userProfile The user profile to create.
     * @return A [Result] indicating success or failure.
     */
    suspend fun createUserProfile(userProfile: UserProfile): Result<Unit>

    /**
     * Retrieves a user profile by ID.
     * @param id The ID of the user profile to retrieve.
     * @return A [Result] containing the user profile or an error.
     */
    suspend fun getUserProfile(id: String): Result<UserProfile>

    /**
     * Updates an existing user profile.
     * @param userProfile The user profile to update.
     * @return A [Result] indicating success or failure.
     */
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit>

    /**
     * Deletes a user profile by ID.
     * @param id The ID of the user profile to delete.
     * @return A [Result] indicating success or failure.
     */
    suspend fun deleteUserProfile(id: String): Result<Unit>
}