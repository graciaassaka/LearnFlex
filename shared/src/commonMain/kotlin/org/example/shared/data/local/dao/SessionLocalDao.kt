package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.SessionEntity

@Dao
abstract class SessionLocalDao : ExtendedLocalDao<SessionEntity>() {
    @Query(
        """
            SELECT * FROM sessions
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<SessionEntity?>

    @Query(
        """
            SELECT * FROM sessions
        """
    )
    abstract fun getAll(): Flow<List<SessionEntity>>

    @Query(
        """
            SELECT * FROM sessions
            WHERE user_id = :userId
        """
    )
    abstract fun getByUserId(userId: String): Flow<List<SessionEntity>>
}