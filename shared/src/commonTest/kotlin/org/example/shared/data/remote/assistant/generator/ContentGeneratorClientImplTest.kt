package org.example.shared.data.remote.assistant.generator

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.assistant.*
import kotlin.test.*

class ContentGeneratorClientImplTest {
    private lateinit var assistant: AIAssistantClient
    private lateinit var client: ContentGeneratorClientImpl

    @BeforeTest
    fun setup() {
        assistant = mockk()
        client = ContentGeneratorClientImpl(assistant, "testAssistantId")
    }

    /**
     * Tests a successful content generation flow (no required action, status goes directly to COMPLETED).
     */
    @Test
    fun `generateContent success flow`() = runTest {
        // Given
        val threadId = "thread_123"
        val runId = "run_123"
        val thread = Thread(id = threadId, objectType = "thread", createdAt = 123)

        // Mock the assistant methods
        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(thread)
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(
            Run(
                id = runId,
                objectType = "run",
                createdAt = 456,
                threadId = threadId,
                assistantId = "asst_123",
                status = RunStatus.COMPLETED.value,
                tools = emptyList(),
                truncationStrategy = TruncationStrategy(),
                model = "gpt-4"
            )
        )

        // The CompletionProcessor will fetch the latest assistant message
        // that contains the JSON-encoded GeneratedResponse
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = "My Lesson Title",
            description = "A short description about the lesson content",
            content = listOf("Introduction", "Main topic", "Summary")
        )

        coEvery { assistant.listMessages(threadId, 10, MessagesOrder.DESC) } returns Result.success(
            ListMessagesResponse(
                objectType = "list",
                data = listOf(
                    Message(
                        id = "msg_999",
                        objectType = "message",
                        createdAt = 789,
                        threadId = threadId,
                        role = MessageRole.ASSISTANT.value,
                        content = listOf(
                            Content.TextContent(
                                type = "text",
                                text = Text(Json.encodeToString(ContentGeneratorClient.GeneratedResponse.serializer(), generatedResponse))
                            )
                        )
                    )
                ),
                hasMore = false
            )
        )

        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        // The context for generating content
        val context = ContentGeneratorClient.Context(
            field = Field.COMPUTER_SCIENCE,
            level = Level.BEGINNER,
            style = Profile.LearningStyle(
                dominant = Style.READING.name,
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 70,
                    kinesthetic = 30
                )
            ),
            type = ContentType.LESSON,
            contentDescriptors = listOf(
                ContentGeneratorClient.Context.ContentDescriptor(
                    type = ContentType.SECTION,
                    title = "Getting Started",
                    description = "Setup and basic syntax"
                )
            )
        )

        // When
        val results = mutableListOf<Result<ContentGeneratorClient.GeneratedResponse>>()
        client.generateContent(context).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isSuccess)

        val actual = firstResult.getOrNull()
        assertNotNull(actual)
        assertEquals(generatedResponse.title, actual.title)
        assertEquals(generatedResponse.description, actual.description)
        assertEquals(generatedResponse.content, actual.content)

        // Verify assistant interactions
        coVerify { assistant.createThread(ThreadRequestBody()) }
        coVerify { assistant.createRun(threadId, any()) }
        coVerify { assistant.listMessages(threadId, 10, MessagesOrder.DESC) }
        coVerify { assistant.deleteThread(threadId) }
    }

    /**
     * Tests behavior when thread creation fails immediately.
     */
    @Test
    fun `generateContent handles thread creation failure`() = runTest {
        // Given
        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.failure(Exception("Failed to create thread"))

        val context = ContentGeneratorClient.Context(
            field = Field.COMPUTER_SCIENCE,
            level = Level.BEGINNER,
            style = Profile.LearningStyle(
                dominant = Style.READING.name,
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 70,
                    kinesthetic = 30
                )
            ),
            type = ContentType.LESSON,
            contentDescriptors = emptyList()
        )

        // When
        val results = mutableListOf<Result<ContentGeneratorClient.GeneratedResponse>>()
        client.generateContent(context).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        assertTrue(results.first().isFailure)
        assertIs<Exception>(results.first().exceptionOrNull())
    }

    /**
     * Tests a flow where the run goes into REQUIRES_ACTION and must submit tool outputs before completion.
     */
    @Test
    fun `generateContent handles required action flow`() = runTest {
        // Given
        val threadId = "thread_123"
        val runId = "run_123"
        val thread = Thread(id = threadId, objectType = "thread", createdAt = 123)

        // Create a run initially in REQUIRES_ACTION
        val runRequiresAction = Run(
            id = runId,
            objectType = "run",
            createdAt = 123,
            threadId = threadId,
            assistantId = "asst_123",
            status = RunStatus.REQUIRES_ACTION.value,
            tools = emptyList(),
            truncationStrategy = TruncationStrategy(),
            model = "gpt-4",
            requiredAction = RequiredAction(
                type = RequiredActionType.SUBMIT_TOOL_OUTPUTS.value,
                submitToolOutputs = SubmitToolOutputs(
                    listOf(
                        ToolCall(
                            id = "tool_call_123",
                            function = ToolCallFunction(
                                name = "get_context",
                                arguments = "{}"
                            )
                        )
                    )
                )
            )
        )

        // After the required action is submitted, the run completes
        val runCompleted = runRequiresAction.copy(status = RunStatus.COMPLETED.value)

        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(thread)
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(runRequiresAction)
        // Once the user "submits tool output," we mock the run as completed
        coEvery { assistant.submitToolOutput(threadId, runId, any()) } returns Result.success(runCompleted)

        // The CompletionProcessor will fetch the final assistant message
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = "Lesson on Asynchronous Flows",
            description = "Detailed content about Kotlin flows",
            content = listOf("Flow basics", "Cancellation", "Cold and Hot flows")
        )

        coEvery { assistant.listMessages(threadId, 10, MessagesOrder.DESC) } returns Result.success(
            ListMessagesResponse(
                objectType = "list",
                data = listOf(
                    Message(
                        id = "msg_999",
                        objectType = "message",
                        createdAt = 789,
                        threadId = threadId,
                        role = MessageRole.ASSISTANT.value,
                        content = listOf(
                            Content.TextContent(
                                type = "text",
                                text = Text(Json.encodeToString(ContentGeneratorClient.GeneratedResponse.serializer(), generatedResponse))
                            )
                        )
                    )
                ),
                hasMore = false
            )
        )

        // We'll retrieve the run twice: once while it's still REQUIRES_ACTION, then after we submit tool outputs
        coEvery { assistant.retrieveRun(threadId, runId) } returnsMany listOf(
            Result.success(runRequiresAction),
            Result.success(runCompleted)
        )

        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        val context = ContentGeneratorClient.Context(
            field = Field.COMPUTER_SCIENCE,
            level = Level.BEGINNER,
            style = Profile.LearningStyle(
                dominant = Style.READING.name,
                breakdown = Profile.LearningStyleBreakdown(reading = 80, kinesthetic = 20)
            ),
            type = ContentType.LESSON,
            contentDescriptors = listOf(
                ContentGeneratorClient.Context.ContentDescriptor(
                    type = ContentType.SECTION,
                    title = "Flows Introduction",
                    description = "Concepts and definitions"
                )
            )
        )

        // When
        val results = mutableListOf<Result<ContentGeneratorClient.GeneratedResponse>>()
        client.generateContent(context).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isSuccess)
        assertEquals(generatedResponse, firstResult.getOrNull())

        // Verify calls
        coVerify { assistant.createThread(ThreadRequestBody()) }
        coVerify { assistant.createRun(threadId, any()) }
        coVerify { assistant.retrieveRun(threadId, runId) }
        coVerify { assistant.submitToolOutput(threadId, runId, any()) }
        coVerify { assistant.deleteThread(threadId) }
    }

    /**
     * Tests behavior when a run ends with status FAILED (or anything unexpected) instead of COMPLETED.
     */
    @Test
    fun `generateContent handles failed run`() = runTest {
        // Given
        val threadId = "thread_123"
        val runId = "run_123"
        val thread = Thread(id = threadId, objectType = "thread", createdAt = 123)

        val failedRun = Run(
            id = runId,
            objectType = "run",
            createdAt = 456,
            threadId = threadId,
            assistantId = "asst_123",
            status = RunStatus.FAILED.value,
            tools = emptyList(),
            truncationStrategy = TruncationStrategy(),
            model = "gpt-4",
            lastError = LastError(message = "Something went wrong", code = "500"),
        )

        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(thread)
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(failedRun)
        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        val context = ContentGeneratorClient.Context(
            field = Field.COMPUTER_SCIENCE,
            level = Level.BEGINNER,
            style = Profile.LearningStyle(
                dominant = Style.READING.name,
                breakdown = Profile.LearningStyleBreakdown(reading = 70, kinesthetic = 30)
            ),
            type = ContentType.LESSON,
            contentDescriptors = emptyList()
        )

        // When
        val results = mutableListOf<Result<ContentGeneratorClient.GeneratedResponse>>()
        client.generateContent(context).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isFailure)
        val exception = firstResult.exceptionOrNull()
        assertIs<IllegalStateException>(exception)
        assertEquals("Run failed: Something went wrong", exception.message)
    }
}
