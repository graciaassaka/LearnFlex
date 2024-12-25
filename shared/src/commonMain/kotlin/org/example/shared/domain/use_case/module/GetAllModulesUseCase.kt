package org.example.shared.domain.use_case.module

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving all modules.
 *
 * @property repository The repository to retrieve module data from.
 */
class GetAllModulesUseCase(private val repository: ModuleRepository) {

    /**
     * Invokes the use case to retrieve all modules from the specified path.
     *
     * @param path The path from where the modules should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of modules.
     * @throws IllegalArgumentException If the path does not end with [Collection.MODULES].
     */
    operator fun invoke(path: String) = flow {
        require(path.split("/").last() == Collection.MODULES.value) {
            "The path must end with ${Collection.MODULES.value}"
        }
        repository.getAll(path).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}