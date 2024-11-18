package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.shared.data.remote.util.FirestoreCollection
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.UserProfile

/**
 * Implementation of the UserProfileRepos interface using Firebase Firestore.
 *
 * @property firestore The Firebase Firestore instance.
 */
class UserProfileRemoteDataSource(private val firestore: FirebaseFirestore) : RemoteDataSource<UserProfile> {

    /**
     * Creates a new user profile in the Firestore database.
     *
     * @param item The user profile to create.
     * @return A Result indicating success or failure.
     */
    override suspend fun create(item: UserProfile) = runCatching {
        firestore.collection(FirestoreCollection.USERS.value)
            .document(item.id)
            .set(UserProfile.serializer(), item) {
                encodeDefaults = true
            }
    }

    /**
     * Retrieves a user profile from the Firestore database by ID.
     *
     * @param id The ID of the user profile to retrieve.
     * @return A Result containing the user profile or an error.
     */
    override suspend fun fetch(id: String) = runCatching {
        firestore.collection(FirestoreCollection.USERS.value)
            .document(id)
            .get()
            .data(UserProfile.serializer())
    }

    /**
     * Deletes a user profile from the Firestore database by ID.
     *
     * @param id The ID of the user profile to delete.
     * @return A Result indicating success or failure.
     */
    override suspend fun delete(id: String) = runCatching {
        firestore.collection(FirestoreCollection.USERS.value)
            .document(id)
            .delete()
    }
}
