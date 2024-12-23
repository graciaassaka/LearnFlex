package org.example.shared.data.local.dao.util

import androidx.room.Dao
import androidx.room.Query

/**
 * Interface for updating the timestamp of an entity.
 */
sealed interface TimestampUpdater {
    suspend fun updateTimestamp(id: String, timestamp: Long)

    @Dao
    interface ProfileTimestampUpdater : TimestampUpdater {
        @Query(
            """
                UPDATE profiles
                SET last_updated = :timestamp
                WHERE id = :id
            """
        )
        override suspend fun updateTimestamp(id: String, timestamp: Long)
    }

    @Dao
    interface CurriculumTimestampUpdater : TimestampUpdater {
        @Query(
            """
                UPDATE curricula
                SET last_updated = :timestamp
                WHERE id = :id
            """
        )
        override suspend fun updateTimestamp(id: String, timestamp: Long)
    }

    @Dao
    interface ModuleTimestampUpdater : TimestampUpdater {
        @Query(
            """
                UPDATE modules
                SET last_updated = :timestamp
                WHERE id = :id
            """
        )
        override suspend fun updateTimestamp(id: String, timestamp: Long)
    }

    @Dao
    interface LessonTimestampUpdater : TimestampUpdater {
        @Query(
            """
                UPDATE lessons
                SET last_updated = :timestamp
                WHERE id = :id
            """
        )
        override suspend fun updateTimestamp(id: String, timestamp: Long)
    }

    @Dao
    interface SectionTimestampUpdater : TimestampUpdater {
        @Query(
            """
                UPDATE sections
                SET last_updated = :timestamp
                WHERE id = :id
            """
        )
        override suspend fun updateTimestamp(id: String, timestamp: Long)
    }

    @Dao
    interface SessionTimestampUpdater : TimestampUpdater {
        @Query(
            """
                UPDATE sessions
                SET last_updated = :timestamp
                WHERE id = :id
            """
        )
        override suspend fun updateTimestamp(id: String, timestamp: Long)
    }
}