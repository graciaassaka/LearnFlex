package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.ModuleEntity

@Dao
abstract class ModuleLocalDao : ExtendedLocalDao<ModuleEntity>() {
    @Query(
        """
            SELECT * FROM modules
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<ModuleEntity?>

    @Query(
        """
            SELECT * FROM modules
        """
    )
    abstract fun getAll(): Flow<List<ModuleEntity>>

    @Query(
        """
            SELECT * FROM modules
            WHERE curriculum_id = :curriculumId
        """
    )
    abstract fun getByCurriculumId(curriculumId: String): Flow<List<ModuleEntity>>

    @Query(
        """
            SELECT * FROM modules
            WHERE curriculum_id = :curriculumId AND quiz_score >= :minScore
        """
    )
    abstract fun getByMinQuizScore(curriculumId: String, minScore: Int): Flow<List<ModuleEntity>>
}