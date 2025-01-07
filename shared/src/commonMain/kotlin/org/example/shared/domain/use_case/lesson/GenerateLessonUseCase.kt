package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
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
    operator fun invoke(
        title: String,
        profile: Profile,
        curriculum: Curriculum,
        module: Module
    ) = flow {
        contentGeneratorClient.generateContent(
            context = ContentGeneratorClient.Context(
                field = Field.valueOf(profile.preferences.field),
                level = Level.valueOf(profile.preferences.level),
                style = profile.learningStyle,
                type = ContentType.LESSON,
                contentDescriptors = listOf(
                    ContentGeneratorClient.Context.ContentDescriptor(
                        type = ContentType.CURRICULUM,
                        title = curriculum.title,
                        description = curriculum.description
                    ),
                    ContentGeneratorClient.Context.ContentDescriptor(
                        type = ContentType.MODULE,
                        title = module.title,
                        description = module.description
                    ),
                    ContentGeneratorClient.Context.ContentDescriptor(
                        type = ContentType.LESSON,
                        title = title,
                        description = "Not provided"
                    )
                )
            )
        ).collect(::emit)
    }.catch { e ->
        emit(Result.failure(e))
    }
}