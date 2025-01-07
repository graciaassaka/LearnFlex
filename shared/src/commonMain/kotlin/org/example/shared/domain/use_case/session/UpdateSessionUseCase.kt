package org.example.shared.domain.use_case.session

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for updating a session.
 *
 * @property repository The repository that provides the update mechanism for sessions.
 */
class UpdateSessionUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(session: Session, userId: String) = try {
        repository.update(
            item = session,
            path = PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.SESSIONS)
                .document(session.id)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}