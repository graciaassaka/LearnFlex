package org.example.shared.domain.use_case.section

import kotlinx.coroutines.flow.first
import org.example.shared.domain.model.*

/**
 * Use case for regenerating and updating a section.
 *
 * Regenerates the content of a given section using the GenerateSectionUseCase
 * and updates the section in the repository using the UploadSectionUseCase.
 *
 * @property generateSection The use case responsible for generating the section content.
 * @property updateSection The use case responsible for uploading/updating the section in the repository.
 */
class RegenerateAndUpdateSectionUseCase(
    private val generateSection: GenerateSectionUseCase,
    private val updateSection: UploadSectionUseCase
) {
    /**
     * Regenerates and updates a section based on the provided profile, curriculum, module, lesson,
     * and section details. It generates a new section using the given inputs, updates the section
     * with the latest attributes, and handles any exceptions that may occur.
     *
     * @param profile The user's profile providing context for the operation.
     * @param curriculum The curriculum to which the section belongs.
     * @param module The module within the curriculum where the section resides.
     * @param lesson The lesson associated with the section.
     * @param section The section to be regenerated and updated.
     */
    suspend operator fun invoke(
        profile: Profile,
        curriculum: Curriculum,
        module: Module,
        lesson: Lesson,
        section: Section
    ) = try {
        val section = generateSection(section.title, profile, curriculum, module, lesson).first().getOrThrow()
            .run { section.copy(title = title, description = description, content = content) }

        updateSection(section, profile.id, curriculum.id, module.id, lesson.id).getOrThrow()

        Result.success(section)
    } catch (e: Exception) {
        Result.failure(e)
    }
}