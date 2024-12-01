package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.KSerializer
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * Base class for remote data sources interacting with Firebase Firestore.
 *
 * @param Model The type of model this data source operates on.
 * @property firestore The Firebase Firestore instance.
 * @property serializer The serializer for the model.
 */
open class RemoteDataSourceImpl<Model : DatabaseRecord>(
    private val firestore: FirebaseFirestore,
    private val serializer: KSerializer<Model>
) : RemoteDataSource<Model> {

    /**
     * Creates a new item in the Firestore collection.
     *
     * @param item The item to be created.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun create(path: String, item: Model) = item.runCatching {
        firestore.collection(path)
            .document(id)
            .set(serializer, this) {
                encodeDefaults = true
            }
    }

    /**
     * Fetches an item from the Firestore collection by its ID.
     *
     * @param id The ID of the item to be fetched.
     * @return A [Result] containing the fetched item or an error.
     */
    override suspend fun fetch(path: String, id: String) = runCatching {
        firestore.collection(path)
            .document(id)
            .get()
            .data(serializer)
    }

    /**
     * Updates an item in the Firestore collection.
     *
     * @param item The item to be updated.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun update(path: String, item: Model) = item.runCatching {
        firestore.collection(path)
            .document(id)
            .set(serializer, this) {
                encodeDefaults = true
            }
    }

    /**
     * Deletes an item from the Firestore collection by its ID.
     *
     * @param id The ID of the item to be deleted.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun delete(path: String, id: String) = runCatching {
        firestore.collection(path)
            .document(id)
            .delete()
    }
}