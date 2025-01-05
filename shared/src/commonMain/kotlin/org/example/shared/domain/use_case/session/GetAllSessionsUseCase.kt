package org.example.shared.domain.use_case.session

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.SessionRepository

/**
 * Use case for retrieving all sessions.
 *
 * @property repository The repository to retrieve sessions from.
 */
class GetAllSessionsUseCase(private val repository: SessionRepository) {

    /**
     * Retrieves the first non-empty sessions list from the specified path.
     *
     * @param path The path where the sessions should be retrieved.
     * The path must end with the value of [Collection.SESSIONS].
     * @return A [Result] containing the first non-empty list of sessions or an empty list if none are found.
     * @throws IllegalArgumentException If the path does not end with [Collection.SESSIONS].
     */
    suspend operator fun invoke(path: String) = runCatching {
        require(path.split("/").last() == Collection.SESSIONS.value) {
            "The path must end with ${Collection.SESSIONS.value}"
        }
        withTimeoutOrNull(500L) { repository.getAll(path).filter { it.getOrThrow().isNotEmpty() == true }.first() }
            ?.getOrNull()
            ?: emptyList()
    }
}