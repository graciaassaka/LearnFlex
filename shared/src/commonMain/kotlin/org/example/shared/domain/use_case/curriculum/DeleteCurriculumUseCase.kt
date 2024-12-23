package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for deleting a curriculum.
 *
 * @property repository The repository to interact with curriculum data.
 */
class DeleteCurriculumUseCase(private val repository: CurriculumRepository) {
    /**
     * Deletes a curriculum.
     *
     * @param path The path to the curriculum.
     * @param curriculum The curriculum to be deleted.
     */
    suspend operator fun invoke(path: String, curriculum: Curriculum) =
        repository.delete(path, curriculum, System.currentTimeMillis())
}
