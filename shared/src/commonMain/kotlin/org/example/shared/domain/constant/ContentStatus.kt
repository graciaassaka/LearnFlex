package org.example.shared.domain.constant

/**
 * Enum class representing the status of content with different progression stages.
 *
 * This enum class provides a standardized way to represent the state of
 * progress for a given piece of content within the system.
 *
 */
enum class ContentStatus(val value: String) {
    /**
     * Represents a content state that is not yet completed.
     */
    UNFINISHED("Unfinished"),

    /**
     * Represents a content status indicating that the content has been fully completed.
     */
    FINISHED("Finished")
}