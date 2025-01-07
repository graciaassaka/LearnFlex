package org.example.shared.data.local.dao.util

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.storage_operations.util.Path

/**
 * Manages the updating of timestamps for various entities.
 *
 * @property entityTimestampUpdaters A map of DataCollection to TimestampUpdater.
 */
class TimestampManager(private val entityTimestampUpdaters: Map<Collection, TimestampUpdater>) {

    /**
     * Updates the timestamps for the given path.
     *
     * @param path The path whose segments are used to determine the entities to update.
     * @param timestamp The new timestamp to set.
     */
    suspend fun updateTimestamps(path: Path, timestamp: Long) {
        val segments = path.value.split("/")

        for (i in segments.size - 1 downTo 0 step 2)
            if (i - 1 >= 0)
                entityTimestampUpdaters[Collection.valueOf(segments[i - 1].uppercase())]
                    ?.updateTimestamp(segments[i], timestamp)
    }
}