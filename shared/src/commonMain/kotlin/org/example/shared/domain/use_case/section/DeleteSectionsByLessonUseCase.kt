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
class DeleteSectionsByLessonUseCase(private val repository: SectionRepository) {

    /**
     * Deletes all specified sections from the given path.
     *
     * @param sections The sections to be deleted.
     * @param userId The ID of the user who owns the sections.
     * @param curriculumId The ID of the curriculum that contains the sections.
     * @param moduleId The ID of the module that contains the sections.
     * @param lessonId The ID of the lesson that contains the sections.
     * @return A Result indicating the success or failure of the delete operation.
     */
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