package org.example.shared.domain.repository

import org.example.shared.domain.model.Session
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations
import org.example.shared.domain.storage_operations.QueryByDateRangeOperation

interface SessionRepository :
    CrudOperations<Session>,
    BatchOperations<Session>,
    QueryByDateRangeOperation<Session>