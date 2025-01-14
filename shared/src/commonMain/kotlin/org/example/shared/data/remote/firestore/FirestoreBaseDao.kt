package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.WriteBatch
import kotlinx.serialization.KSerializer
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.storage_operations.util.Path

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
    override suspend fun insert(item: Model, path: Path, timestamp: Long) = firestore.batch().runCatching {
        set(firestore.document(path.value), serializer, item) { encodeDefaults = true }
        updateTimestamps(path, timestamp)
        commit()
    }

    /**
     * Retrieves an item from the Firestore collection by its ID.
     *
     * @param id The ID of the item to retrieve.
     * @return A [Result] containing the fetched item or an error.
     */
    override suspend fun get(path: Path) = runCatching {
        firestore.document(path.value)
            .get(source = Source.SERVER)
            .data(serializer)
    }


    /**
     * Updates an item in the Firestore collection.
     *
     * @param item The item to be updated.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun update(item: Model, path: Path, timestamp: Long) = firestore.batch().runCatching {
        update(firestore.document(path.value), serializer, item) { encodeDefaults = true }
        updateTimestamps(path, timestamp)
        commit()
    }

    /**
     * Deletes an item from the Firestore collection by its ID.
     *
     * @param item The item to be deleted.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun delete(item: Model, path: Path, timestamp: Long) = firestore.batch().runCatching {
        updateTimestamps(path, timestamp)
        delete(firestore.collection(path.value).document(item.id))
        commit()
    }

    /**
     * Updates the lastUpdated timestamp of all parent documents in the path.
     *
     * @param path The path of the document to update.
     * @param timestamp The timestamp to associate with the operation.
     */
    protected fun WriteBatch.updateTimestamps(path: Path, timestamp: Long) {
        val segments = path.value.split("/").filter { it.isNotBlank() }

        if (segments.isNotEmpty()) for (endIndex in segments.size downTo 1 step 2) {
            val parentPath = segments.take(endIndex).joinToString("/")
            val parentRef = firestore.document(parentPath)

            update(parentRef, mapOf("lastUpdated" to timestamp))
        }
    }
}