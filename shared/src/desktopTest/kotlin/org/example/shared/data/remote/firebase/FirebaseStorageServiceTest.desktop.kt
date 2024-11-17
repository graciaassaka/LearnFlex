package org.example.shared.data.remote.firebase

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.shared.data.remote.firebase.util.TestFirebaseUtil
import org.example.shared.domain.constant.FileType
import org.example.shared.data.util.StorageException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.URLEncoder
import kotlin.test.assertContentEquals

@ExperimentalCoroutinesApi
actual class FirebaseStorageServiceTest {

    private lateinit var firebaseStorageService: FirebaseStorageService
    private lateinit var httpClient: HttpClient
    private lateinit var wireMockServer: WireMockServer

    companion object {
        private val firebaseInit = TestFirebaseUtil.getFirebaseInit()
        private const val WIREMOCK_PORT = 9099
    }

    @Before
    fun setUp() {
        // Initialize WireMock server
        wireMockServer = WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT))
        wireMockServer.start()
        configureFor("localhost", WIREMOCK_PORT)

        // Configure FirebaseConfig to use the emulator and new ports
        FirebaseConfig.useEmulator = true
        FirebaseConfig.emulatorHost = "localhost"
        FirebaseConfig.storageEmulatorPort = WIREMOCK_PORT
        FirebaseConfig.authEmulatorPort = 9100

        // Initialize Ktor HttpClient
        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60000L
                connectTimeoutMillis = 60000L
                socketTimeoutMillis = 60000L
            }
        }

        // Initialize FirebaseStorageService
        firebaseStorageService = FirebaseStorageService(
            client = httpClient,
            firebaseInit = firebaseInit
        )
    }


    @After
    fun tearDown() {
        wireMockServer.stop()
        httpClient.close()
        TestFirebaseUtil.cleanup()
    }

    /**
     * Tests the successful upload of an image file.
     */
    @Test
    fun `uploadFile should upload image and return correct path on success`() = runTest {
        // Given
        val fileData = byteArrayOf(1, 2, 3)
        val path = "uploads/image1"
        val fileType = FileType.IMAGE
        val bucket = FirebaseConfig.storageBucket
        val uploadUrlPath = "/upload/storage/v1/b/$bucket/o"
        val useEmulator = FirebaseConfig.useEmulator

        // Set up request pattern
        val requestPattern = post(urlPathEqualTo(uploadUrlPath))
            .withQueryParam("uploadType", equalTo("multipart"))
            .withQueryParam("name", equalTo(path))
            .withHeader("Content-Type", containing("multipart/related"))

        if (!useEmulator) {
            requestPattern.withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        }

        wireMockServer.stubFor(
            requestPattern.willReturn(
                aResponse()
                    .withStatus(200)
            )
        )

        // When
        val result = firebaseStorageService.uploadFile(fileData, path, fileType)

        // Then
        assertTrue("Expected successful upload", result.isSuccess)
        assertEquals(path, result.getOrNull())

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo(uploadUrlPath))
                .withQueryParam("uploadType", equalTo("multipart"))
                .withQueryParam("name", equalTo(path))
                .withHeader("Content-Type", containing("multipart/related"))
        )
    }

    /**
     * Tests the failure scenario during file upload.
     */
    @Test
    fun `uploadFile should fail when server returns error`() = runTest {
        // Given
        val fileData = byteArrayOf(4, 5, 6)
        val path = "uploads/failureTest"
        val fileType = FileType.DOCUMENT
        val bucket = FirebaseConfig.storageBucket
        val uploadUrlPath = "/upload/storage/v1/b/$bucket/o"
        val useEmulator = FirebaseConfig.useEmulator
        val exceptionMessage = "Upload failed"

        val requestPattern = post(urlPathEqualTo(uploadUrlPath))
            .withQueryParam("uploadType", equalTo("multipart"))
            .withQueryParam("name", equalTo(path))
            .withHeader("Content-Type", containing("multipart/related"))

        if (!useEmulator) {
            requestPattern.withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        }

        wireMockServer.stubFor(
            requestPattern.willReturn(
                aResponse()
                    .withStatus(500)
                    .withBody("{ \"error\": \"$exceptionMessage\" }")
            )
        )

        // When
        val result = firebaseStorageService.uploadFile(fileData, "uploads/failureTest", fileType)

        // Then
        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.UploadFailure)

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo(uploadUrlPath))
                .withQueryParam("uploadType", equalTo("multipart"))
                .withQueryParam("name", equalTo(path))
                .withHeader("Content-Type", containing("multipart/related"))
        )
    }

    /**
     * Tests the successful deletion of a file.
     */
    @Test
    fun `deleteFile should delete file and return success`() = runTest {
        // Given
        val path = "uploads/fileToDelete"
        val bucket = FirebaseConfig.storageBucket
        val encodedPath = encodeStoragePath(path)
        val deleteUrlPath = "/storage/v1/b/$bucket/o/$encodedPath"
        val useEmulator = FirebaseConfig.useEmulator

        val requestPattern = delete(urlPathEqualTo(deleteUrlPath))

        if (!useEmulator) {
            requestPattern.withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        }

        wireMockServer.stubFor(
            requestPattern.willReturn(
                aResponse()
                    .withStatus(200)
            )
        )

        // When
        val result = firebaseStorageService.deleteFile(path)

        // Then
        assertTrue(result.isSuccess)

        wireMockServer.verify(
            deleteRequestedFor(urlPathEqualTo(deleteUrlPath))
        )
    }

    /**
     * Tests the failure scenario during file deletion.
     */
    @Test
    fun `deleteFile should fail when server returns error`() = runTest {
        // Given
        val path = "uploads/fileToDeleteFailure"
        val bucket = FirebaseConfig.storageBucket
        val encodedPath = encodeStoragePath(path)
        val deleteUrlPath = "/storage/v1/b/$bucket/o/$encodedPath"
        val useEmulator = FirebaseConfig.useEmulator
        val exceptionMessage = "Deletion failed"

        val requestPattern = delete(urlPathEqualTo(deleteUrlPath))

        if (!useEmulator) {
            requestPattern.withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        }

        wireMockServer.stubFor(
            requestPattern.willReturn(
                aResponse()
                    .withStatus(500)
                    .withBody("{ \"error\": \"$exceptionMessage\" }")
            )
        )

        // When
        val result = firebaseStorageService.deleteFile(path)

        // Then
        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.DeleteFailure)

        wireMockServer.verify(
            deleteRequestedFor(urlPathEqualTo(deleteUrlPath))
        )
    }

    /**
     * Tests the successful download of a file.
     */
    @Test
    fun `downloadFile should download file and return byte array on success`() = runTest {
        // Given
        val path = "uploads/fileToDownload"
        val bucket = FirebaseConfig.storageBucket
        val encodedPath = encodeStoragePath(path)
        val downloadUrlPath = "/storage/v1/b/$bucket/o/$encodedPath"
        val useEmulator = FirebaseConfig.useEmulator
        val fileContent = byteArrayOf(7, 8, 9)

        val requestPattern = get(urlPathEqualTo(downloadUrlPath))
            .withQueryParam("alt", equalTo("media"))

        if (!useEmulator) {
            requestPattern.withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        }

        wireMockServer.stubFor(
            requestPattern.willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(fileContent)
            )
        )

        // When
        val result = firebaseStorageService.downloadFile(path)

        // Then
        assertTrue(result.isSuccess)
        assertContentEquals(fileContent, result.getOrNull())

        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo(downloadUrlPath))
                .withQueryParam("alt", equalTo("media"))
        )
    }

    /**
     * Tests the failure scenario during file download.
     */
    @Test
    fun `downloadFile should fail when server returns error`() = runTest {
        // Given
        val path = "uploads/fileToDownloadFailure"
        val bucket = FirebaseConfig.storageBucket
        val encodedPath = encodeStoragePath(path)
        val downloadUrlPath = "/storage/v1/b/$bucket/o/$encodedPath"
        val useEmulator = FirebaseConfig.useEmulator
        val exceptionMessage = "Download failed"

        val requestPattern = get(urlPathEqualTo(downloadUrlPath))
            .withQueryParam("alt", equalTo("media"))

        if (!useEmulator) {
            requestPattern.withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        }

        wireMockServer.stubFor(
            requestPattern.willReturn(
                aResponse()
                    .withStatus(500)
                    .withBody("{ \"error\": \"$exceptionMessage\" }")
            )
        )

        // When
        val result = firebaseStorageService.downloadFile(path)

        // Then
        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.DownloadFailure)

        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo(downloadUrlPath))
                .withQueryParam("alt", equalTo("media"))
        )
    }

    /**
     * Tests the successful retrieval of a file URL.
     */
    @Test
    fun `getFileUrl should retrieve and return file URL on success`() = runTest {
        // Given
        val path = "uploads/fileUrlTest"
        val bucket = FirebaseConfig.storageBucket
        val encodedPath = encodeStoragePath(path)
        val expectedUrl =
            "http://${FirebaseConfig.emulatorHost}:${FirebaseConfig.storageEmulatorPort}/storage/v1/b/$bucket/o/$encodedPath?alt=media"

        // When
        val result = firebaseStorageService.getFileUrl(path)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
    }

    /**
     * Tests the failure scenario during retrieval of a file URL.
     */
    @Test
    fun `getFileUrl should fail when invalid path is provided`() = runTest {
        // Given
        val path = "" // Invalid path

        // When
        val result = firebaseStorageService.getFileUrl(path)

        // Then
        assertTrue(result.isFailure)
    }

    /**
     * Helper function to encode storage path.
     */
    private fun encodeStoragePath(path: String): String {
        return URLEncoder.encode(path, "UTF-8").replace("+", "%20")
    }
}
