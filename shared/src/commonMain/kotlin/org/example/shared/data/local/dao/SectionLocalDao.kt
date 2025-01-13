package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
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
}