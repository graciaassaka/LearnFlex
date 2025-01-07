package org.example.shared.domain.use_case.section

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for updating a section.
 *
 * @property repository The repository used to update the section.
 */
class UpdateSectionUseCase(private val repository: SectionRepository) {
    suspend operator fun invoke(
        section: Section,
        userId: String,
        curriculumId: String,
        moduleId: String,
        lessonId: String
    ) = try {
        repository.update(
            item = section,
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
                .document(section.id)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}