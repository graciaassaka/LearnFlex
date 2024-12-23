package org.example.shared.domain.dao

import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Interface for Data Access Objects (DAOs) that perform CRUD operations on a database.
 *
 * @param Model The type of the model.
 */
interface Dao<Model : DatabaseRecord> : CrudOperations<Model>