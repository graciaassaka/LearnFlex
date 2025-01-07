package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Profile

/**
 * Interface for a client that generates content.
 */
interface ContentGeneratorClient {

    /**
     * Represents the response from content generation.
     *
     * @property title The title of the generated content.
     * @property description The description of the generated content.
     * @property content The generated content.
     */
    @Serializable
    data class GeneratedResponse(
        val title: String,
        val description: String,
        val content: List<String>
    )

    /**
     * Represents the context for content generation.
     *
     * @property field The field of the context.
     * @property level The level of the context.
     * @property style The learning style of the context.
     * @property type The type of the content.
     * @property contentDescriptors The content descriptors of the context.
     */
    @Serializable
    data class Context(
        val field: Field,
        val level: Level,
        val style: Profile.LearningStyle,
        val type: ContentType,
        val contentDescriptors: List<ContentDescriptor>
    ) {
        @Serializable
        data class ContentDescriptor(
            val type: ContentType,
            val title: String,
            val description: String
        )
    }

    /**
     * Generates content based on the provided context.
     *
     * @param context The context for content generation.
     * @return A flow emitting the result of the content generation.
     */
    fun generateContent(context: Context): Flow<Result<GeneratedResponse>>
}