package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Source
import kotlinx.serialization.KSerializer
import org.example.shared.domain.dao.ExtendedDao
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.storage_operations.util.Path
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Extension of FirestoreBaseDao that adds support for batch operations in Firestore.
 *
 * @param Model The type of model this DAO operates on, which must implement the DatabaseRecord interface.
 * @property firestore The Firebase Firestore instance to interact with.
 * @property serializer The serializer to convert between model objects and Firestore documents.
 */
open class FirestoreExtendedDao<Model : DatabaseRecord>(
    private val firestore: FirebaseFirestore,
    private val serializer: KSerializer<Model>
) : FirestoreBaseDao<Model>(firestore, serializer),
    ExtendedDao<Model> {
    /**
     * Inserts multiple items into the specified Firestore collection path using a batch operation.
     *
     * @param path The Firestore collection path where the items should be inserted.
     * @param items The items to be inserted, each conforming to the Model type.
     * @param timestamp The timestamp to associate with the operation.
     */
    override suspend fun insertAll(items: List<Model>, path: Path, timestamp: Long) = firestore.batch().runCatching {
        items.forEach {
            val documentPath = PathBuilder(path).document(it.id).build()
            set(firestore.document(documentPath.value), serializer, it) { encodeDefaults = true }
            updateTimestamps(documentPath, timestamp)
        }
        commit()
    }

    /**
     * Updates multiple items in a Firestore collection at the specified path using a batch operation.
     *
     * @param path The Firestore collection path where the items are located.
     * @param items The items to be updated in the Firestore collection.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating the success or failure of the update operation.
     */
    override suspend fun updateAll(items: List<Model>, path: Path, timestamp: Long) = firestore.batch().runCatching {
        items.forEach {
            val documentPath = PathBuilder(path).document(it.id).build()
            update(firestore.document(documentPath.value), serializer, it) { encodeDefaults = true }
            updateTimestamps(documentPath, timestamp)
        }
        commit()
    }

    /**
     * Deletes multiple items from a specified Firestore collection.
     *
     * @param path The path of the Firestore collection from which the items will be deleted.
     * @param items The items to be deleted, each represented by a [Model] instance.
     * @param timestamp The timestamp to associate with the operation.
     */
    override suspend fun deleteAll(items: List<Model>, path: Path, timestamp: Long) = firestore.batch().runCatching {
        items.forEach {
            val documentPath = PathBuilder(path).document(it.id).build()
            updateTimestamps(documentPath, timestamp)
            delete(firestore.document(documentPath.value))
        }
        commit()
    }

    /**
     * Retrieves all documents from a specified Firestore collection path.
     *
     * @param path The Firestore collection path from which documents are to be retrieved.
     * @return A list of all documents in the specified collection.
     */
    override suspend fun getAll(path: Path) = runCatching {
        firestore.collection(path.value)
            .get(source = Source.SERVER)
            .documents.map { it.data(serializer) }
    }
}