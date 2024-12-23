package org.example.shared.domain.repository

import org.example.shared.domain.model.Profile
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Repository for user profiles.
 */
interface ProfileRepository :
    CrudOperations<Profile>