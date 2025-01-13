package org.example.shared.domain.repository

import org.example.shared.domain.model.Session
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations


/**
 * Repository interface for managing session data.
 *
 * Combines multiple operations including CRUD operations, batch processing,
 * and querying sessions within a specific date range.
 */
interface SessionRepository :
    CrudOperations<Session>,
    BatchOperations<Session>