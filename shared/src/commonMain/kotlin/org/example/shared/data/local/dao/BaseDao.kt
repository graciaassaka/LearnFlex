package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
abstract class BaseDao<Entity> {
    @Insert
    abstract suspend fun insert(item: Entity)

    @Update
    abstract suspend fun update(item: Entity)

    @Delete
    abstract suspend fun delete(item: Entity)
}