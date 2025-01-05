package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.model.interfaces.DatabaseRecord

/**
 * Base class for remote data sources interacting with Firebase Firestore.
 *
 * @param Model The type of model this data source operates on.
 * @property firestore The Firebase Firestore instance.
 * @property serializer The serializer for the model.
 */
open class FirestoreBaseDao<Model : DatabaseRecord>(
    private val firestore: FirebaseFirestore,
    private val serializer: KSerializer<Model>
) : Dao<Model> {

    /**
     * Creates a new item in the Firestore collection.
     *
     * @param item The item to be created.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun insert(path: String, item: Model, timestamp: Long) = firestore.batch().runCatching {
        set(firestore.collection(path).document(item.id), serializer, item) { encodeDefaults = true }
        updateTimestamps(path + "/${item.id}", timestamp)
        commit()
    }

    /**
     * Fetches an item from the Firestore collection by its ID.
     *
     * @param id The ID of the item to be fetched.
     * @return A [Result] containing the fetched item or an error.
     */
    override fun get(path: String, id: String) = firestore.collection(path)
        .document(id)
        .snapshots()
        .map { snap ->
            try {
                require(snap.exists) { "Document not found" }
                Result.success(snap.data(serializer))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Updates an item in the Firestore collection.
     *
     * @param item The item to be updated.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun update(path: String, item: Model, timestamp: Long) = firestore.batch().runCatching {
        update(firestore.collection(path).document(item.id), serializer, item) { encodeDefaults = true }
        updateTimestamps(path + "/${item.id}", timestamp)
        commit()
    }

    /**
     * Deletes an item from the Firestore collection by its ID.
     *
     * @param item The item to be deleted.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun delete(path: String, item: Model, timestamp: Long) = firestore.batch().runCatching {
        updateTimestamps(path + "/${item.id}", timestamp)
        delete(firestore.collection(path).document(item.id))
        commit()
    }

    /**
     * Updates the lastUpdated timestamp of all parent documents in the path.
     *
     * @param path The path of the document to update.
     * @param timestamp The timestamp to associate with the operation.
     */
    protected fun WriteBatch.updateTimestamps(path: String, timestamp: Long) {
        val segments = path.split("/").filter { it.isNotBlank() }

        if (segments.isNotEmpty()) for (endIndex in segments.size downTo 1 step 2) {
            val parentPath = segments.take(endIndex).joinToString("/")
            val parentRef = firestore.document(parentPath)

            update(parentRef, mapOf("lastUpdated" to timestamp))
        }
    }
}