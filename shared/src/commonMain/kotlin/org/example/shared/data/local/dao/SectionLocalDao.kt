package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.SectionEntity

@Dao
abstract class SectionLocalDao : ExtendedLocalDao<SectionEntity>() {
    @Query(
        """
            SELECT * FROM sections
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<SectionEntity?>

    @Query(
        """
            SELECT * FROM sections
        """
    )
    abstract fun getAll(): Flow<List<SectionEntity>>

    @Query(
        """
            SELECT * FROM sections
            WHERE lesson_id = :lessonId
       """
    )
    abstract fun getByLessonId(lessonId: String): Flow<List<SectionEntity>>

    @Query(
        """
            SELECT * FROM sections
            INNER JOIN lessons ON sections.lesson_id = lessons.id
            INNER JOIN modules ON lessons.module_id = modules.id
            WHERE modules.curriculum_id = :curriculumId
        """
    )
    @RewriteQueriesToDropUnusedColumns
    abstract fun getByCurriculumId(curriculumId: String): Flow<List<SectionEntity>>

    @Query(
        """
            SELECT * FROM sections 
            WHERE lesson_id = :lessonId AND quiz_score >= :minScore
        """
    )
    abstract fun getByMinQuizScore(lessonId: String, minScore: Int): Flow<List<SectionEntity>>
}