package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.first
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving lesson data.
 *
 * @property repository The repository to retrieve lesson data from.
 */
class GetLessonUseCase(private val repository: LessonRepository) {

    /**
     * Retrieves a lesson by its ID.
     *
     * @param path The path in the repository where the lesson should be retrieved from.
     * @param id The ID of the lesson to be retrieved.
     * @return The lesson data.
     */
    suspend operator fun invoke(path: String, id: String) = runCatching {
        require(path.split("/").last() == Collection.LESSONS.value) {
            "The path must end with ${Collection.LESSONS.value}"
        }
        repository.get(path, id).first().getOrThrow()
    }
}