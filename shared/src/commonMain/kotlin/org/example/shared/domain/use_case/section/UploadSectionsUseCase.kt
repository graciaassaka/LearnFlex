package org.example.shared.domain.use_case.section

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for uploading a batch of sections to the specified path.
 *
 * @property repository The repository responsible for handling section data operations.
 */
class UploadSectionsUseCase(private val repository: SectionRepository) {

    suspend operator fun invoke(
        sections: List<Section>,
        userId: String,
        curriculumId: String,
        moduleId: String,
        lessonId: String
    ) = try {
        repository.insertAll(
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