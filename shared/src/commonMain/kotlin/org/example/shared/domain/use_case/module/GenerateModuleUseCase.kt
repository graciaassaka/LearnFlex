package org.example.shared.domain.use_case.module

import kotlinx.coroutines.flow.first
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
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
    suspend operator fun invoke(
        tile: String,
        profile: Profile,
        curriculum: Curriculum
    ) = contentGeneratorClient.generateContent(
        context = ContentGeneratorClient.Context(
            field = profile.preferences.field,
            level = profile.preferences.level,
            goal = profile.preferences.goal,
            style = profile.learningStyle,
            type = ContentType.MODULE,
            contentDescriptors = listOf(
                ContentGeneratorClient.ContentDescriptor(
                    type = ContentType.CURRICULUM,
                    title = curriculum.title,
                    description = curriculum.description
                ),
                ContentGeneratorClient.ContentDescriptor(
                    type = ContentType.MODULE,
                    title = tile,
                    description = "Not provided"
                )
            )
        )
    ).first()
}