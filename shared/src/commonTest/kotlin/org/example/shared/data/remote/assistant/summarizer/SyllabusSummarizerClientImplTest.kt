package org.example.shared.data.remote.assistant.summarizer

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.SyllabusSummarizerClient
import org.example.shared.domain.model.assistant.*
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SyllabusSummarizerClientImplTest {

    private lateinit var assistant: AIAssistantClient
    private lateinit var client: SyllabusSummarizerClient

    @Before
    fun setup() {
        assistant = mockk()
        client = SyllabusSummarizerClientImpl(
            assistantClient = assistant,
            assistantId = "testAssistantId"
        )
    }

    /**
     * Tests a successful summarization flow (file upload + thread creation + run creation + run completes).
     */
    @Test
    fun `summarizeSyllabus success flow`() = runTest {
        // Given
        val testFile = File.createTempFile("dummy_path", ".txt")
        val uploadedFileId = "file_upload_123"
        val threadId = "thread_123"
        val runId = "run_123"

        // Mock file upload success
        coEvery { assistant.uploadFile(testFile, FilePurpose.ASSISTANTS) } returns Result.success(
            FileUploadResponse(
                id = uploadedFileId,
                bytes = 1024,
                createdAt = 123,
                filename = "dummy_path.txt",
                objectType = "file",
                purpose = FilePurpose.ASSISTANTS.value,
            )
        )

        // Mock thread creation success
        val thread = Thread(id = threadId, objectType = "thread", createdAt = 456)
        coEvery { assistant.createThread(any()) } returns Result.success(thread)

        // Mock run creation success
        val runCompleted = Run(
            id = runId,
            objectType = "run",
            createdAt = 789,
            threadId = threadId,
            assistantId = "asst_123",
            status = RunStatus.COMPLETED.value,
            tools = emptyList(),
            truncationStrategy = TruncationStrategy(),
            model = "gpt-4",
        )
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(runCompleted)

        // Mock deleteThread success
        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        // The `CompletionProcessor` will fetch the final assistant message.
        // We'll simulate that the final summary string is "Summary of the syllabus".
        val summaryText = "Summary of the syllabus"
        coEvery { assistant.listMessages(threadId, 10, MessagesOrder.DESC) } returns Result.success(
            ListMessagesResponse(
                objectType = "list",
                data = listOf(
                    Message(
                        id = "msg_999",
                        objectType = "message",
                        createdAt = 111,
                        threadId = threadId,
                        role = MessageRole.ASSISTANT.value,
                        content = listOf(
                            Content.TextContent(
                                type = "text",
                                text = Text(summaryText)
                            )
                        )
                    )
                ),
                hasMore = false
            )
        )

        // When
        val results = mutableListOf<Result<String>>()
        client.summarizeSyllabus(testFile).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isSuccess)
        assertEquals(summaryText, firstResult.getOrNull())

        // Verify all necessary calls
        coVerify { assistant.uploadFile(testFile, FilePurpose.ASSISTANTS) }
        coVerify { assistant.createThread(any()) }
        coVerify { assistant.createRun(threadId, any()) }
        coVerify { assistant.deleteThread(threadId) }
    }

    /**
     * Tests behavior when the file upload fails immediately.
     */
    @Test
    fun `summarizeSyllabus handles file upload failure`() = runTest {
        // Given
        val testFile = File("dummy_path")

        coEvery { assistant.uploadFile(testFile, FilePurpose.ASSISTANTS) } returns Result.failure(
            Exception("File upload failed")
        )

        // When
        val results = mutableListOf<Result<String>>()
        client.summarizeSyllabus(testFile).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isFailure)
        assertIs<Exception>(firstResult.exceptionOrNull())
    }

    /**
     * Tests behavior when thread creation fails after file upload succeeds.
     */
    @Test
    fun `summarizeSyllabus handles thread creation failure`() = runTest {
        // Given
        val testFile = File.createTempFile("dummy_path", ".txt")
        val uploadedFileId = "file_upload_123"

        coEvery { assistant.uploadFile(testFile, FilePurpose.ASSISTANTS) } returns Result.success(
            FileUploadResponse(
                id = uploadedFileId,
                bytes = 1024,
                createdAt = 123,
                filename = "dummy_path.txt",
                objectType = "file",
                purpose = FilePurpose.ASSISTANTS.value,
            )
        )
        coEvery { assistant.createThread(any()) } returns Result.failure(Exception("Failed to create thread"))

        // When
        val results = mutableListOf<Result<String>>()
        client.summarizeSyllabus(testFile).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isFailure)
        assertIs<Exception>(firstResult.exceptionOrNull())
    }

    /**
     * Tests behavior when run creation fails.
     */
    @Test
    fun `summarizeSyllabus handles run creation failure`() = runTest {
        // Given
        val testFile = File.createTempFile("dummy_path", ".txt")
        val uploadedFileId = "file_upload_123"
        val threadId = "thread_123"

        coEvery { assistant.uploadFile(testFile, FilePurpose.ASSISTANTS) } returns Result.success(
            FileUploadResponse(
                id = uploadedFileId,
                bytes = 1024,
                createdAt = 123,
                filename = "dummy_path.txt",
                objectType = "file",
                purpose = FilePurpose.ASSISTANTS.value,
            )
        )
        coEvery { assistant.createThread(any()) } returns Result.success(Thread(id = threadId, objectType = "thread", createdAt = 456))
        coEvery { assistant.createRun(threadId, any()) } returns Result.failure(Exception("Run creation failed"))
        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        // When
        val results = mutableListOf<Result<String>>()
        client.summarizeSyllabus(testFile).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isFailure)
        assertIs<Exception>(firstResult.exceptionOrNull())
    }

    /**
     * Tests a scenario where the run ends with status FAILED (or any status other than COMPLETED).
     */
    @Test
    fun `summarizeSyllabus handles run failure status`() = runTest {
        // Given
        val testFile = File.createTempFile("dummy_path", ".txt")
        val uploadedFileId = "file_upload_123"
        val threadId = "thread_123"
        val runId = "run_123"

        coEvery { assistant.uploadFile(testFile, FilePurpose.ASSISTANTS) } returns Result.success(
            FileUploadResponse(
                id = uploadedFileId,
                bytes = 1024,
                createdAt = 123,
                filename = "dummy_path.txt",
                objectType = "file",
                purpose = FilePurpose.ASSISTANTS.value,
            )
        )
        coEvery { assistant.createThread(any()) } returns Result.success(
            Thread(id = threadId, objectType = "thread", createdAt = 456)
        )

        val failedRun = Run(
            id = runId,
            objectType = "run",
            createdAt = 999,
            threadId = threadId,
            assistantId = "asst_123",
            status = RunStatus.FAILED.value,
            tools = emptyList(),
            truncationStrategy = TruncationStrategy(),
            model = "gpt-4",
            lastError = LastError(message = "Something went wrong", code = "500"),
        )
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(failedRun)
        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        // When
        val results = mutableListOf<Result<String>>()
        client.summarizeSyllabus(testFile).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isFailure)
        val ex = firstResult.exceptionOrNull()
        assertIs<IllegalStateException>(ex)
        assertEquals("Run failed: Something went wrong", ex.message)
    }
}
