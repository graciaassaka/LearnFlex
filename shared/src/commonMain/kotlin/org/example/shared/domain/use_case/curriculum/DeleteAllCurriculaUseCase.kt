package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for deleting all curricula.
 *
 * @property repository The repository to interact with the data source.
 */
class DeleteAllCurriculaUseCase(private val repository: CurriculumRepository) {

    /**
     * Deletes all curricula at the specified path.
     *
     * @param path The path where the curricula are stored.
     * @param curricula The list of curricula to delete.
     */
    suspend operator fun invoke(path: String, curricula: List<Curriculum>) =
        repository.deleteAll(path, curricula, System.currentTimeMillis())
}
