package org.example.shared.domain.use_case.module

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving all modules.
 *
 * @property repository The repository to retrieve module data from.
 */
class GetAllModulesUseCase(private val repository: ModuleRepository) {

    /**
     * Retrieves all modules.
     *
     * @param path The path in the repository where the modules should be retrieved from.
     * @return The module data.
     */
    suspend operator fun invoke(path: String) = runCatching {
        require(path.split("/").last() == Collection.MODULES.value) {
            "The path must end with ${Collection.MODULES.value}"
        }
        withTimeoutOrNull(500L) { repository.getAll(path).filter { it.getOrThrow().isNotEmpty() == true }.first() }
            ?.getOrNull()
            ?: emptyList()
    }
}