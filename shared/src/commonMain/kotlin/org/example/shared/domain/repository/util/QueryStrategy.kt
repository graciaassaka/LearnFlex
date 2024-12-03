package org.example.shared.domain.repository.util

import kotlinx.coroutines.flow.Flow

interface QueryStrategy<T> {
    fun execute(): Flow<T?>
}