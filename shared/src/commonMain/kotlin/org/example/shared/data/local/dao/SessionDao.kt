package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.entity.SessionEntity

@Dao
abstract class SessionDao : ExtendedDao<SessionEntity>() {
    @Query(
        """
        SELECT * FROM session
        WHERE id = :id
        """
    )
    abstract suspend fun get(id: String): SessionEntity?

    @Query(
        """
        SELECT * FROM session
        WHERE start_time_ms >= :startDate AND end_time_ms <= :endDate
        """
    )
    abstract suspend fun getSessionsByDateRange(
        startDate: Long,
        endDate: Long
    ): List<SessionEntity>
}