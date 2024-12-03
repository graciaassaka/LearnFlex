package org.example.shared.domain.use_case

import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving module IDs based on a minimum quiz score.
 *
 * @property repository The repository to interact with module data.
 */
class GetModuleIdsByMinQuizScoreUseCase(private val repository: ModuleRepository) {

    /**
     * Invokes the use case to retrieve module IDs based on a minimum quiz score.
     *
     * @param path The path where the modules are located.
     * @param minQuizScore The minimum quiz score to filter module IDs by.
     */
    operator fun invoke(path: String, minQuizScore: Int) = repository.getIdsByMinScore(path, minQuizScore)
}