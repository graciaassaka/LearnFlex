package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a style question with a list of options and a scenario.
 *
 * @property styleOptions The list of style options.
 * @property scenario The scenario description.
 */
@Serializable
data class StyleQuestion(
    @SerialName("options") val styleOptions: List<StyleOption>,
    @SerialName("scenario") val scenario: String
)

/**
 * Represents a style option with a style and text.
 *
 * @property style The style identifier.
 * @property text The text description of the style.
 */
@Serializable
data class StyleOption(
    @SerialName("style") val style: String,
    @SerialName("text") val text: String
)

/**
 * Represents the result of a style questionnaire.
 *
 * @property dominantStyle The dominant style identified.
 * @property styleBreakdown The breakdown of styles.
 */
@Serializable
data class StyleResult(
    val dominantStyle: String,
    val styleBreakdown: StyleBreakdown
)

/**
 * Represents the breakdown of different styles.
 *
 * @property visual The score for visual style.
 * @property reading The score for reading style.
 * @property kinesthetic The score for kinesthetic style.
 */
@Serializable
data class StyleBreakdown(
    val visual: Int = 0,
    val reading: Int = 0,
    val kinesthetic: Int = 0
)
