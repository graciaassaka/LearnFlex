package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.LessonEntity

@Dao
abstract class LessonLocalDao : ExtendedLocalDao<LessonEntity>() {
    @Query(
        """
            SELECT * FROM lesson
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<LessonEntity?>

    @Query(
        """
            SELECT * FROM lesson
            WHERE module_id = :moduleId
        """
    )
    abstract fun getLessonsByModuleId(moduleId: String): Flow<List<LessonEntity>>

    @Query(
        """
            SELECT id FROM lesson
            WHERE module_id = :moduleId AND quiz_score >= :minScore
        """
    )
    abstract fun getLessonIdsByMinQuizScore(moduleId: String, minScore: Int): Flow<List<String>>
}