package org.example.shared.data.local.dao

import androidx.room.*
import org.example.shared.data.local.entity.definition.RoomEntity

@Dao
abstract class ExtendedLocalDao<Entity : RoomEntity> : LocalDao<Entity>() {
    @Transaction
    open suspend fun insertAll(path: String, items: List<Entity>, timestamp: Long) {
        insertAll(items)
        items.forEach { timestampManager.updateTimestamps(path + "/${it.id}", timestamp) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<Entity>)

    @Transaction
    open suspend fun updateAll(path: String, items: List<Entity>, timestamp: Long) {
        updateAll(items)
        items.forEach { timestampManager.updateTimestamps(path + "/${it.id}", timestamp) }
    }

    @Update
    protected abstract suspend fun updateAll(items: List<Entity>)

    @Transaction
    open suspend fun deleteAll(path: String, items: List<Entity>, timestamp: Long) {
        deleteAll(items)
        items.forEach { timestampManager.updateTimestamps(path + "/${it.id}", timestamp) }
    }

    @Delete
    protected abstract suspend fun deleteAll(items: List<Entity>)
}