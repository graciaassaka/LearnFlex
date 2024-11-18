package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.shared.data.remote.util.FirestoreCollection
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningStyle

/**
 * Implementation of the LearningStyleRepos interface for interacting with Firestore.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class LearningStyleRemoteDataSource(private val firestore: FirebaseFirestore) : RemoteDataSource<LearningStyle> {

    /**
     * Fetches a learning style document from Firestore.
     *
     * @param id The ID of the learning style document to fetch.
     * @return The learning style document fetched from Firestore.
     */
    override suspend fun fetch(id: String) = runCatching {
        firestore.collection(FirestoreCollection.LEARNING_STYLES.value)
            .document(id)
            .get()
            .data(LearningStyle.serializer())
    }

    /**
     * Saves a learning style document to Firestore.
     *
     * @param item The learning style document to save.
     * @return A Result indicating success or failure of the operation.
     */
    override suspend fun create(item: LearningStyle): Result<Unit> = item.runCatching {
        firestore.collection(FirestoreCollection.LEARNING_STYLES.value)
            .document(id)
            .set(this)
    }

    /**
     * Deletes a learning style document from Firestore.
     *
     * @param id The ID of the learning style document to delete.
     * @return A Result indicating success or failure of the operation.
     */
    override suspend fun delete(id: String): Result<Unit> = runCatching {
        firestore.collection(FirestoreCollection.LEARNING_STYLES.value)
            .document(id)
            .delete()
    }
}