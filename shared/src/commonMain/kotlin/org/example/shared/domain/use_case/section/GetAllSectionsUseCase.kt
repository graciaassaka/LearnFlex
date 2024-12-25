package org.example.shared.domain.use_case.section

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for retrieving all sections.
 *
 * @property repository The repository to retrieve section data from.
 */
class GetAllSectionsUseCase(private val repository: SectionRepository) {

    /**
     * Invokes the use case to retrieve all sections from the specified path.
     *
     * @param path The path from where the sections should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of sections.
     * @throws IllegalArgumentException If the path does not end with [Collection.SECTIONS].
     */
    operator fun invoke(path: String) = flow {
        require(path.split("/").last() == Collection.SECTIONS.value) {
            "The path must end with ${Collection.SECTIONS.value}"
        }
        repository.getAll(path).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}