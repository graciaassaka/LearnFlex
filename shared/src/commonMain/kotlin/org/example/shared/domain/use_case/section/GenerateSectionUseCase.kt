package org.example.shared.domain.use_case.section

import kotlinx.coroutines.flow.first
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile

/**
 * Use case for generating a section.
 *
 * @property contentGeneratorClient The client used to generate content.
 */
class GenerateSectionUseCase(
    private val contentGeneratorClient: ContentGeneratorClient
) {
    /**
     * Generates a section based on the provided parameters.
     *
     * @param title The title of the section.
     * @param profile The profile containing user preferences.
     * @param curriculum The curriculum to which the section belongs.
     * @param module The module to which the section belongs.
     * @param lesson The lesson to which the section belongs.
     * @return The generated content as a flow.
     */
    suspend operator fun invoke(
        title: String,
        profile: Profile,
        curriculum: Curriculum,
        module: Module,
        lesson: Lesson
    ) = contentGeneratorClient.generateContent(
        context = ContentGeneratorClient.Context(
            field = profile.preferences.field,
            level = profile.preferences.level,
            style = profile.learningStyle,
            type = ContentType.SECTION,
            contentDescriptors = listOf(
                ContentGeneratorClient.ContentDescriptor(
                    type = ContentType.CURRICULUM,
                    title = curriculum.title,
                    description = curriculum.description
                ),
                ContentGeneratorClient.ContentDescriptor(
                    type = ContentType.MODULE,
                    title = module.title,
                    description = module.description
                ),
                ContentGeneratorClient.ContentDescriptor(
                    type = ContentType.LESSON,
                    title = lesson.title,
                    description = lesson.description
                ),
                ContentGeneratorClient.ContentDescriptor(
                    type = ContentType.SECTION,
                    title = title,
                    description = "Not provided"
                )
            )
        )
    ).first()
}
