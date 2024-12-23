package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for updating a curriculum.
 *
 * @property repository The repository to interact with curriculum data.
 */
class UpdateCurriculumUseCase(private val repository: CurriculumRepository) {
    /**
     * Updates the curriculum at the specified path.
     *
     * @param path The path where the curriculum is located.
     * @param curriculum The curriculum data to update.
     */
    suspend operator fun invoke(path: String, curriculum: Curriculum) =
        repository.update(path, curriculum, System.currentTimeMillis())
}