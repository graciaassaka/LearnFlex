package org.example.shared.domain.use_case.curriculum

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for deleting all curricula.
 *
 * @property repository The repository to interact with the data source.
 */
class DeleteAllCurriculaUseCase(private val repository: CurriculumRepository) {

    /**
     * Deletes all curricula for a given user.
     *
     * @param curricula The list of curricula to delete.
     * @param userId The ID of the user whose curricula are to be deleted.
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(curricula: List<Curriculum>, userId: String) = try {
        repository.deleteAll(
            items = curricula,
            path = PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}
