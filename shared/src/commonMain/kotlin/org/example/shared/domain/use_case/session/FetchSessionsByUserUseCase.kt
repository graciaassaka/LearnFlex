package org.example.shared.domain.use_case.session

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for retrieving all sessions.
 *
 * @property repository The repository to retrieve sessions from.
 */
class FetchSessionsByUserUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(userId: String) = try {
        val path = PathBuilder().collection(Collection.PROFILES)
            .document(userId)
            .collection(Collection.SESSIONS)
            .build()
        withTimeoutOrNull(500L) {
            repository.getAll(path).filter { it.getOrThrow().isNotEmpty() == true }.first()
        } ?: Result.success(emptyList<Session>())
    } catch (e: Exception) {
        Result.failure(e)
    }
}