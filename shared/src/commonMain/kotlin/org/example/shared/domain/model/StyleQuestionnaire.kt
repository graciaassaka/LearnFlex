package org.example.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a style question with a list of options and a scenario.
 *
 * @property options The list of style options.
 * @property scenario The scenario description.
 */
@Serializable
data class StyleQuestion(
    val options: List<StyleOption>,
    val scenario: String
)

/**
 * Represents a style option with a style and text.
 *
 * @property style The style identifier.
 * @property text The text description of the style.
 */
@Serializable
data class StyleOption(
    val style: String,
    val text: String
)

