package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving the most recently updated active curriculum.
 *
 * @property getAllCurriculaUseCase The use case to retrieve all curricula.
 */
class GetActiveCurriculumUseCase(private val getAllCurriculaUseCase: GetAllCurriculaUseCase) {
    /**
     * Invokes the use case to retrieve the most recently updated curriculum from all available curricula
     * at the specified path.
     *
     * @param path The path from where the curricula should be retrieved.
     * @return A transformed [Flow] containing a [Result] with the most recently updated curriculum
     *         among the list of curricula.
     */
    operator fun invoke(path: String) = getAllCurriculaUseCase(path)
        .map { result ->
            result.map { curricula ->
                curricula.maxByOrNull { it.lastUpdated }
            }
        }
}
