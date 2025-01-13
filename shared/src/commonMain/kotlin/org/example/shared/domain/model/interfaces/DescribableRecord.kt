package org.example.shared.domain.model.interfaces

/**
 * Interface representing a describable entity.
 */
interface DescribableRecord {
    /**
     * The title of the entity.
     */
    val title: String

    /**
     * The description of the entity.
     */
    val description: String
}