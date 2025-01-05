package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving all lessons.
 *
 * @property repository The repository to retrieve lesson data from.
 */
class GetAllLessonsUseCase(private val repository: LessonRepository) {

    /**
     * Retrieves all lessons from the repository.
     *
     * @param path The path to the lessons collection.
     * @return A list of lessons.
     */
    suspend operator fun invoke(path: String) = runCatching {
        require(path.split("/").last() == Collection.LESSONS.value) {
            "The path must end with ${Collection.LESSONS.value}"
        }
        withTimeoutOrNull(500L) { repository.getAll(path).filter { it.getOrThrow().isNotEmpty() == true }.first() }
            ?.getOrNull()
            ?: emptyList()
    }
}