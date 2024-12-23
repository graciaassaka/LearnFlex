package org.example.shared.domain.use_case.section

import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for deleting all specified sections from the given path.
 *
 * @property repository The repository to perform delete operations on sections.
 */
class DeleteAllSectionsUseCase(private val repository: SectionRepository) {

    /**
     * Invokes the use case to delete all specified sections at a given path.
     *
     * @param path The path where the sections are located.
     * @param sections The list of sections to delete.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend operator fun invoke(path: String, sections: List<Section>) =
        repository.deleteAll(path, sections, System.currentTimeMillis())
}