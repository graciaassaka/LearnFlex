package org.example.shared.domain.use_case.session

import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository

/**
 * Use case for uploading a session.
 *
 * @property repository The repository used to store session data.
 */
class UploadSessionUseCase(private val repository: SessionRepository) {
    /**
     * Invokes the use case to insert a session into the repository.
     *
     * @param path The path in the repository where the session should be inserted.
     * @param session The session object to be inserted.
     */
    suspend operator fun invoke(path: String, session: Session) =
        repository.insert(path, session, System.currentTimeMillis())
}