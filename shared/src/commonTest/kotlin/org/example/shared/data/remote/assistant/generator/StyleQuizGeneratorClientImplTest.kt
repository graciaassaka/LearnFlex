package org.example.shared.data.remote.assistant.generator

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.assistant.*
import kotlin.test.*

class StyleQuizGeneratorClientImplTest {
    private lateinit var assistant: AIAssistantClient
    private lateinit var client: StyleQuizGeneratorClientImpl

    @BeforeTest
    fun setup() {
        assistant = mockk<AIAssistantClient>()
        client = StyleQuizGeneratorClientImpl(assistant, "testId")
    }

    @Test
    fun `streamQuestions success flow`() = runTest {
        // Setup
        val threadId = "thread_123"
        val runId = "run_123"
        val preferences = Profile.LearningPreferences(Field.COMPUTER_SCIENCE.name, Level.BEGINNER.name, "Learn Kotlin")
        val thread = Thread(id = threadId, objectType = "thread", createdAt = 123)
        val messageId = "msg_123"
        val question = StyleQuizGeneratorClient.StyleQuestion(
            options = listOf(
                StyleQuizGeneratorClient.StyleQuestion.StyleOption("visual", "Watch a video"),
                StyleQuizGeneratorClient.StyleQuestion.StyleOption("reading", "Read documentation"),
                StyleQuizGeneratorClient.StyleQuestion.StyleOption("kinesthetic", "Code examples")
            ),
            scenario = "How would you prefer to learn Kotlin?"
        )

        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(thread)
        coEvery { assistant.createMessage(any(), any()) } returns Result.success(
            Message(
                id = messageId,
                objectType = "message",
                createdAt = 123,
                threadId = threadId,
                role = "user",
                content = listOf()
            )
        )
        coEvery { assistant.createRun(any(), any()) } returns Result.success(
            Run(
                id = runId,
                objectType = "run",
                createdAt = 123,
                threadId = threadId,
                assistantId = "asst_123",
                status = RunStatus.COMPLETED.value,
                tools = emptyList(),
                truncationStrategy = TruncationStrategy(),
                model = "gpt-4"
            )
        )
        coEvery { assistant.listMessages(any(), any(), any()) } returns Result.success(
            ListMessagesResponse(
                objectType = "list",
                data = listOf(
                    Message(
                        id = "msg_456",
                        objectType = "message",
                        createdAt = 124,
                        threadId = threadId,
                        role = "assistant",
                        content = listOf(
                            Content.TextContent(
                                type = "text",
                                text = Text(Json.encodeToString(StyleQuizGeneratorClient.StyleQuestion.serializer(), question))
                            )
                        )
                    )
                ),
                hasMore = false
            )
        )
        coEvery { assistant.deleteThread(any()) } returns Result.success(Unit)
        coEvery { assistant.cancelRun(any(), any()) } returns Result.success(Unit)

        // Test
        val questions = mutableListOf<Result<StyleQuizGeneratorClient.StyleQuestion>>()
        client.streamQuestions(preferences, 1).collect { questions.add(it) }

        // Verify
        assertEquals(1, questions.size)
        assertTrue(questions[0].isSuccess)
        assertEquals(question, questions[0].getOrNull())
        coVerify { assistant.createThread(ThreadRequestBody()) }
        coVerify { assistant.deleteThread(threadId) }
    }

    @Test
    fun `evaluateResponses calculates correct style breakdown`() {
        val responses = listOf(
            Style.READING,
            Style.READING,
            Style.READING,
            Style.KINESTHETIC
        )

        val result = client.evaluateResponses(responses).getOrNull()
        assertNotNull(result)

        assertEquals(Style.READING.name, result.dominant)
        assertEquals(75, result.breakdown.reading)
        assertEquals(25, result.breakdown.kinesthetic)
    }

    @Test
    fun `evaluateResponses handles empty responses`() {
        val result = client.evaluateResponses(emptyList())
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `streamQuestions handles thread creation failure`() = runTest {
        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.failure(Exception("Failed to create thread"))

        val questions = mutableListOf<Result<StyleQuizGeneratorClient.StyleQuestion>>()
        client.streamQuestions(
            Profile.LearningPreferences(Field.COMPUTER_SCIENCE.name, Level.BEGINNER.name, "Learn Kotlin"),
            1
        ).collect { questions.add(it) }

        assertEquals(1, questions.size)
        assertTrue(questions[0].isFailure)
        assertIs<Exception>(questions[0].exceptionOrNull())
    }

    @Test
    fun `streamQuestions handles required action flow`() = runTest {
        val threadId = "thread_123"
        val runId = "run_123"
        val preferences = Profile.LearningPreferences(Field.COMPUTER_SCIENCE.name, Level.BEGINNER.name, "Learn Kotlin")
        val question = StyleQuizGeneratorClient.StyleQuestion(
            options = listOf(
                StyleQuizGeneratorClient.StyleQuestion.StyleOption("visual", "Watch a video")
            ),
            scenario = "Test scenario"
        )

        // Setup initial success responses
        coEvery { assistant.createThread(ThreadRequestBody()) } returns Result.success(Thread(threadId, "thread", 123))
        coEvery { assistant.createMessage(any(), any()) } returns Result.success(
            Message(
                id = "msg_123",
                objectType = "message",
                createdAt = 123,
                threadId = threadId,
                role = "user",
                content = listOf()
            )
        )

        // Setup run with required action
        val runWithAction = mockk<Run>()
        every { runWithAction.status } returns RunStatus.REQUIRES_ACTION.value
        every { runWithAction.id } returns runId
        every { runWithAction.requiredAction } returns RequiredAction(
            type = RequiredActionType.SUBMIT_TOOL_OUTPUTS.value,
            submitToolOutputs = SubmitToolOutputs(
                listOf(
                    ToolCall(
                        id = "call_123",
                        function = ToolCallFunction(
                            name = "get_user_context",
                            arguments = "{}"
                        )
                    )
                )
            )
        )

        val completedRun = mockk<Run>()
        every { completedRun.status } returns RunStatus.COMPLETED.value
        every { completedRun.id } returns runId

        coEvery { assistant.createRun(any(), any()) } returns Result.success(runWithAction)
        coEvery { assistant.retrieveRun(any(), any()) } returnsMany listOf(
            Result.success(runWithAction),
            Result.success(completedRun)
        )
        coEvery { assistant.submitToolOutput(any(), any(), any()) } returns Result.success(completedRun)

        // Setup message response
        coEvery { assistant.listMessages(any(), any(), any()) } returns Result.success(
            ListMessagesResponse(
                objectType = "list",
                data = listOf(
                    Message(
                        id = "msg_456",
                        objectType = "message",
                        createdAt = 124,
                        threadId = threadId,
                        role = "assistant",
                        content = listOf(
                            Content.TextContent(
                                type = "text",
                                text = Text(Json.encodeToString(StyleQuizGeneratorClient.StyleQuestion.serializer(), question))
                            )
                        )
                    )
                ),
                hasMore = false
            )
        )
        coEvery { assistant.deleteThread(any()) } returns Result.success(Unit)
        coEvery { assistant.cancelRun(any(), any()) } returns Result.success(Unit)

        // Test
        val questions = mutableListOf<Result<StyleQuizGeneratorClient.StyleQuestion>>()
        client.streamQuestions(preferences, 1).collect { questions.add(it) }

        // Verify
        coVerify { assistant.submitToolOutput(threadId, runId, any()) }
        assertEquals(1, questions.size)
        assertTrue(questions[0].isSuccess)
    }
}