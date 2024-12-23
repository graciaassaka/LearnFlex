package org.example.shared.domain.use_case.section

import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for retrieving sections based on a specified curriculum ID.
 *
 * @property repository The repository used to access section data.
 */
class GetSectionsByCurriculumIdUseCase(private val repository: SectionRepository) {
    /**
     * Invokes the use case to retrieve a list of sections associated with the specified curriculum ID.
     *
     * @param curriculumId The ID of the curriculum to filter the sections.
     * @return A [Result] containing a list of sections associated with the given curriculum ID
     *         or an error if the operation fails.
     */
    suspend operator fun invoke(curriculumId: String) = repository.getByCurriculumId(curriculumId)
}