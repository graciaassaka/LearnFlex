package org.example.shared.domain.service

import org.example.shared.data.util.FileType

/**
 * Interface for storage service operations.
 */
interface StorageService
{
    /**
     * Uploads a file to the specified path.
     *
     * @param fileData The data of the file to upload.
     * @param path The path where the file will be stored.
     * @param fileType The type of the file being uploaded.
     * @return A [Result] containing the URL of the uploaded file.
     */
    suspend fun uploadFile(fileData: ByteArray, path: String, fileType: FileType): Result<String>

    /**
     * Deletes a file from the specified path.
     *
     * @param path The path of the file to delete.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun deleteFile(path: String): Result<Unit>

    /**
     * Downloads a file from the specified path.
     *
     * @param path The path of the file to download.
     * @return A [Result] containing the data of the downloaded file.
     */
    suspend fun downloadFile(path: String): Result<ByteArray>

    /**
     * Retrieves the URL of a file at the specified path.
     *
     * @param path The path of the file.
     * @return A [Result] containing the URL of the file.
     */
    suspend fun getFileUrl(path: String): Result<String>
}