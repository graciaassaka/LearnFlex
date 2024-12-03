package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.CurriculumEntity

@Dao
abstract class CurriculumLocalDao : ExtendedLocalDao<CurriculumEntity>() {
    @Query(
        """
            SELECT * FROM curriculum
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<CurriculumEntity?>

    @Query(
        """
            SELECT * FROM curriculum
        """
    )
    abstract fun getAll(): Flow<List<CurriculumEntity>>

    @Query(
        """
            SELECT * FROM curriculum
            WHERE status = :status
        """
    )
    abstract fun getCurriculaByStatus(status: String): Flow<List<CurriculumEntity>>
}