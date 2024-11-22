package org.example.shared.domain.use_case

import org.example.shared.domain.constant.FileType
import org.example.shared.domain.service.AuthClient
import org.example.shared.domain.service.StorageClient

/**
 * Use case for uploading a profile picture.
 *
 * @property storageClient The service used to handle file storage.
 * @property authClient The service used to handle authentication and user data.
 */
class UploadProfilePictureUseCase(
    private val storageClient: StorageClient, private val authClient: AuthClient
) {
    /**
     * Invokes the use case to upload a profile picture.
     *
     * @param image The image data as a byte array.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend operator fun invoke(image: ByteArray) = runCatching {
        val user = authClient.getUserData().getOrThrow()

        val url = storageClient.uploadFile(
            fileData = image, path = "profile_pictures/${user.localId}.jpg", fileType = FileType.IMAGE
        ).getOrThrow()

        authClient.updateUserData(user.copy(photoUrl = url)).getOrThrow()

        return@runCatching url
    }
}
