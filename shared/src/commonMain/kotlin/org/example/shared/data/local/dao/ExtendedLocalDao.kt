package org.example.shared.data.local.dao

import androidx.room.*
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.domain.storage_operations.util.Path
import org.example.shared.domain.storage_operations.util.PathBuilder

@Dao
abstract class ExtendedLocalDao<Entity : RoomEntity> : LocalDao<Entity>() {
    @Transaction
    open suspend fun insertAll(path: Path, items: List<Entity>, timestamp: Long) {
        insertAll(items)
        items.forEach { timestampManager.updateTimestamps(PathBuilder(path).document(it.id).build(), timestamp) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<Entity>)

    @Transaction
    open suspend fun updateAll(path: Path, items: List<Entity>, timestamp: Long) {
        updateAll(items)
        items.forEach { timestampManager.updateTimestamps(PathBuilder(path).document(it.id).build(), timestamp) }
    }

    @Update
    protected abstract suspend fun updateAll(items: List<Entity>)

    @Transaction
    open suspend fun deleteAll(path: Path, items: List<Entity>, timestamp: Long) {
        deleteAll(items)
        items.forEach { timestampManager.updateTimestamps(PathBuilder(path).document(it.id).build(), timestamp) }
    }

    @Delete
    protected abstract suspend fun deleteAll(items: List<Entity>)
}