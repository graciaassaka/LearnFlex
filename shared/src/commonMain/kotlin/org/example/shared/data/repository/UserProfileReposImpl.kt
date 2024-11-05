package org.example.shared.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.shared.data.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepos

/**
 * Implementation of the UserProfileRepos interface using Firebase Firestore.
 *
 * @property firestore The Firebase Firestore instance.
 */
class UserProfileReposImpl(private val firestore: FirebaseFirestore) : UserProfileRepos {

    /**
     * Creates a new user profile in the Firestore database.
     *
     * @param userProfile The user profile to create.
     * @return A Result indicating success or failure.
     */
    override suspend fun createUserProfile(userProfile: UserProfile) = runCatching {
        firestore.collection("users")
            .document(userProfile.id)
            .set(UserProfile.serializer(), userProfile) {
                encodeDefaults = true
            }
    }

    /**
     * Retrieves a user profile from the Firestore database by ID.
     *
     * @param id The ID of the user profile to retrieve.
     * @return A Result containing the user profile or an error.
     */
    override suspend fun getUserProfile(id: String) = runCatching {
        firestore.collection("users")
            .document(id)
            .get()
            .data(UserProfile.serializer())
    }

    /**
     * Updates an existing user profile in the Firestore database.
     *
     * @param userProfile The user profile to update.
     * @return A Result indicating success or failure.
     */
    override suspend fun updateUserProfile(userProfile: UserProfile) = runCatching {
        firestore.collection("users")
            .document(userProfile.id)
            .set(userProfile)
    }

    /**
     * Deletes a user profile from the Firestore database by ID.
     *
     * @param id The ID of the user profile to delete.
     * @return A Result indicating success or failure.
     */
    override suspend fun deleteUserProfile(id: String) = runCatching {
        firestore.collection("users")
            .document(id)
            .delete()
    }
}