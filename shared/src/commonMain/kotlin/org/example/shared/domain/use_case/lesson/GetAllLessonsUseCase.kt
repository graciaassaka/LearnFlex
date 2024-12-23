package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.example.shared.domain.constant.DataCollection
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving all lessons.
 *
 * @property repository The repository to retrieve lesson data from.
 */
class GetAllLessonsUseCase(private val repository: LessonRepository) {

    /**
     * Invokes the use case to retrieve all lessons from the specified path.
     *
     * @param path The path from where the lessons should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of lessons.
     * @throws IllegalArgumentException If the path does not end with [DataCollection.LESSONS].
     */
    operator fun invoke(path: String) = flow {
        require(path.split("/").last() == DataCollection.LESSONS.value) {
            "The path must end with ${DataCollection.LESSONS.value}"
        }
        repository.getAll(path).collect(::emit)
    }.catch {
        emit(Result.failure(it))
    }
}