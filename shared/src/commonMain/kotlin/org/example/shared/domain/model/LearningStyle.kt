package org.example.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the learning style.
 *
 * @property dominant The dominant style identified.
 * @property breakdown The breakdown of styles.
 */
@Serializable
data class LearningStyle(
    val dominant: String = "",
    val breakdown: LearningStyleBreakdown = LearningStyleBreakdown()
)

/**
 * Represents the breakdown of different styles.
 *
 * @property visual The score for visual style.
 * @property reading The score for reading style.
 * @property kinesthetic The score for kinesthetic style.
 */
@Serializable
data class LearningStyleBreakdown(
    val visual: Int = 0,
    val reading: Int = 0,
    val kinesthetic: Int = 0
)