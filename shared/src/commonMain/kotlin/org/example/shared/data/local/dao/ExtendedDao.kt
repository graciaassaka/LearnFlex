package org.example.shared.data.local.dao

import androidx.room.*

@Dao
abstract class ExtendedDao<Entity> : BaseDao<Entity>() {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(vararg items: Entity)

    @Update
    abstract suspend fun updateAll(vararg items: Entity)

    @Delete
    abstract suspend fun deleteAll(vararg items: Entity)
}