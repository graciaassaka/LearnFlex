package org.example.shared.domain.dao

import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.storage_operations.BatchOperations

/**
 * Interface for Data Access Objects (DAOs) that perform CRUD operations on a database.
 *
 * @param Model The type of the database record that extends DatabaseRecord.
 */
interface ExtendedDao<Model : DatabaseRecord> :
    Dao<Model>,
    BatchOperations<Model>