package org.example.shared.data.remote.firebase

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.FileType
import org.example.shared.data.util.StorageException
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
actual class FirebaseStorageServiceTest {

    private lateinit var firebaseStorageService: FirebaseStorageService
    private lateinit var mockStorage: FirebaseStorage
    private lateinit var mockStorageReference: StorageReference
    private lateinit var mockChildReference: StorageReference
    private lateinit var mockUploadTask: UploadTask
    private lateinit var mockDownloadTask: Task<ByteArray>
    private lateinit var mockGetUrlTask: Task<Uri>

    @Before
    fun setUp() {
        // Initialize MockK objects
        mockStorage = mockk(relaxed = true)
        mockStorageReference = mockk(relaxed = true)
        mockChildReference = mockk(relaxed = true)
        mockUploadTask = mockk(relaxed = true)
        mockDownloadTask = mockk(relaxed = true)
        mockGetUrlTask = mockk(relaxed = true)

        // Configure FirebaseStorage to return a mock StorageReference
        every { mockStorage.reference } returns mockStorageReference
        // Configure StorageReference.child() to return another mock StorageReference
        every { mockStorageReference.child(any()) } returns mockChildReference

        // Initialize the service with mocked FirebaseStorage
        firebaseStorageService = FirebaseStorageService(mockStorage)

        // Mock the await() extension function
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    // Helper function to create a successful Task with a result
    private fun <T> createSuccessfulTask(result: T): Task<T> = Tasks.forResult(result)

    // Helper function to create a failed Task with an exception
    private fun <T> createFailedTask(exception: Exception): Task<T> = Tasks.forException(exception)

    // Helper function to create a successful Task<Void>
    private fun createSuccessfulVoidTask(): Task<Void> = Tasks.forResult(null)

    // Helper function to create a failed Task<Void>
    private fun createFailedVoidTask(exception: Exception): Task<Void> = Tasks.forException(exception)

    // Helper function to create a successful UploadTask
    private fun createSuccessfulUploadTask(): UploadTask {
        val uploadTask = mockk<UploadTask>(relaxed = true)
        // Mock the await() extension function to complete successfully
        val mockTaskSnapshot = mockk<UploadTask.TaskSnapshot>(relaxed = true)
        coEvery { uploadTask.await() } returns mockTaskSnapshot
        return uploadTask
    }

    // Helper function to create a failed UploadTask
    private fun createFailedUploadTask(exception: Exception): UploadTask {
        val uploadTask = mockk<UploadTask>(relaxed = true)
        // Mock the await() extension function to throw an exception
        coEvery { uploadTask.await() } throws exception
        return uploadTask
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

        // Create a successful UploadTask
        val successfulUploadTask = createSuccessfulUploadTask()

        // Mock StorageReference.child() to return mockChildReference when called with expectedPath
        every { mockStorageReference.child(any()) } returns mockChildReference

        // Mock putBytes() to return the successful UploadTask
        every { mockChildReference.putBytes(any()) } returns successfulUploadTask

        // When
        val result = firebaseStorageService.uploadFile(fileData, path, fileType)

        // Then
        verify(exactly = 1) {
            mockStorageReference.child(any())
            mockChildReference.putBytes(any())
        }

        coVerify(exactly = 1) { successfulUploadTask.await() }

        assertTrue(result.isSuccess)
        assertEquals(path, result.getOrNull())
    }

    /**
     * Tests the successful upload of a document file.
     */
    @Test
    fun `uploadFile should upload document and return correct path on success`() = runTest {
        // Given
        val fileData = byteArrayOf(4, 5, 6)
        val path = "uploads/document1"
        val fileType = FileType.DOCUMENT

        // Create a successful UploadTask
        val successfulUploadTask = createSuccessfulUploadTask()

        // Mock StorageReference.child() to return mockChildReference when called with expectedPath
        every { mockStorageReference.child(any()) } returns mockChildReference

        // Mock putBytes() to return the successful UploadTask
        every { mockChildReference.putBytes(any()) } returns successfulUploadTask

        // When
        val result = firebaseStorageService.uploadFile(fileData, path, fileType)

        // Then
        verify(exactly = 1) {
            mockStorageReference.child(any())
            mockChildReference.putBytes(any())
        }

        coVerify(exactly = 1) { successfulUploadTask.await() }

        assertTrue(result.isSuccess)
        assertEquals(path, result.getOrNull())
    }

    /**
     * Tests the failure scenario during file upload.
     */
    @Test
    fun `uploadFile should return failure when putBytes throws exception`() = runTest {
        // Given
        val fileData = byteArrayOf(7, 8, 9)
        val path = "uploads/failureTest"
        val fileType = FileType.IMAGE
        val exception = Exception("Upload failed")

        // Create a failed UploadTask
        val failedUploadTask = createFailedUploadTask(exception)

        // Mock StorageReference.child() to return mockChildReference when called with expectedPath
        every { mockStorageReference.child(path) } returns mockChildReference

        // Mock putBytes() to return the failed UploadTask
        every { mockChildReference.putBytes(fileData) } returns failedUploadTask

        // When
        val result = firebaseStorageService.uploadFile(fileData, path, fileType)

        // Then
        verify(exactly = 1) { mockStorageReference.child(path) }
        verify(exactly = 1) { mockChildReference.putBytes(fileData) }
        coVerify(exactly = 1) { failedUploadTask.await() }

        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.UploadFailure)
        assertEquals("Failed to upload file", failure.message)
        assertEquals(exception, failure.cause)
    }

    /**
     * Tests the successful deletion of a file.
     */
    @Test
    fun `deleteFile should delete file and return success`() = runTest {
        // Given
        val path = "uploads/fileToDelete"

        // Mock StorageReference.child() to return mockChildReference when called with path
        every { mockStorageReference.child(path) } returns mockChildReference

        // Mock delete() to return a successful Task<Void>
        every { mockChildReference.delete() } returns createSuccessfulVoidTask()

        // When
        val result = firebaseStorageService.deleteFile(path)

        // Then
        verify(exactly = 1) { mockStorageReference.child(path) }
        verify(exactly = 1) { mockChildReference.delete() }

        assertTrue(result.isSuccess)
    }

    /**
     * Tests the failure scenario during file deletion.
     */
    @Test
    fun `deleteFile should return failure when delete throws exception`() = runTest {
        // Given
        val path = "uploads/fileToDeleteFailure"
        val exception = Exception("Deletion failed")

        // Mock StorageReference.child() to return mockChildReference when called with path
        every { mockStorageReference.child(path) } returns mockChildReference

        // Mock delete() to return a failed Task<Void>
        every { mockChildReference.delete() } returns createFailedVoidTask(exception)

        // When
        val result = firebaseStorageService.deleteFile(path)

        // Then
        verify(exactly = 1) { mockStorageReference.child(path) }
        verify(exactly = 1) { mockChildReference.delete() }

        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.DeleteFailure)
        assertEquals("Failed to delete file", failure.message)
        assertEquals(exception, failure.cause)
    }

    /**
     * Tests the successful download of a file.
     */
    @Test
    fun `downloadFile should download file and return byte array on success`() = runTest {
        // Given
        val path = "uploads/fileToDownload"
        val expectedData = byteArrayOf(10, 11, 12)

        // Mock StorageReference.child() to return mockChildReference when called with path
        every { mockStorageReference.child(path) } returns mockChildReference

        // Mock getBytes() to return a successful Task<ByteArray>
        every { mockChildReference.getBytes(FirebaseStorageService.MAX_DOWNLOAD_SIZE) } returns createSuccessfulTask(
            expectedData
        )

        // When
        val result = firebaseStorageService.downloadFile(path)

        // Then
        verify(exactly = 1) { mockStorageReference.child(path) }
        verify(exactly = 1) { mockChildReference.getBytes(FirebaseStorageService.MAX_DOWNLOAD_SIZE) }

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
    }

    /**
     * Tests the failure scenario during file download.
     */
    @Test
    fun `downloadFile should return failure when getBytes throws exception`() = runTest {
        // Given
        val path = "uploads/fileToDownloadFailure"
        val exception = Exception("Download failed")

        // Mock StorageReference.child() to return mockChildReference when called with path
        every { mockStorageReference.child(path) } returns mockChildReference

        // Mock getBytes() to return a failed Task<ByteArray>
        every { mockChildReference.getBytes(FirebaseStorageService.MAX_DOWNLOAD_SIZE) } returns createFailedTask(
            exception
        )

        // When
        val result = firebaseStorageService.downloadFile(path)

        // Then
        verify(exactly = 1) { mockStorageReference.child(path) }
        verify(exactly = 1) { mockChildReference.getBytes(FirebaseStorageService.MAX_DOWNLOAD_SIZE) }

        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.DownloadFailure)
        assertEquals("Failed to download file", failure.message)
        assertEquals(exception, failure.cause)
    }

    /**
     * Tests the successful retrieval of a file URL.
     */
    @Test
    fun `getFileUrl should retrieve and return file URL on success`() = runTest {
        // Given
        val path = "uploads/fileUrlTest"
        val expectedUrl = "https://firebase.storage.com/fileUrlTest"

        // Mock StorageReference.child() to return mockChildReference when called with path
        every { mockStorageReference.child(path) } returns mockChildReference

        // Mock getDownloadUrl() to return a successful Task<Uri>
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns expectedUrl
        every { mockChildReference.downloadUrl } returns createSuccessfulTask(mockUri)

        // When
        val result = firebaseStorageService.getFileUrl(path)

        // Then
        verify(exactly = 1) { mockStorageReference.child(path) }
        verify(exactly = 1) { mockChildReference.downloadUrl }

        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
    }

    /**
     * Tests the failure scenario during retrieval of a file URL.
     */
    @Test
    fun `getFileUrl should return failure when downloadUrl throws exception`() = runTest {
        // Given
        val path = "uploads/fileUrlFailure"
        val exception = Exception("Get URL failed")

        // Mock StorageReference.child() to return mockChildReference when called with path
        every { mockStorageReference.child(path) } returns mockChildReference

        // Mock downloadUrl to return a failed Task<Uri>
        every { mockChildReference.downloadUrl } returns createFailedTask(exception)

        // When
        val result = firebaseStorageService.getFileUrl(path)

        // Then
        verify(exactly = 1) { mockStorageReference.child(path) }
        verify(exactly = 1) { mockChildReference.downloadUrl }

        assertTrue(result.isFailure)
        val failure = result.exceptionOrNull()
        assertTrue(failure is StorageException.DownloadFailure)
        assertEquals("Failed to get file URL", failure.message)
        assertEquals(exception, failure.cause)
    }
}
