package org.example.shared.data.local.dao

import androidx.room.*
import org.example.shared.data.local.dao.util.TimestampManager
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.koin.java.KoinJavaComponent.inject

@Dao
abstract class LocalDao<Entity : RoomEntity> {
    protected val timestampManager: TimestampManager by inject(TimestampManager::class.java)

    @Transaction
    open suspend fun insert(path: String, item: Entity, timestamp: Long) {
        insert(item)
        timestampManager.updateTimestamps(path + "/${item.id}", timestamp)
    }

    @Insert
    protected abstract suspend fun insert(item: Entity)

    @Transaction
    open suspend fun update(path: String, item: Entity, timestamp: Long) {
        update(item)
        timestampManager.updateTimestamps(path + "/${item.id}", timestamp)
    }

    @Update
    protected abstract suspend fun update(item: Entity)

    @Transaction
    open suspend fun delete(path: String, item: Entity, timestamp: Long) {
        delete(item)
        timestampManager.updateTimestamps(path + "/${item.id}", timestamp)
    }

    @Delete
    protected abstract suspend fun delete(item: Entity)
}