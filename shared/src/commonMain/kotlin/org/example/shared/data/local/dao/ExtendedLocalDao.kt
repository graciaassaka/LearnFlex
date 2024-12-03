package org.example.shared.data.local.dao

import androidx.room.*

@Dao
abstract class ExtendedLocalDao<Entity> : LocalDao<Entity>() {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<Entity>)

    @Update
    abstract suspend fun updateAll(items: List<Entity>)

    @Upsert
    abstract suspend fun upsertAll(items: List<Entity>)

    @Delete
    abstract suspend fun deleteAll(items: List<Entity>)
}