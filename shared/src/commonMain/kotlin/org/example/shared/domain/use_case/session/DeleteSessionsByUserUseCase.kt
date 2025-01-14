package org.example.shared.domain.use_case.session

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

class DeleteSessionsByUserUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(sessions: List<Session>, userId: String) = try {
        repository.deleteAll(
            items = sessions,
            path = PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.SESSIONS)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}