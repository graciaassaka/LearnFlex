package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.entity.SectionEntity

@Dao
abstract class SectionDao : ExtendedDao<SectionEntity>() {
    @Query(
        """
            SELECT * FROM section
            WHERE id = :id
        """
    )
    abstract suspend fun get(id: String): SectionEntity?

    @Query(
        """
            SELECT * FROM section
            WHERE lesson_id = :lessonId
            ORDER BY `index`
       """
    )
    abstract suspend fun getSectionsByLessonId(lessonId: String): List<SectionEntity>

    @Query(
        """
            SELECT id FROM section 
            WHERE lesson_id = :lessonId AND quiz_score >= :minScore
        """
    )
    abstract suspend fun getSectionIdsByMinQuizScore(lessonId: String, minScore: Int): List<String>
}