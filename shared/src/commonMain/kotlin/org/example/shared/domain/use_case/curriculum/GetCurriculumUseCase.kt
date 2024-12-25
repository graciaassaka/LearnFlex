package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for retrieving curriculum data.
 *
 * @property repository The repository to retrieve curriculum data from.
 */
class GetCurriculumUseCase(private val repository: CurriculumRepository) {

    /**
     * Invokes the use case to get curriculum data.
     *
     * @param path The path to the curriculum data.
     * @param id The ID of the curriculum data.
     * @return A [Flow] emitting a [Result] containing the curriculum data.
     * @throws IllegalArgumentException If the path does not end with [Collection.CURRICULA].
     */
    operator fun invoke(path: String, id: String) = flow {
        require(path.split("/").last() == Collection.CURRICULA.value) {
            "The path must end with ${Collection.CURRICULA.value}"
        }
        repository.get(path, id).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}