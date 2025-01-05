package org.example.shared.domain.use_case.activity

import org.example.shared.domain.use_case.session.GetSessionsByDateRangeUseCase
import java.time.Instant
import java.time.ZoneId

/**
 * Use case class for fetching the weekly activity.
 *
 * @property getSessionsByDateRangeUseCase The use case for fetching sessions by date range.
 */
class GetWeeklyActivityUseCase(
    private val getSessionsByDateRangeUseCase: GetSessionsByDateRangeUseCase
) {

    /**
     * Invokes the use case to fetch the weekly activity.
     *
     * @param timestamp The timestamp to fetch the weekly activity for.
     * @return The result of the weekly activity fetch operation.
     */
    suspend operator fun invoke(timestamp: Long) = runCatching {
        val start = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .minusDays(7)
            .toInstant()
            .toEpochMilli()

        getSessionsByDateRangeUseCase(start, timestamp).getOrThrow().let { sessions ->
            sessions
                .groupBy {
                    Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).dayOfWeek
                }.mapValues { (_, sessions) ->
                    Pair((sessions.sumOf { it.endTime - it.createdAt } / 60000), sessions.size)
                }
        }
    }
}
