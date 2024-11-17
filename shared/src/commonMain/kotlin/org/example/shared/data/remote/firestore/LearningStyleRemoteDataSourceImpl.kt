package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.shared.domain.model.StyleResult
import org.example.shared.data.util.FirestoreCollection
import org.example.shared.domain.data_source.LearningStyleRemoteDataSource

/**
 * Implementation of the LearningStyleRepos interface for interacting with Firestore.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class LearningStyleRemoteDataSourceImpl(private val firestore: FirebaseFirestore): LearningStyleRemoteDataSource {

    /**
     * Retrieves a learning style document from Firestore.
     *
     * @param id The ID of the learning style document to retrieve.
     * @return A Result containing the StyleResult data or an exception if the operation fails.
     */
    override suspend fun fetchLearningStyle(id: String)= runCatching {
        firestore.collection(FirestoreCollection.LEARNING_STYLES.value)
            .document(id)
            .get()
            .data(StyleResult.serializer())
    }

    /**
     * Sets a learning style document in Firestore.
     *
     * @param id The ID of the learning style document to set.
     * @param style The StyleResult data to set in the document.
     * @return A Result indicating success or failure of the operation.
     */
    override suspend fun setLearningStyle(id: String, style: StyleResult): Result<Unit> = runCatching {
        firestore.collection(FirestoreCollection.LEARNING_STYLES.value)
            .document(id)
            .set(style)
    }

    /**
     * Deletes a learning style document from Firestore.
     *
     * @param id The ID of the learning style document to delete.
     * @return A Result indicating success or failure of the operation.
     */
    override suspend fun deleteLearningStyle(id: String): Result<Unit> = runCatching {
        firestore.collection(FirestoreCollection.LEARNING_STYLES.value)
            .document(id)
            .delete()
    }
}