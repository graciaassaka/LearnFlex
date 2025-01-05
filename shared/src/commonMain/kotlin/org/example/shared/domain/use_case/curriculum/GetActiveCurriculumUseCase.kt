package org.example.shared.domain.use_case.curriculum

/**
 * Use case for retrieving the most recently updated active curriculum.
 *
 * @property getAllCurriculaUseCase The use case to retrieve all curricula.
 */
class GetActiveCurriculumUseCase(private val getAllCurriculaUseCase: GetAllCurriculaUseCase) {

    /**
     * Retrieves the most recently updated active curriculum.
     *
     * @param path The path to the curricula collection.
     * @return A [Result] containing the most recently updated active curriculum or an error.
     */
    suspend operator fun invoke(path: String) = getAllCurriculaUseCase(path).map { curricula ->
        curricula.maxByOrNull { it.lastUpdated }
    }
}
