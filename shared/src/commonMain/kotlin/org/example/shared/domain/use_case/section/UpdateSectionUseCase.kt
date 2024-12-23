package org.example.shared.domain.use_case.section

import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for updating a section.
 *
 * @property repository The repository used to update the section.
 */
class UpdateSectionUseCase(private val repository: SectionRepository) {

    /**
     * Invokes the use case to update a section at the specified path.
     *
     * @param path The path where the section should be updated.
     * @param section The section object containing updated information.
     */
    suspend operator fun invoke(path: String, section: Section) =
        repository.update(path, section, System.currentTimeMillis())
}