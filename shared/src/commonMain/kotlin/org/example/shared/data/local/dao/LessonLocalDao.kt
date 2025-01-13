package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
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
            WHERE module_id = :moduleId
        """
    )
    abstract fun getByModuleId(moduleId: String): Flow<List<LessonEntity>>
}