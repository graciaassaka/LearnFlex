package org.example.shared.domain.repository

import org.example.shared.domain.model.Module
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations
import org.example.shared.domain.storage_operations.QueryByCurriculumIdOperation
import org.example.shared.domain.storage_operations.QueryByScoreOperation

/**
 * Interface representing a repository for modules.
 */
interface ModuleRepository :
    CrudOperations<Module>,
    BatchOperations<Module>,
    QueryByScoreOperation<Module>,
    QueryByCurriculumIdOperation<Module>