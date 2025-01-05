package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.model.Profile

/**
 * Use case for generating a curriculum based on a syllabus description and a user profile.
 *
 * @constructor Creates an instance with the necessary content generation client.
 * @param contentGeneratorClient The client responsible for generating the curriculum content.
 */
class GenerateCurriculumUseCase(
    private val contentGeneratorClient: ContentGeneratorClient
) {
    /**
     * Invokes the use case to generate a curriculum based on the given syllabus description and user profile.
     *
     * @param syllabusDescription The description of the syllabus to base the curriculum on.
     * @param profile The user profile containing learning preferences and learning style.
     * @return A [Result] encapsulating the first generated curriculum content or the failure if an error occurs.
     */
    operator fun invoke(syllabusDescription: String, profile: Profile) =
        contentGeneratorClient.generateContent(
            ContentGeneratorClient.Context(
                field = profile.preferences.field,
                level = profile.preferences.level,
                style = profile.learningStyle,
                type = ContentType.CURRICULUM,
                contentDescriptors = listOf(
                    ContentGeneratorClient.ContentDescriptor(
                        type = ContentType.SYLLABUS,
                        title = "No title provided",
                        description = syllabusDescription
                    )
                )
            )
        )

}