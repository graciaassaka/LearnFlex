package org.example.shared.domain.constant

import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Enum class representing the status of content with different progression stages.
 *
 * @property value The status of the content.
 */
enum class Status(override val value: String) : ValuableEnum<String> {
    /**
     * Represents a content state that is not yet completed.
     */
    UNFINISHED("Unfinished"),

    /**
     * Represents a content status indicating that the content has been fully completed.
     */
    FINISHED("Finished")
}