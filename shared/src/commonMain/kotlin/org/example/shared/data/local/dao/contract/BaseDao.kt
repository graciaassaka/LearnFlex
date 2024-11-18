package org.example.shared.data.local.dao.contract

import androidx.room.*

@Dao
interface BaseDao<Entity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Entity)

    @Update
    suspend fun update(item: Entity)

    @Delete
    suspend fun delete(item: Entity)

    suspend fun get(id: String): Entity?
}