package org.example.shared.domain.use_case.section

import kotlinx.coroutines.flow.first
import org.example.shared.domain.model.*

/**
 * Use case that combines the functionality of generating a section and uploading it.
 *
 * @property generateSection An instance of [GenerateSectionUseCase] responsible for generating the section content.
 * @property uploadSectionUseCase An instance of [UploadSectionUseCase] responsible for uploading the generated section.
 */
class GenerateAndUploadSectionUseCase(
    private val generateSection: GenerateSectionUseCase,
    private val uploadSectionUseCase: UploadSectionUseCase
) {
    /**
     * Generates a section for the specified lesson and uploads it to the corresponding curriculum module.
     *
     * @param title The title of the section to be generated.
     * @param profile The user profile associated with the operation.
     * @param curriculum The curriculum to which the section belongs.
     * @param module The module within the curriculum to which the section belongs.
     * @param lesson The lesson within the module to which the section belongs.
     * @return A `Result` containing the generated and uploaded section if successful, or an exception*/
    suspend operator fun invoke(
        title: String,
        profile: Profile,
        curriculum: Curriculum,
        module: Module,
        lesson: Lesson
    ) = try {
        val section = generateSection(title, profile, curriculum, module, lesson).first()
            .getOrThrow().run { Section(title = title, description = description, content = content) }

        uploadSectionUseCase(section, profile.id, curriculum.id, module.id, lesson.id).getOrThrow()

        Result.success(section)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
