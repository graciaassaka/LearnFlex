package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for uploading a curriculum.
 *
 * @property repository The repository to interact with curriculum data.
 */
class UploadCurriculumUseCase(private val repository: CurriculumRepository) {
    /**
     * Invokes the use case to upload a curriculum.
     *
     * @param path The path where the curriculum will be uploaded.
     * @param curriculum The curriculum data to be uploaded.
     */
    suspend operator fun invoke(path: String, curriculum: Curriculum) =
        repository.insert(path, curriculum, System.currentTimeMillis())
}