package org.example.shared.data.remote.assistant.generator

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.QuestionGeneratorClient
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Question
import org.example.shared.domain.model.assistant.*
import kotlin.test.*

class QuestionGeneratorClientImplTest {

    private lateinit var assistant: AIAssistantClient
    private lateinit var client: QuestionGeneratorClientImpl<Question.MultipleChoice>

    @BeforeTest
    fun setup() {
        assistant = mockk()
        // We’ll use the MultipleChoice serializer for demonstration; adapt as needed.
        client = QuestionGeneratorClientImpl(
            assistantClient = assistant,
            assistantId = "testAssistantId",
            serializer = Question.MultipleChoice.serializer()
        )
    }

    /**
     * Tests a successful question generation flow (no required action, status goes directly to COMPLETED).
     */
    @Test
    fun `generateQuestion single success flow`() = runTest {
        // Given
        val threadId = "thread_123"
        val runId = "run_123"
        val thread = Thread(
            id = threadId,
            objectType = "thread",
            createdAt = 123
        )
        // The run immediately completes
        val completedRun = Run(
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

        // Mock assistant methods
        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(thread)
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(completedRun)

        // The CompletionProcessor will fetch the assistant"s final message containing the question
        val expectedQuestion = Question.MultipleChoice(
            text = "Which of the following is a coroutine builder in Kotlin?",
            correctAnswer = "b",
            options = listOf(
                Question.MultipleChoice.Option("a", "runBlocking"),
                Question.MultipleChoice.Option("b", "launch"),
                Question.MultipleChoice.Option("c", "async")
            )
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
                                text = Text(
                                    Json.encodeToString(
                                        Question.MultipleChoice.serializer(),
                                        expectedQuestion
                                    )
                                )
                            )
                        )
                    )
                ),
                hasMore = false
            )
        )

        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        // The context for generating the question
        val context = QuestionGeneratorClient.Context(
            topic = "Kotlin Coroutines",
            level = Level.BEGINNER
        )

        // When
        val results = mutableListOf<Result<Question.MultipleChoice>>()
        client.generateQuestion(context, number = 1).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isSuccess)

        val actual = firstResult.getOrNull()
        assertNotNull(actual)
        assertEquals(expectedQuestion.text, actual.text)
        assertEquals(expectedQuestion.options.size, actual.options.size)
        assertEquals(expectedQuestion.correctAnswer, actual.correctAnswer)

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
    fun `generateQuestion handles thread creation failure`() = runTest {
        // Given
        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.failure(Exception("Failed to create thread"))

        val context = QuestionGeneratorClient.Context(
            topic = "Kotlin Basics",
            level = Level.BEGINNER
        )

        // When
        val results = mutableListOf<Result<Question.MultipleChoice>>()
        client.generateQuestion(context, number = 1).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isFailure)
        assertIs<Exception>(firstResult.exceptionOrNull())
        assertEquals("Failed to create thread", firstResult.exceptionOrNull()?.message)

        coVerify { assistant.createThread(ThreadRequestBody()) }
        // No run creation or message listing should occur since thread creation failed
        coVerifyAll(true) { assistant.createRun(any(), any()) }
    }

    /**
     * Tests a flow where the run goes into REQUIRES_ACTION and must submit tool outputs before completion.
     */
    @Test
    fun `generateQuestion handles required action flow`() = runTest {
        // Given
        val threadId = "thread_req_action"
        val runId = "run_req_action"
        val thread = Thread(
            id = threadId,
            objectType = "thread",
            createdAt = 123
        )

        // Run initially in REQUIRES_ACTION
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
                            id = "tool_call_987",
                            function = ToolCallFunction(
                                name = "get_context",
                                arguments = "{}"
                            )
                        )
                    )
                )
            )
        )

        // After the required action is fulfilled, the run completes
        val runCompleted = runRequiresAction.copy(status = RunStatus.COMPLETED.value)

        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(thread)
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(runRequiresAction)
        // Once the user submits tool output, the run moves to COMPLETED
        coEvery {
            assistant.submitToolOutput(threadId, runId, any())
        } returns Result.success(runCompleted)

        // We"ll retrieve the run twice: once while it"s still REQUIRES_ACTION, then after tool outputs are submitted
        coEvery { assistant.retrieveRun(threadId, runId) } returnsMany listOf(
            Result.success(runRequiresAction),
            Result.success(runCompleted)
        )

        // The final assistant message
        val expectedQuestion = Question.MultipleChoice(
            text = "Which keyword is used to launch a coroutine in Kotlin?",
            correctAnswer = "a",
            options = listOf(
                Question.MultipleChoice.Option("a", "launch"),
                Question.MultipleChoice.Option("b", "async"),
                Question.MultipleChoice.Option("c", "runBlocking")
            )
        )

        coEvery { assistant.listMessages(threadId, 10, MessagesOrder.DESC) } returns Result.success(
            ListMessagesResponse(
                objectType = "list",
                data = listOf(
                    Message(
                        id = "msg_789",
                        objectType = "message",
                        createdAt = 999,
                        threadId = threadId,
                        role = MessageRole.ASSISTANT.value,
                        content = listOf(
                            Content.TextContent(
                                type = "text",
                                text = Text(
                                    Json.encodeToString(
                                        Question.MultipleChoice.serializer(),
                                        expectedQuestion
                                    )
                                )
                            )
                        )
                    )
                ),
                hasMore = false
            )
        )

        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        val context = QuestionGeneratorClient.Context(
            topic = "Coroutines advanced usage",
            level = Level.INTERMEDIATE
        )

        // When
        val results = mutableListOf<Result<Question.MultipleChoice>>()
        client.generateQuestion(context, number = 1).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isSuccess)
        val actual = firstResult.getOrNull()
        assertNotNull(actual)
        assertEquals(expectedQuestion, actual)

        // Verify calls
        coVerify { assistant.createThread(ThreadRequestBody()) }
        coVerify { assistant.createRun(threadId, any()) }
        coVerify { assistant.retrieveRun(threadId, runId) }
        coVerify { assistant.submitToolOutput(threadId, runId, any()) }
        coVerify { assistant.listMessages(threadId, 10, MessagesOrder.DESC) }
        coVerify { assistant.deleteThread(threadId) }
    }

    /**
     * Tests behavior when a run ends with status FAILED (or anything unexpected) instead of COMPLETED.
     */
    @Test
    fun `generateQuestion handles failed run`() = runTest {
        // Given
        val threadId = "thread_failed"
        val runId = "run_failed"
        val thread = Thread(
            id = threadId,
            objectType = "thread",
            createdAt = 123
        )

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
            lastError = LastError(message = "Some internal error", code = "500")
        )

        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(thread)
        coEvery { assistant.createRun(threadId, any()) } returns Result.success(failedRun)
        coEvery { assistant.deleteThread(threadId) } returns Result.success(Unit)

        val context = QuestionGeneratorClient.Context(
            topic = "Kotlin Flow",
            level = Level.BEGINNER
        )

        // When
        val results = mutableListOf<Result<Question.MultipleChoice>>()
        client.generateQuestion(context, number = 1).collect { results.add(it) }

        // Then
        assertEquals(1, results.size)
        val firstResult = results.first()
        assertTrue(firstResult.isFailure)
        val exception = firstResult.exceptionOrNull()
        assertIs<IllegalStateException>(exception)
        assertEquals("Run failed: Some internal error", exception?.message)

        coVerify { assistant.createThread(ThreadRequestBody()) }
        coVerify { assistant.createRun(threadId, any()) }
        // No message listing because the run was never completed
        coVerify { assistant.deleteThread(threadId) }
    }
}
