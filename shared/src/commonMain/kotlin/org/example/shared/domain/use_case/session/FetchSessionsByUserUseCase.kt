package org.example.shared.domain.use_case.session

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.SessionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for retrieving all sessions.
 *
 * @property repository The repository to retrieve sessions from.
 */
class FetchSessionsByUserUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(userId: String) = try {
        repository.getAll(
            PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.SESSIONS)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}