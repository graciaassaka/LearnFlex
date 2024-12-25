package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Interface for a client that generates content.
 *
 * @param Model The type of the database record.
 */
interface ContentGenerator {

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
     * Generates content based on the provided context.
     *
     * @param context The context for content generation.
     * @return A flow emitting the result of the content generation.
     */
    fun generateContent(context: String): Flow<Result<GeneratedResponse>>
}