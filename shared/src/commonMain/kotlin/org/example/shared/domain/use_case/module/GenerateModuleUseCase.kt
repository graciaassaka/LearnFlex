package org.example.shared.domain.use_case.module

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Profile

/**
 * Use case for generating a module.
 *
 * @property contentGeneratorClient The client used to generate content.
 */
class GenerateModuleUseCase(
    private val contentGeneratorClient: ContentGeneratorClient
) {
    /**
     * Invokes the use case to generate a module.
     *
     * @param tile The title of the module.
     * @param profile The profile containing user preferences.
     * @param curriculum The curriculum associated with the module.
     * @return The generated content.
     */
    operator fun invoke(
        tile: String,
        profile: Profile,
        curriculum: Curriculum
    ) = flow {
        contentGeneratorClient.generateContent(
            context = ContentGeneratorClient.Context(
                field = Field.valueOf(profile.preferences.field),
                level = Level.valueOf(profile.preferences.level),
                style = profile.learningStyle,
                type = ContentType.MODULE,
                contentDescriptors = listOf(
                    ContentGeneratorClient.Context.ContentDescriptor(
                        type = ContentType.CURRICULUM,
                        title = curriculum.title,
                        description = curriculum.description
                    ),
                    ContentGeneratorClient.Context.ContentDescriptor(
                        type = ContentType.MODULE,
                        title = tile,
                        description = "No description provided"
                    )
                )
            )
        ).collect(::emit)
    }.catch { e ->
        emit(Result.failure(e))
    }
}