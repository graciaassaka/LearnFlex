package org.example.shared.domain.model.util

import org.example.shared.domain.model.Session
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

/**
 * Manages a list of sessions and provides utility functions to analyze them.
 *
 * @property sessions The list of sessions to manage.
 */
class SessionManager(private val sessions: List<Session>) {

    /**
     * Calculates the weekly activity based on a given timestamp.
     *
     * @param timestamp The end timestamp in milliseconds to calculate the activity for the past week.
     * @return A map where the key is the day of the week and the value is a pair containing the total duration
     *         of sessions in minutes and the number of sessions for that day.
     */
    fun calculateWeeklyActivity(timestamp: Long): Map<DayOfWeek, Pair<Long, Int>> {
        val start = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .minusDays(7)
            .toInstant()
            .toEpochMilli()

        return getSessionsByDateRange(start, timestamp)
            .groupBy {
                Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).dayOfWeek
            }.mapValues { (_, sessions) ->
                Pair((sessions.sumOf { it.lastUpdated - it.createdAt } / 60000), sessions.size)
            }
    }

    /**
     * Filters the sessions that fall within the specified date range.
     *
     * @param startDate The start timestamp in milliseconds.
     * @param endDate The end timestamp in milliseconds.
     * @return A list of sessions that were created within the specified date range.
     */
    fun getSessionsByDateRange(startDate: Long, endDate: Long) =
        sessions.filter { it.createdAt in startDate..endDate }
}