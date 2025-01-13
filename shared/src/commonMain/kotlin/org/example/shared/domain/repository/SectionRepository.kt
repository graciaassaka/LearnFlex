package org.example.shared.domain.repository

import org.example.shared.domain.model.Section
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Interface representing a repository for sections.
 */
interface SectionRepository :
    CrudOperations<Section>,
    BatchOperations<Section>