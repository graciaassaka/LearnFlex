package org.example.shared.domain.use_case

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
     * @return The list of sessions.
     */
    operator fun invoke(path: String) = repository.getAll(path)
}