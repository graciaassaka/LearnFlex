package org.example.shared.domain.use_case

import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository

/**
 * Use case for updating a session.
 *
 * @property repository The repository that provides the update mechanism for sessions.
 */
class UpdateSessionUseCase(private val repository: SessionRepository) {
    /**
     * Invokes the use case to update a session in the specified path.
     *
     * @param path The path where the session is located.
     * @param session The session object with updated information.
     */
    suspend operator fun invoke(path: String, session: Session) = repository.update(path, session)
}