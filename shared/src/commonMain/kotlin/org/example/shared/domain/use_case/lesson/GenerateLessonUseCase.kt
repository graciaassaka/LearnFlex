package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.first
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile

/**
 * Use case responsible for generating a lesson based on the provided title, profile, curriculum, and module.
 *
 * @property contentGeneratorClient The client responsible for generating content.
 */
class GenerateLessonUseCase(
    private val contentGeneratorClient: ContentGeneratorClient
) {
    /**
     * Invokes the content generation process for a lesson based on the specified parameters.
     *
     * @param title The title of the lesson to generate.
     * @param profile The user profile including preferences and learning style.
     * @param curriculum The curriculum associated with the lesson.
     * @param module The module associated with the lesson.
     */
    suspend operator fun invoke(
        title: String,
        profile: Profile,
        curriculum: Curriculum,
        module: Module
    ) = contentGeneratorClient.generateContent(
        context = ContentGeneratorClient.Context(
            field = profile.preferences.field,
            level = profile.preferences.level,
            goal = profile.preferences.goal,
            style = profile.learningStyle,
            type = ContentType.LESSON,
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
                    title = title,
                    description = "Not provided"
                )
            )
        )
    ).first()
}