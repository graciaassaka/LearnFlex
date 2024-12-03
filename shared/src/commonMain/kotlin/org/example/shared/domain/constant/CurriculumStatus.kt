package org.example.shared.domain.constant

import org.example.shared.domain.constant.definition.Status

/**
 * Enum class representing the status of a curriculum.
 *
 * @property value The string representation of the status.
 */
@Suppress("unused")
enum class CurriculumStatus(override val value: String) : Status {
    /** Status indicating the curriculum is in progress. */
    IN_PROGRESS("In Progress"),

    /** Status indicating the curriculum is unfinished. */
    UNFINISHED("Unfinished"),

    /** Status indicating the curriculum is finished. */
    FINISHED("Finished")
}