package org.example.shared.domain.use_case.profile

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Profile
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.example.shared.domain.use_case.auth.DeleteUserUseCase

class DeleteProfileUseCase(
    private val repository: ProfileRepository,
    private val deleteUserUseCase: DeleteUserUseCase
) {
    suspend operator fun invoke(profile: Profile) = try {
        deleteUserUseCase().getOrThrow()
        repository.update(
            item = profile,
            path = PathBuilder().collection(Collection.PROFILES).document(profile.id).build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}

