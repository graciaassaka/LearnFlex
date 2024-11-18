package org.example.shared.data.local.dao.contract

import androidx.room.*

@Dao
interface ExtendedDao<Entity> : BaseDao<Entity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg items: Entity)

    @Update
    suspend fun updateAll(vararg items: Entity)

    @Delete
    suspend fun deleteAll(vararg items: Entity)
}