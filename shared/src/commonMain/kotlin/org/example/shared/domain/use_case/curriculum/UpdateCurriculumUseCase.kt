package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for updating a curriculum.
 *
 * @property repository The repository to interact with curriculum data.
 */
class UpdateCurriculumUseCase(private val repository: CurriculumRepository) {

    /**
     * Invokes the use case to update a curriculum.
     *
     * @param curriculum The curriculum to be updated.
     * @param userId The ID of the user associated with the curriculum.
     * @return A Result indicating the success or failure of the update operation.
     */
    suspend operator fun invoke(curriculum: Curriculum, userId: String) = try {
        repository.update(
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