package org.example.shared.data.local.dao.util

import org.example.shared.domain.constant.DataCollection

/**
 * Manages the updating of timestamps for various entities.
 *
 * @property entityTimestampUpdaters A map of DataCollection to TimestampUpdater.
 */
class TimestampManager(private val entityTimestampUpdaters: Map<DataCollection, TimestampUpdater>) {

    /**
     * Updates the timestamps for the given path.
     *
     * @param path The path whose segments are used to determine the entities to update.
     * @param timestamp The new timestamp to set.
     */
    suspend fun updateTimestamps(path: String, timestamp: Long) {
        val segments = path.split("/")

        for (i in segments.size - 1 downTo 0 step 2)
            if (i - 1 >= 0)
                entityTimestampUpdaters[DataCollection.valueOf(segments[i - 1].uppercase())]
                    ?.updateTimestamp(segments[i], timestamp)
    }
}