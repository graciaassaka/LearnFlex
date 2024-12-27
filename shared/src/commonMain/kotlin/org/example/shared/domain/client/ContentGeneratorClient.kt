package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.Profile

/**
 * Interface for a client that generates content.
 *
 * @param Model The type of the database record.
 */
interface ContentGeneratorClient {

    /**
     * Represents the response from content generation.
     *
     * @property title The title of the generated content.
     * @property imagePrompt The image prompt for the generated content.
     * @property description The description of the generated content.
     * @property content The generated content.
     */
    @Serializable
    data class GeneratedResponse(
        val title: String,
        val imagePrompt: String,
        val description: String,
        val content: List<String>
    )

    /**
     * Represents the context for content generation.
     *
     * @property type The type of the context.
     * @property contentDescriptors A list of content descriptors.
     */
    @Serializable
    data class Context(
        val field: String,
        val level: String,
        val goal: String,
        val style: Profile.LearningStyle,
        val type: String,
        val contentDescriptors: List<ContentDescriptor>
    )

    /**
     * Represents a descriptor for content.
     *
     * @property title The title of the content.
     * @property description The description of the content.
     */
    @Serializable
    data class ContentDescriptor(
        val type: String,
        val title: String,
        val description: String
    )

    /**
     * Generates content based on the provided context.
     *
     * @param context The context for content generation.
     * @return A flow emitting the result of the content generation.
     */
    fun generateContent(context: Context): Flow<Result<GeneratedResponse>>
}