package org.example.shared.domain.use_case

import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for uploading a section.
 *
 * @property repository The repository to interact with section data.
 */
class UploadSectionUseCase(private val repository: SectionRepository) {

    /**
     * Invokes the use case to upload a section.
     *
     * @param path The path where the section will be uploaded.
     * @param section The section to be uploaded.
     */
    suspend operator fun invoke(path: String, section: Section) = repository.insert(path, section)
}