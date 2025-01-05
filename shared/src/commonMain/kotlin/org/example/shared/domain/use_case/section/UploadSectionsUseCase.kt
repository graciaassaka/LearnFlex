package org.example.shared.domain.use_case.section

import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for uploading a batch of sections to the specified path.
 *
 * @property repository The repository responsible for handling section data operations.
 */
class UploadSectionsUseCase(private val repository: SectionRepository) {

    /**
     * Inserts a list of sections into the repository at the specified path.
     *
     * @param path The path where the sections should be inserted.
     * @param sections The list of sections to insert.
     */
    suspend operator fun invoke(path: String, sections: List<Section>) =
        repository.insertAll(path, sections, System.currentTimeMillis())
}