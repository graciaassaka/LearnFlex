package org.example.shared.domain.use_case.section

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for uploading a section associated with a specific user, curriculum, module, and lesson.
 *
 * @property repository The repository used for inserting the section.
 */
class UploadSectionUseCase(
    private val repository: SectionRepository
) {
    /**
     * Inserts a given `Section` object into the repository at a specific path, which is dynamically built
     * using the provided user ID, curriculum ID, module ID, and lesson ID.
     *
     * @param section The `Section` object to be inserted into the repository.
     * @param userId The unique identifier for the user under whose profile the section is to be added.
     * @param curriculumId The unique identifier for the curriculum under which the section is to be added.
     * @param moduleId The unique identifier for the module under which the section is to be added.
     * @param lessonId The unique identifier for the lesson under which the section is to be added.
     */
    suspend operator fun invoke(
        section: Section,
        userId: String,
        curriculumId: String,
        moduleId: String,
        lessonId: String
    ) = try {
        repository.insert(
            item = section,
            path = PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .document(curriculumId)
                .collection(Collection.MODULES)
                .document(moduleId)
                .collection(Collection.LESSONS)
                .document(lessonId)
                .collection(Collection.SECTIONS)
                .document(section.id)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}