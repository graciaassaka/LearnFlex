package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.SessionEntity

@Dao
abstract class SessionLocalDao : ExtendedLocalDao<SessionEntity>() {
    @Query(
        """
            SELECT * FROM session
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<SessionEntity?>

    @Query(
        """
            SELECT * FROM session
        """
    )
    abstract fun getAll(): Flow<List<SessionEntity>>

    @Query(
        """
            SELECT * FROM session
            WHERE created_at >= :startDate AND end_time_ms <= :endDate
        """
    )
    abstract fun getSessionsByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<SessionEntity>>
}