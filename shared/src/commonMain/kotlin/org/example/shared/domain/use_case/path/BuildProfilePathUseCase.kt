package org.example.shared.domain.use_case.path

import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for building a profile path. This class delegates the path building to a
 * provided [PathBuilder] implementation.
 *
 * @property path The PathBuilder instance used to build the profile path.
 */
class BuildProfilePathUseCase(private val path: PathBuilder) {
    /**
     * Executes the use case to build a profile path.
     *
     * @return A string representing the path to the user's profile directory.
     */
    operator fun invoke() = path.buildProfilePath()
}