package org.example.shared.data.firebase

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import org.example.shared.FirebaseInit
import org.example.shared.data.util.FileType
import org.example.shared.data.util.StorageException
import org.example.shared.domain.service.StorageService
import java.net.URLEncoder

/**
 * Service for interacting with Firebase Storage.
 *
 * @property client The HTTP client used for making requests.
 * @property firebaseInit The Firebase initialization object containing authentication tokens.
 * @property baseUrl The base URL for Firebase Storage API.
 */
actual class FirebaseStorageService(
    private val client: HttpClient,
    private val firebaseInit: FirebaseInit,
    private val baseUrl: String = "https://firebasestorage.googleapis.com/v0/b/${FirebaseConfig.getStorageBucket()}/o"
) : StorageService {

    /**
     * Uploads a file to Firebase Storage.
     *
     * @param fileData The byte array of the file to be uploaded.
     * @param path The path where the file will be stored.
     * @param fileType The type of the file (IMAGE or DOCUMENT).
     * @return The full path of the uploaded file.
     * @throws StorageException.UploadFailure if the upload fails.
     */
    @OptIn(InternalAPI::class)
    override suspend fun uploadFile(fileData: ByteArray, path: String, fileType: FileType) = runCatching {
        val (fullPath, contentType) = when (fileType) {
            FileType.IMAGE -> "$path.jpg" to ContentType.Image.JPEG
            FileType.DOCUMENT -> "$path.pdf" to ContentType.Application.Pdf
        }

        client.post("$baseUrl/${encodeStoragePath(fullPath)}") {
            header("Authorization", "Bearer ${firebaseInit.idToken}")
            timeout { requestTimeoutMillis = TIMEOUT }
            body = MultiPartFormDataContent(formData {
                append("file", fileData, Headers.build {
                    append(HttpHeaders.ContentType, contentType.toString())
                    append(HttpHeaders.ContentDisposition, "filename=\"$fullPath\"")
                })
            })
        }.run {
            if (status.isSuccess()) fullPath
            else throw StorageException.UploadFailure("Failed to upload file")
        }
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it) }
    )

    /**
     * Deletes a file from Firebase Storage.
     *
     * @param path The path of the file to be deleted.
     * @throws StorageException.DeleteFailure if the deletion fails.
     */
    override suspend fun deleteFile(path: String) = runCatching {
        client.delete("$baseUrl/${encodeStoragePath(path)}") {
            header("Authorization", "Bearer ${firebaseInit.idToken}")
        }.run {
            if (!status.isSuccess()) throw StorageException.DeleteFailure("Failed to delete file")
        }
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(it) }
    )

    /**
     * Downloads a file from Firebase Storage.
     *
     * @param path The path of the file to be downloaded.
     * @return The byte array of the downloaded file.
     * @throws StorageException.DownloadFailure if the download fails.
     */
    override suspend fun downloadFile(path: String) = runCatching {
        client.get("$baseUrl/${encodeStoragePath(path)}") {
            header("Authorization", "Bearer ${firebaseInit.idToken}")
            parameter("alt", "media")
        }.run {
            if (status.isSuccess()) readBytes()
            else throw StorageException.DownloadFailure("Failed to download file")
        }
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it) }
    )

    /**
     * Gets the URL of a file in Firebase Storage.
     *
     * @param path The path of the file.
     * @return The URL of the file.
     * @throws StorageException.DownloadFailure if the URL retrieval fails.
     */
    override suspend fun getFileUrl(path: String) = runCatching {
        client.get("$baseUrl/${encodeStoragePath(path)}") {
            header("Authorization", "Bearer ${firebaseInit.idToken}")
            parameter("alt", "media")
        }.run {
            if (status.isSuccess()) "$baseUrl/${encodeStoragePath(path)}?alt=media"
            else throw StorageException.DownloadFailure("Failed to get file URL")
        }
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it) }
    )

    /**
     * Encodes the storage path to be URL-safe.
     *
     * @param path The path to be encoded.
     * @return The encoded path.
     */
    private fun encodeStoragePath(path: String) = path.split("/").joinToString("/") { URLEncoder.encode(it, "UTF-8") }

    companion object {
        private const val TIMEOUT = 60000L
    }
}
