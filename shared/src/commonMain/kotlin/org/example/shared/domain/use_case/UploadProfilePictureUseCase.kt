package org.example.shared.domain.use_case

import org.example.shared.data.util.FileType
import org.example.shared.domain.service.AuthService
import org.example.shared.domain.service.StorageService

/**
 * Use case for uploading a profile picture.
 *
 * @property storageService The service used to handle file storage.
 * @property authService The service used to handle authentication and user data.
 */
class UploadProfilePictureUseCase(
    private val storageService: StorageService, private val authService: AuthService
) {
    /**
     * Invokes the use case to upload a profile picture.
     *
     * @param image The image data as a byte array.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend operator fun invoke(image: ByteArray) = runCatching {
        val user = authService.getUserData().getOrThrow()

        val url = storageService.uploadFile(
            fileData = image, path = "profile_pictures/${user.uid}.jpg", fileType = FileType.IMAGE
        ).getOrThrow()

        authService.updateUserData(user.copy(photoUrl = url)).getOrThrow()

        return@runCatching url
    }
}
