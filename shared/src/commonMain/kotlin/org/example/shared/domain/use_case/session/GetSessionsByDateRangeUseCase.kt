package org.example.shared.domain.use_case.session

import org.example.shared.domain.repository.SessionRepository

/**
 * Use case for retrieving sessions within a specified date range.
 *
 * @property repository The repository from which session data is fetched.
 */
class GetSessionsByDateRangeUseCase(
    private val repository: SessionRepository
) {
    /**
     * Invokes the use case to query sessions within a specified date range.
     *
     * @param start The start of the date range, represented as a timestamp in milliseconds.
     * @param end The end of the date range, represented as a timestamp in milliseconds.
     * @return A [Flow] emitting a [Result] containing a list of session models.
     */
    suspend operator fun invoke(start: Long, end: Long) = repository.queryByDateRange(start, end)
}
