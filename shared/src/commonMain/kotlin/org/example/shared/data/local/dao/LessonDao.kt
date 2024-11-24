package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.entity.LessonEntity

@Dao
abstract class LessonDao : ExtendedDao<LessonEntity>() {
    @Query(
        """
            SELECT * FROM lesson
            WHERE module_id = :moduleId
        """
    )
    abstract suspend fun getLessonsByModuleId(moduleId: String): List<LessonEntity>

    @Query(
        """
            SELECT id FROM lesson
            WHERE module_id = :moduleId AND quiz_score >= :minScore
        """
    )
    abstract suspend fun getLessonIdsByMinQuizScore(moduleId: String, minScore: Int): List<String>
}