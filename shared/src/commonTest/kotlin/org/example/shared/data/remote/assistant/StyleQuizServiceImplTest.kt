package org.example.shared.data.remote.assistant

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.model.*
import org.example.shared.domain.constant.Style
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class StyleQuizServiceImplTest {
    private lateinit var styleQuizService: StyleQuizServiceImpl
    private lateinit var assistant: OpenAIAssistantClient
    private lateinit var run: Run

    @Before
    fun setUp() {
        assistant = mockk<OpenAIAssistantClient>(relaxed = true)
        styleQuizService = StyleQuizServiceImpl(assistant)

        run = mockk<Run>(relaxed = true)
        val listMessagesResponse = ListMessagesResponse(objectType = "list", data = listOf(message), hasMore = false)

        coEvery { assistant.createThread() } returns Result.success(mockk(relaxed = true))
        coEvery { assistant.deleteThread(any()) } returns Result.success(Unit)
        coEvery { assistant.createMessage(any(), any()) } returns Result.success(message)
        coEvery { assistant.createRun(any(), any()) } returns Result.success(run)
        every { run.status } returns RunStatus.COMPLETED.value
        coEvery { assistant.listMessages(any(), any(), any()) } returns Result.success(listMessagesResponse)
    }

    @Test
    fun `generateQuiz should return Result#success when all operations succeed`() = runTest {
        // When
        val result = styleQuizService.generateQuiz(mockk())

        // Then
        coVerify {
            assistant.createThread()
            assistant.createMessage(any(), any())
            assistant.createRun(any(), any())
            assistant.listMessages(any(), any(), any())
            assistant.deleteThread(any())
        }

        assert(result.isSuccess)
        assertTrue(result.getOrThrow().styleQuestions.isNotEmpty())
    }

    @Test
    fun `generateQuiz should return Result#failure when assistant#createThread fails`() = runTest {
        // Given
        val exception = Exception("Failed to create thread")
        coEvery { assistant.createThread() } returns Result.failure(exception)

        // When
        val result = styleQuizService.generateQuiz(mockk())

        // Then
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `generateQuiz should return Result#failure when assistant#createMessage fails`() = runTest {
        // Given
        val exception = Exception("Failed to create message")
        coEvery { assistant.createMessage(any(), any()) } returns Result.failure(exception)

        // When
        val result = styleQuizService.generateQuiz(mockk())

        // Then
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `generateQuiz should return Result#failure when assistant#createRun fails`() = runTest {
        // Given
        val exception = Exception("Failed to create run")
        coEvery { assistant.createRun(any(), any()) } returns Result.failure(exception)

        // When
        val result = styleQuizService.generateQuiz(mockk())

        // Then
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `generateQuiz should return Result#failure when assistant#listMessages fails`() = runTest {
        // Given
        val exception = Exception("Failed to list messages")
        coEvery { assistant.listMessages(any(), any(), any()) } returns Result.failure(exception)

        // When
        val result = styleQuizService.generateQuiz(mockk())

        // Then
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `generateQuiz should throw an exception when assistant#deleteThread fails`() = runTest {
        // Given
        val exception = Exception("Failed to delete thread")
        coEvery { assistant.deleteThread(any()) } returns Result.failure(exception)

        // When
        val result = styleQuizService.generateQuiz(mockk())

        // Then
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `generateQuiz should call assistant#retrieveRun when RunStatus is IN_PROGRESS`() = runTest {
        // Given
        val inProgressRun = mockk<Run>().apply {
            every { id } returns "run-id"
            every { status } returns RunStatus.IN_PROGRESS.value
        }

        coEvery { assistant.createRun(any(), any()) } returns Result.success(inProgressRun)
        coEvery { assistant.retrieveRun(any(), any()) } returns Result.success(inProgressRun)

        // When
        styleQuizService.generateQuiz(mockk(relaxed = true))

        // Then
        coVerify { assistant.retrieveRun(any(), any()) }
    }

    @Test
    fun `generateQuiz should call assistant#submitToolOutput when RequiredActionType is SUBMIT_TOOL_OUTPUTS`() =
        runTest {
            // Given
            val runWithAction = mockk<Run>().apply {
                every { id } returns "run-id"
                every { status } returns RunStatus.REQUIRES_ACTION.value
                every { requiredAction } returns RequiredAction(
                    type = RequiredActionType.SUBMIT_TOOL_OUTPUTS.value,
                    submitToolOutputs = SubmitToolOutputs(
                        toolCalls = listOf(
                            ToolCall(
                                id = "tool-id",
                                function = ToolCallFunction(
                                    name = "get_user_context",
                                    arguments = "{}"
                                )
                            )
                        )
                    )
                )
            }

            val completedRun = mockk<Run>().apply {
                every { id } returns "run-id"
                every { status } returns RunStatus.COMPLETED.value
                every { requiredAction } returns null
            }

            coEvery { assistant.createRun(any(), any()) } returns Result.success(runWithAction)
            coEvery { assistant.retrieveRun(any(), any()) } returnsMany listOf(
                Result.success(runWithAction),
                Result.success(completedRun)
            )
            coEvery { assistant.submitToolOutput(any(), any(), any()) } returns Result.success(completedRun)

            // Mock the message response
            val messageResponse = ListMessagesResponse(objectType = "list", data = listOf(message), hasMore = false)
            coEvery { assistant.listMessages(any(), any(), any()) } returns Result.success(messageResponse)

            // When
            styleQuizService.generateQuiz(mockk(relaxed = true))

            // Then
            coVerify { assistant.submitToolOutput(any(), any(), any()) }
        }

    @Test
    fun `generateQuiz should return a failure when submitToolOutputs is null`() = runTest {
        // Given
        val runWithAction = mockk<Run>(relaxed = true).apply {
            every { status } returns RunStatus.REQUIRES_ACTION.value
            every { requiredAction } returns RequiredAction(
                type = RequiredActionType.SUBMIT_TOOL_OUTPUTS.value,
                submitToolOutputs = null
            )
        }

        coEvery { assistant.createRun(any(), any()) } returns Result.success(runWithAction)
        coEvery { assistant.retrieveRun(any(), any()) } returns Result.success(runWithAction)

        // When
        val result = styleQuizService.generateQuiz(mockk(relaxed = true))

        // Then
        assert(result.isFailure)
        assertEquals(IllegalStateException::class, result.exceptionOrNull()!!::class)
    }

    @Test
    fun `generateQuiz should return a failure when RunStatus is FAILED`() = runTest {
        // Given
        val failedRun = mockk<Run>(relaxed = true).apply {
            every { status } returns RunStatus.FAILED.value
        }

        coEvery { assistant.createRun(any(), any()) } returns Result.success(failedRun)
        coEvery { assistant.retrieveRun(any(), any()) } returns Result.success(failedRun)

        // When
        val result = styleQuizService.generateQuiz(mockk(relaxed = true))

        // Then
        assert(result.isFailure)
        assertEquals(IllegalStateException::class, result.exceptionOrNull()!!::class)
    }

    @Test
    fun `generateQuiz should return a failure when RunStatus is INCOMPLETE`() = runTest {
        // Given
        val incompleteRun = mockk<Run>(relaxed = true).apply {
            every { status } returns RunStatus.INCOMPLETE.value
        }

        coEvery { assistant.createRun(any(), any()) } returns Result.success(incompleteRun)
        coEvery { assistant.retrieveRun(any(), any()) } returns Result.success(incompleteRun)

        // When
        val result = styleQuizService.generateQuiz(mockk(relaxed = true))

        // Then
        assert(result.isFailure)
        assertEquals(IllegalStateException::class, result.exceptionOrNull()!!::class)
    }

    @Test
    fun `generateQuiz should return a failure when RunStatus is EXPIRED`() = runTest {
        // Given
        val expiredRun = mockk<Run>(relaxed = true).apply {
            every { status } returns RunStatus.EXPIRED.value
        }

        coEvery { assistant.createRun(any(), any()) } returns Result.success(expiredRun)
        coEvery { assistant.retrieveRun(any(), any()) } returns Result.success(expiredRun)

        // When
        val result = styleQuizService.generateQuiz(mockk(relaxed = true))

        // Then
        assert(result.isFailure)
        assertEquals(IllegalStateException::class, result.exceptionOrNull()!!::class)
    }

    @Test
    fun `generateQuiz should return a failure when an unexpected RunStatus is returned`() = runTest {
        // Given
        val unexpectedRun = mockk<Run>(relaxed = true).apply {
            every { status } returns RunStatus.CANCELLED.value
        }

        coEvery { assistant.createRun(any(), any()) } returns Result.success(unexpectedRun)
        coEvery { assistant.retrieveRun(any(), any()) } returns Result.success(unexpectedRun)

        // When
        val result = styleQuizService.generateQuiz(mockk(relaxed = true))

        // Then
        assert(result.isFailure)
        assertEquals(IllegalStateException::class, result.exceptionOrNull()!!::class)
    }

    @Test
    fun `evaluateResponses should return correct dominant style and breakdown when all styles are present`() {
        // Given
        val responses = listOf(Style.VISUAL, Style.READING, Style.KINESTHETIC, Style.VISUAL)

        // When
        val result = styleQuizService.evaluateResponses(responses)

        // Then
        val styleResult = result.getOrThrow()
        assertEquals("visual", styleResult.dominantStyle)
        assertEquals(50, styleResult.styleBreakdown.visual)
        assertEquals(25, styleResult.styleBreakdown.reading)
        assertEquals(25, styleResult.styleBreakdown.kinesthetic)
    }

    @Test
    fun `evaluateResponses should return correct dominant style and breakdown when single style is present`() {
        // Given
        val responses = listOf(Style.VISUAL, Style.VISUAL, Style.VISUAL)

        // When
        val result = styleQuizService.evaluateResponses(responses)

        // Then
        val styleResult = result.getOrThrow()
        assertEquals("visual", styleResult.dominantStyle)
        assertEquals(100, styleResult.styleBreakdown.visual)
        assertEquals(0, styleResult.styleBreakdown.reading)
        assertEquals(0, styleResult.styleBreakdown.kinesthetic)
    }

    @Test
    fun `evaluateResponses should return a failure when responses are empty`() {
        // Given
        val responses = emptyList<Style>()

        // When
        val result = styleQuizService.evaluateResponses(responses)

        // Then
        assert(result.isFailure)
        assertEquals(IllegalArgumentException::class, result.exceptionOrNull()!!::class)
    }

    @Test
    fun `evaluateResponses should return correct dominant style and breakdown when multiple dominant styles are present`() {
        // Given
        val responses = listOf(Style.VISUAL, Style.READING, Style.VISUAL, Style.READING)

        // When
        val result = styleQuizService.evaluateResponses(responses)

        // Then
        val styleResult = result.getOrThrow()
        assertTrue(styleResult.dominantStyle == "visual" || styleResult.dominantStyle == "auditory")
        assertEquals(50, styleResult.styleBreakdown.visual)
        assertEquals(50, styleResult.styleBreakdown.reading)
        assertEquals(0, styleResult.styleBreakdown.kinesthetic)
    }

    companion object {
        private val message = Message(
            id = "message-id",
            objectType = "message",
            createdAt = 1234567890,
            threadId = "thread-id",
            role = MessageRole.ASSISTANT.value,
            content = listOf(
                Content.TextContent(
                    text = Text(
                        value = """
                        {
                          "questions": [
                            {
                              "scenario": "You're learning about data structures like trees and graphs in your computer science course. What's your preferred way to understand these concepts?",
                              "options": [
                                {
                                  "text": "Watch a video tutorial that explains data structures with animations and visual diagrams.",
                                  "style": "visual"
                                },
                                {
                                  "text": "Read a comprehensive article or textbook chapter that details data structures and algorithms.",
                                  "style": "reading"
                                },
                                {
                                  "text": "Participate in a coding challenge where you implement tree structures and manipulate graphs.",
                                  "style": "kinesthetic"
                                }
                              ]
                            }
                          ]
                        }
                        """.trimIndent()
                    )
                )
            )
        )
    }
}

