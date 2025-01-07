package org.example.shared.domain.use_case.profile

import kotlinx.coroutines.delay
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.client.StorageClient
import org.example.shared.domain.constant.FileType
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.util.CompoundException

/**
 * Use case for uploading a profile picture.
 *
 * @property storageClient The service used to handle file storage.
 * @property authClient The service used to handle authentication and user data.
 */
class UploadProfilePictureUseCase(
    private val storageClient: StorageClient,
    private val authClient: AuthClient,
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) {
    /**
     * Uploads a profile picture.
     *
     * @param image The image data to be uploaded.
     * @return A Result indicating the success or failure of the upload operation.
     */
    suspend operator fun invoke(image: ByteArray) = runCatching {
        val user = authClient.getUserData().getOrThrow()
        val profile = fetchProfileUseCase().getOrNull()
        var fileUploaded = false
        var authUpdated = false

        repeat(RETRY_TIMES) { time ->
            try {
                val newImageUrl = storageClient
                    .uploadFile(image, "profile_pictures/${user.localId}.jpg", FileType.IMAGE)
                    .getOrThrow()
                    .also { fileUploaded = true }

                authClient.updatePhotoUrl(newImageUrl).getOrThrow().also { authUpdated = true }

                profile?.let { updateProfileUseCase(it.copy(photoUrl = newImageUrl)).getOrThrow() }

                return@runCatching newImageUrl
            } catch (e: Exception) {
                rollbackProfilePictureUpload(profile, fileUploaded, authUpdated, e)
                if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1)) else throw e
            }
        }

        return@runCatching ""
    }

    /**
     * Rollback the profile picture upload in case of failure.
     *
     * @param profile The profile to be updated.
     * @param fileUploaded Indicates if the file was uploaded.
     * @param authUpdated Indicates if the authentication data was updated.
     * @param e The exception that caused the rollback.
     */
    private suspend fun rollbackProfilePictureUpload(
        profile: Profile?, fileUploaded: Boolean, authUpdated: Boolean, e: Exception
    ) = repeat(RETRY_TIMES) { time ->
        try {
            if (authUpdated && profile != null) authClient.updatePhotoUrl(profile.photoUrl).getOrThrow()
            if (fileUploaded && profile != null) storageClient.deleteFile("profile_pictures/${profile.id}.jpg").getOrThrow()
            return
        } catch (rollbackError: Exception) {
            if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1))
            else throw CompoundException(ROLLBACK_ERROR_MESSAGE, e, rollbackError)
        }
    }

    companion object {
        const val RETRY_TIMES = 3
        const val RETRY_DELAY = 1000L
        const val ROLLBACK_ERROR_MESSAGE = "Failed to upload profile picture and rollback failed"
    }
}