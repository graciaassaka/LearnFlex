package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.CurriculumEntity

@Dao
abstract class CurriculumLocalDao : ExtendedLocalDao<CurriculumEntity>() {
    @Query(
        """
            SELECT * FROM curricula
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<CurriculumEntity?>

    @Query(
        """
            SELECT * FROM curricula
        """
    )
    abstract fun getAll(): Flow<List<CurriculumEntity>>

    @Query(
        """
            SELECT * FROM curricula
            WHERE user_id = :userId
        """
    )
    abstract fun getByUserId(userId: String): Flow<List<CurriculumEntity>>
}