package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.entity.CachedImageEntity

@Dao
abstract class CachedImageDao : BaseDao<CachedImageEntity>() {
    @Query(
        """
            SELECT * FROM cached_image
            WHERE id = :id
        """
    )
    abstract suspend fun get(id: String): CachedImageEntity?

    @Query(
        """
            DELETE FROM cached_image
            WHERE last_accessed < :timestamp
        """
    )
    abstract suspend fun deleteOlderThan(timestamp: Long)

    @Query(
        """
            SELECT SUM(file_size) FROM cached_image
        """
    )
    abstract suspend fun getTotalCacheSize(): Long
}