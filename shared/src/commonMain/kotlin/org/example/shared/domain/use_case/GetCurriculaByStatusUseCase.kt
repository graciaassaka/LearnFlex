package org.example.shared.domain.use_case

import org.example.shared.domain.constant.CurriculumStatus
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for retrieving curricula by their status.
 *
 * @property repository The repository to access curriculum data.
 */
class GetCurriculaByStatusUseCase(private val repository: CurriculumRepository) {
    /**
     * Retrieves curricula based on the given status.
     *
     * @param status The status of the curricula to retrieve.
     * @return A list of curricula with the specified status.
     */
    operator fun invoke(status: CurriculumStatus) = repository.getByStatus(status)
}