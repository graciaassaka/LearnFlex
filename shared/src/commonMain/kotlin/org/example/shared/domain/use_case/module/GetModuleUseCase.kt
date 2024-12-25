package org.example.shared.domain.use_case.module

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving a module.
 *
 * @property repository The repository to retrieve the module from.
 */
class GetModuleUseCase(private val repository: ModuleRepository) {

    /**
     * Retrieves a module by its path and id.
     *
     * @param path The path of the module.
     * @param id The id of the module.
     * @return A [Flow] emitting a [Result] containing the module.
     * @throws IllegalArgumentException If the path does not end with [Collection.MODULES].
     */
    operator fun invoke(path: String, id: String) = flow {
        require(path.split("/").last() == Collection.MODULES.value) {
            "The path must end with ${Collection.MODULES.value}"
        }
        repository.get(path, id).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}