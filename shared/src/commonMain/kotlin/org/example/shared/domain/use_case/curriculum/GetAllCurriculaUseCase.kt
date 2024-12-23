package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.DataCollection
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for retrieving all curricula.
 *
 * @property repository The repository to retrieve curricula from.
 */
class GetAllCurriculaUseCase(private val repository: CurriculumRepository) {

    /**
     * Invokes the use case to retrieve all curricula from the specified path.
     *
     * @param path The path from where the curricula should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of curricula.
     * @throws IllegalArgumentException If the path does not end with [DataCollection.CURRICULA].
     */
    operator fun invoke(path: String) = flow {
        require(path.split("/").last() == DataCollection.CURRICULA.value) {
            "The path must end with ${DataCollection.CURRICULA.value}"
        }
        repository.getAll(path).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}