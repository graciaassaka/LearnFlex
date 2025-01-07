package org.example.shared.domain.use_case.session

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for uploading a session.
 *
 * @property repository The repository used to store session data.
 */
class UploadSessionUseCase(private val repository: SessionRepository) {
    /**
     * Uploads a session to the repository.
     *
     * @param session The session to be uploaded.
     * @param userId The ID of the user.
     * @return A Result indicating the success or failure of the upload operation.
     */
    suspend operator fun invoke(session: Session, userId: String) = try {
        repository.insert(
            item = session,
            path = PathBuilder()
                .collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.SESSIONS)
                .document(session.id)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}