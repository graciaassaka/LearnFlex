package org.example.shared.domain.repository

import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Interface representing a repository for curriculums.
 */
interface CurriculumRepository :
    CrudOperations<Curriculum>,
    BatchOperations<Curriculum>