package org.example.shared.domain.repository

import org.example.shared.domain.model.Lesson
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Interface representing a repository for lessons.
 */
interface LessonRepository :
    BatchOperations<Lesson>,
    CrudOperations<Lesson>