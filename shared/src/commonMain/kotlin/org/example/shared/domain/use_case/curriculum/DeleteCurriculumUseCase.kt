package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for deleting a curriculum.
 *
 * @property repository The repository to interact with curriculum data.
 */
class DeleteCurriculumUseCase(private val repository: CurriculumRepository) {

    /**
     * Deletes a curriculum for a given user.
     *
     * @param curriculum The curriculum to delete.
     * @param userId The ID of the user whose curriculum is to be deleted.
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(curriculum: Curriculum, userId: String) = try {
        repository.delete(
            item = curriculum,
            path = PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .document(curriculum.id)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}
