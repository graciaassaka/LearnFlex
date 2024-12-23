package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.LessonEntity

@Dao
abstract class LessonLocalDao : ExtendedLocalDao<LessonEntity>() {
    @Query(
        """
            SELECT * FROM lessons
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<LessonEntity?>

    @Query(
        """
            SELECT * FROM lessons
        """
    )
    abstract fun getAll(): Flow<List<LessonEntity>>

    @Query(
        """
            SELECT * FROM lessons
            INNER JOIN modules ON lessons.module_id = modules.id
            WHERE modules.curriculum_id = :curriculumId
        """
    )
    @RewriteQueriesToDropUnusedColumns
    abstract fun getByCurriculumId(curriculumId: String): Flow<List<LessonEntity>>

    @Query(
        """
            SELECT * FROM lessons
            WHERE module_id = :moduleId
        """
    )
    abstract fun getByModuleId(moduleId: String): Flow<List<LessonEntity>>

    @Query(
        """
            SELECT * FROM lessons
            WHERE module_id = :moduleId AND quiz_score >= :minScore
        """
    )
    abstract fun getByMinQuizScore(moduleId: String, minScore: Int): Flow<List<LessonEntity>>
}