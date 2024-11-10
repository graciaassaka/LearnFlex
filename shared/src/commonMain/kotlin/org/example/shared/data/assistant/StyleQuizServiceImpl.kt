package org.example.shared.data.assistant

import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import org.example.shared.data.model.*
import org.example.shared.data.model.Function
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.data.util.Style
import org.example.shared.domain.service.AIAssistantClient
import org.example.shared.domain.service.StyleQuizService

/**
 * Implementation of the StyleQuizService interface.
 * This service generates a learning style quiz based on user preferences.
 *
 * @property assistant The OpenAIAssistantClient used to interact with the OpenAI API.
 */
class StyleQuizServiceImpl(private val assistant: AIAssistantClient) : StyleQuizService {
    /**
     * Generates a learning style quiz based on the provided learning preferences.
     *
     * @param preferences The user's learning preferences.
     * @return A Result containing the generated StyleQuestionnaire.
     */
    override suspend fun generateQuiz(preferences: LearningPreferences) = with(assistant) {
        var result: Result<StyleQuestionnaire>
        var thread: Thread? = null

        try {
            thread = createThread().getOrThrow()

            createMessage(thread.id, MessageRequestBody(MessageRole.USER.value, MESSAGE)).onFailure { throw it }

            var run = createRun(thread.id).getOrThrow()

            while (RunStatus.valueOf(run.status.uppercase()).isRunActive()) {
                run = retrieveRun(thread.id, run.id).getOrThrow().apply {
                    handleRequiredAction(thread.id, preferences)
                }

                delay(POLLING_INTERVAL)
            }

            val questionnaire = run.processCompletion(thread.id)
            result = Result.success(questionnaire)
        } catch (e: Exception) {
            result = Result.failure(e)
        } finally {
            thread?.let { t -> deleteThread(t.id).onFailure { result = Result.failure(it) } }
        }

        return@with result
    }

    /**
     * Creates a new run for the given thread ID.
     *
     * @param threadId The ID of the thread.
     * @return A Result containing the created Run.
     */
    private suspend fun createRun(threadId: String) = assistant.createRun(
        threadId = threadId,
        requestBody = RunRequestBody(
            assistantId = OpenAIConstants.STYLE_ASSISTANT_ID,
            instructions = INSTRUCTIONS,
            tools = listOf(Tool.FunctionTool(constructFunction()))
        )
    )

    /**
     * Constructs a Function object with predefined parameters.
     *
     * @return The constructed Function object.
     */
    private fun constructFunction() = Function(
        name = FUN_NAME,
        description = FUN_DESC,
        strict = true,
        parameters = Parameters(
            type = "object",
            properties = buildJsonObject {
                put("field", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The user's field of study"))
                    put("enum", buildJsonArray { Field.entries.forEach { add(JsonPrimitive(it.name)) } })
                })
                put("level", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The user's level of expertise"))
                    put("enum", buildJsonArray { Level.entries.forEach { add(JsonPrimitive(it.name)) } })
                })
                put("goal", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The user's learning goal"))
                })
            },
            required = listOf("field", "level", "goal"),
            additionalProperties = false
        )
    )


    /**
     * Determines if the given run status is active.
     *
     * @return `true` if the run status is either QUEUED, IN_PROGRESS, or REQUIRES_ACTION, `false` otherwise.
     */
    private fun RunStatus.isRunActive(): Boolean = when (this) {
        RunStatus.QUEUED,
        RunStatus.IN_PROGRESS,
        RunStatus.REQUIRES_ACTION -> true

        else                      -> false
    }

    /**
     * Handles the required action for the given run.
     *
     * @param threadId The ID of the thread.
     * @param preferences The user's learning preferences.
     */
    private suspend fun Run.handleRequiredAction(threadId: String, preferences: LearningPreferences) =
        requiredAction?.let { action ->
            when (RequiredActionType.valueOf(action.type.uppercase())) {
                RequiredActionType.SUBMIT_TOOL_OUTPUTS -> {
                    requiredAction
                        .submitToolOutputs
                        ?.toolCalls
                        ?.forEach { call -> submitToolOutput(threadId, id, call.id, preferences) }
                        ?: throw IllegalStateException("No tool calls to submit")
                }
            }
        }


    /**
     * Submits the tool output for the given tool call ID.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     * @param toolCallId The ID of the tool call.
     * @param preferences The user's learning preferences.
     */
    private suspend fun submitToolOutput(
        threadId: String,
        runId: String,
        toolCallId: String,
        preferences: LearningPreferences
    ) = assistant.submitToolOutput(
        threadId = threadId,
        runId = runId,
        requestBody = SubmitToolOutputsRequestBody(
            listOf(ToolOutput(
                toolCallId = toolCallId,
                output = Json.encodeToString(
                    JsonObject.serializer(),
                    buildJsonObject {
                        put("field", JsonPrimitive(preferences.field))
                        put("level", JsonPrimitive(preferences.level))
                        put("goal", JsonPrimitive(preferences.goal))
                    }
                )
            ))
        ))

    /**
     * Processes the completion of the run.
     *
     * @param threadId The ID of the thread.
     * @return The generated StyleQuestionnaire.
     * @throws IllegalStateException if the run status is not COMPLETED.
     */
    private suspend fun Run.processCompletion(threadId: String) =
        when (RunStatus.valueOf(status.uppercase())) {
            RunStatus.COMPLETED -> getAssistantMessage(threadId)
            RunStatus.FAILED -> throw IllegalStateException("Run failed: ${lastError?.message}")
            RunStatus.CANCELLED -> throw IllegalStateException("Run cancelled: ${lastError?.message}")
            RunStatus.INCOMPLETE -> throw IllegalStateException("Run incomplete: ${incompleteDetails?.reason}")
            RunStatus.EXPIRED -> throw IllegalStateException("Run expired: ${lastError?.message}")
            else -> throw IllegalStateException("Unexpected status: $status")
        }

    /**
     * Retrieves the assistant message for the given thread ID.
     *
     * @param threadId The ID of the thread.
     * @return The decoded StyleQuestionnaire.
     */
    private suspend fun getAssistantMessage(threadId: String) = assistant
        .listMessages(threadId, 5, MessagesOrder.ASC)
        .getOrThrow().data
        .first { it.role == MessageRole.ASSISTANT.value }
        .let { Json.decodeFromString<StyleQuestionnaire>((it.content.first() as Content.TextContent).text.value) }

    /**
     * Evaluates the responses and calculates the dominant learning style and breakdown.
     *
     * @param responses The list of learning style responses.
     * @return A Result containing the StyleResult.
     * @throws IllegalArgumentException if the responses list is empty.
     */
    override fun evaluateResponses(responses: List<Style>) =
        responses.groupingBy { it.value }.eachCount().runCatching {
            if (responses.isEmpty()) throw IllegalArgumentException("Responses cannot be empty")
            StyleResult(
                dominantStyle = maxBy { it.value }.key,
                styleBreakdown = StyleBreakdown(
                    visual = getOrDefault(Style.VISUAL.value, 0) * 100 / responses.size,
                    reading = getOrDefault(Style.READING.value, 0) * 100 / responses.size,
                    kinesthetic = getOrDefault(Style.KINESTHETIC.value, 0) * 100 / responses.size
                )
            )
        }

    companion object {
        private const val MESSAGE = "I want to take a learning style assessment"
        private const val FUN_NAME = "get_user_context"
        private const val FUN_DESC = "Get the user's learning context to generate personalized assessment questions"
        private const val INSTRUCTIONS = "Generate learning style assessment questions based on the user's context"
        private const val POLLING_INTERVAL = 1000L
    }
}