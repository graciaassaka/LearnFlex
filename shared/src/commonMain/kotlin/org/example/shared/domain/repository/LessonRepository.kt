package org.example.shared.domain.repository

import org.example.shared.domain.model.Lesson
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations
import org.example.shared.domain.storage_operations.QueryByScoreOperation

interface LessonRepository :
    BatchOperations<Lesson>,
    CrudOperations<Lesson>,
    QueryByScoreOperation<Lesson>