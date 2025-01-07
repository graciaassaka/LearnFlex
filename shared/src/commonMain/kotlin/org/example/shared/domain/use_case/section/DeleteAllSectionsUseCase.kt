package org.example.shared.domain.use_case.section

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for deleting all specified sections from the given path.
 *
 * @property repository The repository to perform delete operations on sections.
 */
class DeleteAllSectionsUseCase(private val repository: SectionRepository) {
    suspend operator fun invoke(
        sections: List<Section>,
        userId: String,
        curriculumId: String,
        moduleId: String,
        lessonId: String
    ) = try {
        repository.deleteAll(
            items = sections,
            path = PathBuilder()
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
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}