package org.example.shared.domain.use_case.section

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for retrieving all sections.
 *
 * @property repository The repository to retrieve section data from.
 */
class FetchSectionsByLessonUseCase(private val repository: SectionRepository) {
    suspend operator fun invoke(
        userId: String,
        curriculumId: String,
        moduleId: String,
        lessonId: String
    ) = try {
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(userId)
            .collection(Collection.CURRICULA)
            .document(curriculumId)
            .collection(Collection.MODULES)
            .document(moduleId)
            .collection(Collection.LESSONS)
            .document(lessonId)
            .collection(Collection.SECTIONS)
            .build()
        withTimeoutOrNull(2500L) {
            repository.getAll(path).filter { it.getOrThrow().isNotEmpty() == true }.first()
        } ?: Result.success(emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }
}