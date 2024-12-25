package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving lesson data.
 *
 * @property repository The repository to retrieve lesson data from.
 */
class GetLessonUseCase(private val repository: LessonRepository) {

    /**
     * Invokes the use case to get lesson data.
     *
     * @param path The path to the lesson data.
     * @param id The ID of the lesson data.
     * @return A [Flow] emitting a [Result] containing the lesson data.
     * @throws IllegalArgumentException If the path does not end with [Collection.LESSONS].
     */
    operator fun invoke(path: String, id: String) = flow {
        require(path.split("/").last() == Collection.LESSONS.value) {
            "The path must end with ${Collection.LESSONS.value}"
        }
        repository.get(path, id).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}