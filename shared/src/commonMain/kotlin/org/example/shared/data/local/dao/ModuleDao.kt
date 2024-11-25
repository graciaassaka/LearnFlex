package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.entity.ModuleEntity

@Dao
abstract class ModuleDao : ExtendedDao<ModuleEntity>() {

    @Query(
        """
            SELECT * FROM module
            WHERE id = :id
        """
    )
    abstract suspend fun get(id: String): ModuleEntity?

    @Query(
        """
            SELECT * FROM module
            WHERE curriculum_id = :curriculumId
        """
    )
    abstract suspend fun getModulesByCurriculumId(curriculumId: String): List<ModuleEntity>

    @Query(
        """
            SELECT id FROM module
            WHERE curriculum_id = :curriculumId AND quiz_score >= :minScore
        """
    )
    abstract suspend fun getModuleIdsByMinQuizScore(curriculumId: String, minScore: Int): List<String>
}