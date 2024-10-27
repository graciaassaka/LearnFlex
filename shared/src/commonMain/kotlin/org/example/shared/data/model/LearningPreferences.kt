package org.example.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LearningPreferences(
    val field: LearningField,
    val level: LearningLevel,
    val goal: String
)

@Suppress("unused")
@Serializable
enum class LearningField {
    Arts,
    Business,
    ComputerScience,
    Engineering,
    Health,
    Humanities,
    Languages,
    Law,
    Math,
    Science,
    SocialScience
}

@Suppress("unused")
@Serializable
enum class LearningLevel
{
    Beginner,
    Intermediate,
    Advanced
}