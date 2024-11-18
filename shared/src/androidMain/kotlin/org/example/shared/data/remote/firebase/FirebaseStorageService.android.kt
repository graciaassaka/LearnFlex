package org.example.shared.data.remote.firebase

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import org.example.shared.data.remote.util.StorageException
import org.example.shared.domain.constant.FileType
import org.example.shared.domain.service.StorageService

/**
 * Service class for handling Firebase Storage operations.
 *
 * @property storage The FirebaseStorage instance used for storage operations.
 */
actual class FirebaseStorageService(private val storage: FirebaseStorage) : StorageService {

    /**
     * Uploads a file to Firebase Storage.
     *
     * @param fileData The byte array of the file to be uploaded.
     * @param path The path where the file will be stored.
     * @param fileType The type of the file (IMAGE or DOCUMENT).
     * @return A Result containing the path of the uploaded file on success, or a StorageException on failure.
     */
    override suspend fun uploadFile(fileData: ByteArray, path: String, fileType: FileType) = runCatching {
        storage.reference.child(path).putBytes(fileData).await()
    }.fold(
        onSuccess = { Result.success(path) },
        onFailure = { Result.failure(StorageException.UploadFailure("Failed to upload file", it)) }
    )

    /**
     * Deletes a file from Firebase Storage.
     *
     * @param path The path of the file to be deleted.
     * @return A Result containing Unit on success, or a StorageException on failure.
     */
    override suspend fun deleteFile(path: String) = runCatching {
        storage.reference.child(path).delete().await()
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(StorageException.DeleteFailure("Failed to delete file", it)) }
    )

    /**
     * Downloads a file from Firebase Storage.
     *
     * @param path The path of the file to be downloaded.
     * @return A Result containing the byte array of the downloaded file on success, or a StorageException on failure.
     */
    override suspend fun downloadFile(path: String) = runCatching {
        storage.reference.child(path).getBytes(MAX_DOWNLOAD_SIZE).await()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(StorageException.DownloadFailure("Failed to download file", it)) }
    )

    /**
     * Retrieves the download URL of a file from Firebase Storage.
     *
     * @param path The path of the file.
     * @return A Result containing the download URL as a String on success, or a StorageException on failure.
     */
    override suspend fun getFileUrl(path: String) = runCatching {
        storage.reference.child(path).downloadUrl.await().toString()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(StorageException.DownloadFailure("Failed to get file URL", it)) }
    )

    companion object {
        const val MAX_DOWNLOAD_SIZE = 10L * 1024 * 1024
    }
}