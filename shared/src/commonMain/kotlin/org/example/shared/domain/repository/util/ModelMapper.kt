package org.example.shared.domain.repository.util

/**
 * Interface for mapping between model and entity objects.
 *
 * @param Model the type of the model object
 * @param Entity the type of the entity object
 */
interface ModelMapper<Model, Entity> {

    /**
     * Maps an entity object to a model object.
     *
     * @param entity the entity object to be mapped
     * @return the mapped model object
     */
    fun toModel(entity: Entity): Model

    /**
     * Maps a model object to an entity object.
     *
     * @param model the model object to be mapped
     * @param parentId the ID of the parent entity
     * @return the mapped entity object
     */
    fun toEntity(model: Model, parentId: String? = null): Entity
}