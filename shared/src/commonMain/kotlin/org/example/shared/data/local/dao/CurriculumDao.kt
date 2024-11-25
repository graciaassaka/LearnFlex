package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.entity.CurriculumEntity

@Dao
abstract class CurriculumDao : ExtendedDao<CurriculumEntity>() {
    @Query(
        """
            SELECT * FROM curriculum
            WHERE id = :id
        """
    )
    abstract suspend fun get(id: String): CurriculumEntity?

    @Query(
        """
            SELECT * FROM curriculum
            WHERE status = :status
        """
    )
    abstract suspend fun getCurriculumsByStatus(status: String): List<CurriculumEntity>

    @Query(
        """
            SELECT COUNT(*) FROM curriculum
            WHERE status = :status
        """
    )
    abstract suspend fun countCurriculumsByStatus(status: String): Int
}