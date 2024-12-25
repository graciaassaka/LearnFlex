package org.example.shared.domain.use_case.session

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.SessionRepository

/**
 * Use case for retrieving all sessions.
 *
 * @property repository The repository to retrieve sessions from.
 */
class GetAllSessionsUseCase(private val repository: SessionRepository) {

    /**
     * Invokes the use case to get all sessions.
     *
     * @param path The path to retrieve sessions from.
     * @return A [Flow] emitting a [Result] containing the list of sessions.
     * @throws IllegalArgumentException If the path does not end with [Collection.SESSIONS].
     */
    operator fun invoke(path: String) = flow {
        require(path.split("/").last() == Collection.SESSIONS.value) {
            "The path must end with ${Collection.SESSIONS.value}"
        }
        repository.getAll(path).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}