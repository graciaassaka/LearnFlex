package org.example.shared.domain.storage_operations

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.definition.EndTimeQueryable

interface QueryByDateRangeOperation<Model : EndTimeQueryable> {
    fun queryByDateRange(start: Long, end: Long): Flow<Result<List<Model>>>
}