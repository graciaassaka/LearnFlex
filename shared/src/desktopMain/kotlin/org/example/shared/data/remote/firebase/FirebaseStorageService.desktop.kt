package org.example.shared.data.remote.firebase

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.shared.FirebaseInit
import org.example.shared.data.remote.util.StorageException
import org.example.shared.domain.constant.FileType
import org.example.shared.domain.service.StorageClient
import java.net.URLEncoder

/**
 * Service for handling Firebase Storage operations.
 *
 * @property client The HTTP client used for making requests.
 * @property firebaseInit The Firebase initialization object containing configuration and tokens.
 */
actual class FirebaseStorageClient(
    private val client: HttpClient,
    private val firebaseInit: FirebaseInit
) : StorageClient
{
    /**
     * Uploads a file to Firebase Storage.
     *
     * @param fileData The byte array of the file to be uploaded.
     * @param path The path where the file will be stored.
     * @param fileType The type of the file (e.g., IMAGE, DOCUMENT).
     * @return The result of the upload operation, containing the path if successful.
     */
    override suspend fun uploadFile(fileData: ByteArray, path: String, fileType: FileType) = runCatching {
        val mimeType = when (fileType)
        {
            FileType.IMAGE    -> "image/jpeg"
            FileType.DOCUMENT -> "application/pdf"
        }

        val boundary = "Firebase-Storage-Boundary-${System.currentTimeMillis()}"
        val multipartBody = createMultipartBody(path, mimeType, fileData, boundary)

        val response = client.post {
            setUpStorageRequest()
            url {
                path("upload", "storage", "v1", "b", FirebaseConfig.storageBucket, "o")
                parameters.append("uploadType", "multipart")
                parameters.append("name", path)
            }
            headers {
                if (!FirebaseConfig.useEmulator)
                    append(HttpHeaders.Authorization, "Bearer ${firebaseInit.idToken}")
                append(HttpHeaders.ContentType, "multipart/related; boundary=$boundary")
                append(HttpHeaders.ContentLength, multipartBody.size.toString())
            }
            setBody(multipartBody)
        }

        if (response.status.isSuccess().not()) throw StorageException.UploadFailure(response.bodyAsText())
    }.fold(
        onSuccess = { Result.success(path) },
        onFailure = { Result.failure(it) }
    )

    /**
     * Creates a multipart body for the file upload request.
     *
     * @param fullPath The full path of the file in Firebase Storage.
     * @param mimeType The MIME type of the file.
     * @param fileData The byte array of the file to be uploaded.
     * @param boundary The boundary string for the multipart request.
     * @return The byte array representing the multipart body.
     */
    private fun createMultipartBody(
        fullPath: String,
        mimeType: String,
        fileData: ByteArray,
        boundary: String
    ): ByteArray
    {
        val metadataJson = Json.encodeToString(StorageMetadata(mimeType, fullPath))
        return buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json\r\n\r\n")
            append(metadataJson)
            append("\r\n")
            append("--$boundary\r\n")
            append("Content-Type: $mimeType\r\n\r\n")
        }.toByteArray() + fileData + "\r\n--$boundary--\r\n".toByteArray()
    }

    /**
     * Deletes a file from Firebase Storage.
     *
     * @param path The path of the file to be deleted.
     * @return The result of the delete operation.
     */
    override suspend fun deleteFile(path: String) = runCatching {
        val response = client.delete {
            setUpStorageRequest()
            url {
                path("storage", "v1", "b", FirebaseConfig.storageBucket, "o", URLEncoder.encode(path, "UTF-8"))
            }
            headers {
                if (!FirebaseConfig.useEmulator) append(HttpHeaders.Authorization, "Bearer ${firebaseInit.idToken}")
            }
        }
        if (!response.status.isSuccess()) throw StorageException.DeleteFailure(response.bodyAsText())
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(it) }
    )

    /**
     * Downloads a file from Firebase Storage.
     *
     * @param path The path of the file to be downloaded.
     * @return The result of the download operation, containing the file data if successful.
     */
    override suspend fun downloadFile(path: String) = runCatching {
        val response = client.get {
            setUpStorageRequest()
            url {
                path("storage", "v1", "b", FirebaseConfig.storageBucket, "o", URLEncoder.encode(path, "UTF-8"))
                parameters.append("alt", "media")
            }
            headers {
                if (!FirebaseConfig.useEmulator) append(HttpHeaders.Authorization, "Bearer ${firebaseInit.idToken}")
            }
        }

        if (response.status.isSuccess()) response.readBytes()
        else throw StorageException.DownloadFailure(response.bodyAsText())
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it) }
    )

    /**
     * Retrieves the URL of a file in Firebase Storage.
     *
     * @param path The path of the file.
     * @return The result of the operation, containing the file URL if successful.
     */
    override suspend fun getFileUrl(path: String) = runCatching {
        if (path.isBlank()) throw IllegalArgumentException()

        val bucket = FirebaseConfig.storageBucket

        val (protocol, host, port) = with(FirebaseConfig) {
            if (useEmulator) Triple(URLProtocol.HTTP, emulatorHost, storageEmulatorPort)
            else Triple(URLProtocol.HTTPS, "storage.googleapis.com", 443)
        }

        URLBuilder(protocol, host, port).apply {
            path("storage", "v1", "b", bucket, "o", URLEncoder.encode(path, "UTF-8"))
            parameters.append("alt", "media")
        }.buildString()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it) }
    )


    /**
     * Sets up the HTTP request for Firebase Storage operations.
     */
    private fun HttpRequestBuilder.setUpStorageRequest()
    {
        url {
            if (FirebaseConfig.useEmulator)
            {
                protocol = URLProtocol.HTTP
                host = FirebaseConfig.emulatorHost
                port = FirebaseConfig.storageEmulatorPort
            } else
            {
                protocol = URLProtocol.HTTPS
                host = "storage.googleapis.com"
            }
        }
        timeout {
            requestTimeoutMillis = TIMEOUT
            connectTimeoutMillis = TIMEOUT
            socketTimeoutMillis = TIMEOUT
        }
    }

    companion object
    {
        private const val TIMEOUT = 60000L
    }
}
