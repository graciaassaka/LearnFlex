package org.example.shared.domain.constant

import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Represents the type of content.
 *
 * @property value The type of content.
 */
@Suppress("unused")
enum class ContentType(override val value: String) : ValuableEnum<String> {
    SYLLABUS("Syllabus"),
    CURRICULUM("Curriculum"),
    MODULE("Module"),
    LESSON("Lesson"),
    SECTION("Section"),
    QUIZ("Quiz")
}