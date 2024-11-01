package org.example.shared.data.firebase

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
import org.example.shared.FirebaseInit
import org.example.shared.data.util.FileType
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
        private val firebaseInit = FirebaseInit().apply {
            idToken = "test-id-token"
        }
        private const val WIREMOCK_PORT = 9099
        private const val WIREMOCK_BASE_URL = "http://localhost:$WIREMOCK_PORT/o"
    }

    @Before
    fun setUp() {
        // Initialize WireMock server
        wireMockServer = WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT))
        wireMockServer.start()
        configureFor("localhost", WIREMOCK_PORT)

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
                level = LogLevel.NONE
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60000L
                connectTimeoutMillis = 60000L
                socketTimeoutMillis = 60000L
            }
        }

        // Initialize FirebaseStorageService with the WireMock baseUrl
        firebaseStorageService = FirebaseStorageService(
            client = httpClient,
            firebaseInit = firebaseInit,
            baseUrl = WIREMOCK_BASE_URL
        )
    }

    @After
    fun tearDown() {
        wireMockServer.stop()
        httpClient.close()
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
        val expectedPath = "$path.jpg"
        val encodedPath = encodeStoragePath(expectedPath)
        val uploadUrl = "/o/$encodedPath"

        wireMockServer.stubFor(
            post(urlEqualTo(uploadUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                )
        )

        // When
        val result = firebaseStorageService.uploadFile(fileData, path, fileType)

        // Then
        assertTrue("Expected successful upload", result.isSuccess)
        assertEquals(expectedPath, result.getOrNull())

        wireMockServer.verify(
            postRequestedFor(urlEqualTo(uploadUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .withHeader("Content-Type", containing("multipart/form-data"))
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
        val expectedPath = "$path.pdf"
        val encodedPath = encodeStoragePath(expectedPath)
        val uploadUrl = "/o/$encodedPath"
        val exceptionMessage = "Upload failed"

        wireMockServer.stubFor(
            post(urlEqualTo(uploadUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withBody("{ \"error\": \"$exceptionMessage\" }")
                )
        )

        // When
        val result = firebaseStorageService.uploadFile(fileData, path, fileType)

        // Then
        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.UploadFailure)

        wireMockServer.verify(
            postRequestedFor(urlEqualTo(uploadUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }

    /**
     * Tests the successful deletion of a file.
     */
    @Test
    fun `deleteFile should delete file and return success`() = runTest {
        // Given
        val path = "uploads/fileToDelete"
        val encodedPath = encodeStoragePath(path)
        val deleteUrl = "/o/$encodedPath"

        wireMockServer.stubFor(
            delete(urlEqualTo(deleteUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                )
        )

        // When
        val result = firebaseStorageService.deleteFile(path)

        // Then
        assertTrue(result.isSuccess)

        wireMockServer.verify(
            deleteRequestedFor(urlEqualTo(deleteUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        )
    }

    /**
     * Tests the failure scenario during file deletion.
     */
    @Test
    fun `deleteFile should fail when server returns error`() = runTest {
        // Given
        val path = "uploads/fileToDeleteFailure"
        val encodedPath = encodeStoragePath(path)
        val deleteUrl = "/o/$encodedPath"
        val exceptionMessage = "Deletion failed"

        wireMockServer.stubFor(
            delete(urlEqualTo(deleteUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .willReturn(
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
            deleteRequestedFor(urlEqualTo(deleteUrl))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        )
    }

    /**
     * Tests the successful download of a file.
     */
    @Test
    fun `downloadFile should download file and return byte array on success`() = runTest {
        // Given
        val path = "uploads/fileToDownload"
        val encodedPath = encodeStoragePath(path)
        val fileContent = byteArrayOf(7, 8, 9)

        wireMockServer.stubFor(
            get(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .willReturn(
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
            getRequestedFor(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        )
    }

    /**
     * Tests the failure scenario during file download.
     */
    @Test
    fun `downloadFile should fail when server returns error`() = runTest {
        // Given
        val path = "uploads/fileToDownloadFailure"
        val encodedPath = encodeStoragePath(path)
        val exceptionMessage = "Download failed"

        wireMockServer.stubFor(
            get(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .willReturn(
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
            getRequestedFor(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        )
    }

    /**
     * Tests the successful retrieval of a file URL.
     */
    @Test
    fun `getFileUrl should retrieve and return file URL on success`() = runTest {
        // Given
        val path = "uploads/fileUrlTest"
        val encodedPath = encodeStoragePath(path)
        val expectedUrl = "$WIREMOCK_BASE_URL/$encodedPath?alt=media"

        wireMockServer.stubFor(
            get(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                )
        )

        // When
        val result = firebaseStorageService.getFileUrl(path)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())

        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        )
    }

    /**
     * Tests the failure scenario during retrieval of a file URL.
     */
    @Test
    fun `getFileUrl should fail when server returns error`() = runTest {
        // Given
        val path = "uploads/fileUrlFailure"
        val encodedPath = encodeStoragePath(path)
        val exceptionMessage = "Get URL failed"

        wireMockServer.stubFor(
            get(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withBody("{ \"error\": \"$exceptionMessage\" }")
                )
        )

        // When
        val result = firebaseStorageService.getFileUrl(path)

        // Then
        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.DownloadFailure)

        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/o/$encodedPath"))
                .withQueryParam("alt", equalTo("media"))
                .withHeader("Authorization", equalTo("Bearer ${firebaseInit.idToken}"))
        )
    }

    /**
     * Helper function to encode storage path.
     */
    private fun encodeStoragePath(path: String): String {
        return path.split("/").joinToString("/") { URLEncoder.encode(it, "UTF-8") }
    }
}
