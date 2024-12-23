package org.example.shared.data.remote.assistant

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import org.example.shared.data.remote.model.*
import org.example.shared.data.remote.model.Function
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.StyleQuizClient
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.*

/**
 * Implementation of the StyleQuizService interface.
 * This service generates a learning style quiz based on user preferences.
 *
 * @property assistant The OpenAIAssistantClient used to interact with the OpenAI API.
 */
class StyleQuizClientImpl(private val assistant: AIAssistantClient) : StyleQuizClient {

    /**
     * Streams questions for the user based on their learning preferences.
     *
     * @param preferences The user's learning preferences.
     * @param number The number of questions to generate.
     * @return A flow of results containing the generated StyleQuestion.
     */
    override fun streamQuestions(preferences: LearningPreferences, number: Int) = with(assistant) {
        flow<Result<StyleQuestion>> {
            var thread: Thread? = null
            val previousScenarios = mutableListOf<String>()
            try {
                thread = createThread().getOrThrow()
                createMessage(thread.id, MessageRequestBody(MessageRole.USER.value, MESSAGE)).getOrThrow()

                var count = 0
                while (count < number) {
                    processRun(thread, preferences, previousScenarios).onSuccess {
                        emit(Result.success(it))
                        previousScenarios.add(it.scenario)
                        count++
                    }.onFailure {
                        emit(Result.failure(it))
                    }

                    if (count < number - 1) {
                        createMessage(thread.id, MessageRequestBody(MessageRole.USER.value, MESSAGE)).getOrThrow()
                    }
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            } finally {
                thread?.let { t -> deleteThread(t.id).onFailure { emit(Result.failure(it)) } }
            }
        }
    }

    /**
     * Processes a run for the specified thread, handling any required actions and ensuring completion.
     *
     * @param thread The thread associated with the run being processed.
     * @param preferences The user's learning preferences which may influence the run processing.
     * @param previousScenarios A list of previously processed scenarios to be included in the current run.
     */
    private suspend fun processRun(thread: Thread, preferences: LearningPreferences, previousScenarios: List<String>) =
        with(assistant) {
            var currentRun = createRun(thread.id, previousScenarios).getOrThrow()

        return@with try {
            while (RunStatus.valueOf(currentRun.status.uppercase()).isRunActive()) {
                if (currentRun.status == "requires_action") {
                    currentRun.handleRequiredAction(thread.id, preferences).onFailure { throw it }
                    delay(POLLING_INTERVAL)
                }

                currentRun = retrieveRun(thread.id, currentRun.id).getOrThrow()
            }

            if (currentRun.status == "completed") Result.success(currentRun.processCompletion(thread.id))
            else throw IllegalStateException("Run ended with unexpected status: ${currentRun.status}")
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            cancelRun(thread.id, currentRun.id).onFailure { Result.failure<Throwable>(it) }
        }
    }


    /**
     * Initiates the creation of a new run for a specified thread, incorporating previous scenarios into the run instructions.
     *
     * @param threadId The ID of the thread in which the run will be created.
     * @param previousScenarios A list of scenarios that were processed previously and need to be included in the instructions.
     */
    private suspend fun createRun(threadId: String, previousScenarios: List<String>) = assistant.createRun(
        threadId = threadId,
        requestBody = RunRequestBody(
            assistantId = OpenAIConstants.STYLE_ASSISTANT_ID,
            instructions = INSTRUCTIONS + previousScenarios.joinToString(", "),
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
    private suspend fun Run.handleRequiredAction(threadId: String, preferences: LearningPreferences) = runCatching {
        requiredAction?.let { action ->
            when (RequiredActionType.valueOf(action.type.uppercase())) {
                RequiredActionType.SUBMIT_TOOL_OUTPUTS -> {
                    val toolCalls = requiredAction.submitToolOutputs?.toolCalls
                        ?: throw IllegalStateException("No tool calls found")

                    val results = toolCalls.map { call ->
                        submitToolOutput(threadId, id, call.id, preferences)
                    }

                    results.filter { it.isFailure }
                        .takeIf { it.isNotEmpty() }
                        ?.let { failures ->
                            throw IllegalStateException(
                                "Failed to submit tool outputs: ${failures.mapNotNull { it.exceptionOrNull()?.message }}"
                            )
                        }

                    results.forEach { it.getOrThrow() }
                }
            }
        } ?: throw IllegalStateException("No required action found")
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
            listOf(
                ToolOutput(
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
        .listMessages(threadId, 10, MessagesOrder.DESC)
        .getOrThrow().data
        .first { it.role == MessageRole.ASSISTANT.value }
        .let { Json.decodeFromString<StyleQuestion>((it.content.first() as Content.TextContent).text.value) }

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
            LearningStyle(
                dominant = maxBy { it.value }.key,
                breakdown = LearningStyleBreakdown(
                    visual = getOrDefault(Style.VISUAL.value, 0) * 100 / responses.size,
                    reading = getOrDefault(Style.READING.value, 0) * 100 / responses.size,
                    kinesthetic = getOrDefault(Style.KINESTHETIC.value, 0) * 100 / responses.size
                )
            )
        }

    companion object {
        private const val MESSAGE = "Generate the next question."
        private const val FUN_NAME = "get_user_context"
        private const val FUN_DESC = "Get the user's learning context to generate personalized assessment questions"
        private const val INSTRUCTIONS = """
                Generate learning style assessment question based on the user's context. 
                Ensure the new question is completely different from the following scenarios:
            """
        private const val POLLING_INTERVAL = 1000L
    }
}