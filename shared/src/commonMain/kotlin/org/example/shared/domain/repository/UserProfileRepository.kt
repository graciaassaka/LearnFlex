package org.example.shared.domain.repository

import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.storage_operations.CrudOperations

/**
 * Repository for user profiles.
 */
interface UserProfileRepository : CrudOperations<UserProfile>