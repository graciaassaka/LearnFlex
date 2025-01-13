package org.example.shared.domain.repository

import org.example.shared.domain.model.Module
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Interface representing a repository for modules.
 */
interface ModuleRepository :
    CrudOperations<Module>,
    BatchOperations<Module>