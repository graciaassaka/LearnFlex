package org.example.shared.data.sync.util

import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * Interface for helping with model operations.
 *
 * @param Model the type of the model which extends DatabaseRecord
 * @param Entity the type of the entity which extends DatabaseRecord
 */
interface ModelHelper<Model : DatabaseRecord, Entity : DatabaseRecord> {

    /**
     * Gets the ID of the model.
     *
     * @param model the model instance
     * @return the ID of the model as a String
     */
    fun getId(model: Model): String

    /**
     * Gets the last updated timestamp of the model.
     *
     * @param model the model instance
     * @return the last updated timestamp as a Long
     */
    fun getLastUpdated(model: Model): Long

    /**
     * Converts the model to an entity.
     *
     * @param model the model instance
     * @return the entity converted from the model
     */
    fun toEntity(model: Model): Entity
}