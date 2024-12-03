package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.SectionEntity

@Dao
abstract class SectionLocalDao : ExtendedLocalDao<SectionEntity>() {
    @Query(
        """
            SELECT * FROM section
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<SectionEntity?>

    @Query(
        """
            SELECT * FROM section
            WHERE lesson_id = :lessonId
            ORDER BY `index`
       """
    )
    abstract fun getSectionsByLessonId(lessonId: String): Flow<List<SectionEntity>>

    @Query(
        """
            SELECT id FROM section 
            WHERE lesson_id = :lessonId AND quiz_score >= :minScore
        """
    )
    abstract fun getSectionIdsByMinQuizScore(lessonId: String, minScore: Int): Flow<List<String>>
}