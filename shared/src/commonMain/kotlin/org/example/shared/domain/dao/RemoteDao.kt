package org.example.shared.domain.dao

import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Interface representing a remote data access object.
 *
 * @param Model The type of the model.
 */
interface RemoteDao<Model : DatabaseRecord> : CrudOperations<Model>