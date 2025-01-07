package org.example.shared.domain.use_case.curriculum

/**
 * Use case for retrieving the most recently updated active curriculum.
 *
 * @property fetchCurriculaByUserUseCase The use case to retrieve all curricula.
 */
class FetchActiveCurriculumUseCase(private val fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase) {
    /**
     * Retrieves the most recently updated active curriculum for a given user.
     *
     * @param userId The ID of the user whose active curriculum is to be retrieved.
     * @return A Result containing the most recently updated active curriculum, or an error if the operation fails.
     */
    suspend operator fun invoke(userId: String) = fetchCurriculaByUserUseCase(userId).map { curricula ->
        curricula.maxByOrNull { it.lastUpdated }
    }
}
