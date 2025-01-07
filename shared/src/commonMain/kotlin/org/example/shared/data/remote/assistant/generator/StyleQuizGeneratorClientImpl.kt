package org.example.shared.data.remote.assistant.generator

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import org.example.shared.data.remote.assistant.util.CompletionProcessor
import org.example.shared.data.remote.assistant.util.RequiredActionHandler
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.client.StyleQuizGeneratorClient.StyleQuestion
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.assistant.*
import org.example.shared.domain.model.assistant.Function

/**
 * Implementation of the StyleQuizGeneratorClient interface that interacts with an AI assistant service
 * to generate style quizzes based on user preferences.
 *
 * @constructor Instantiates the client with an AI assistant client and a specific assistant ID.
 * @param assistantClient The AI assistant client used to handle API interactions.
 * @param assistantId The unique identifier of the assistant to be used for generating style quizzes.
 */
class StyleQuizGeneratorClientImpl(
    private val assistantClient: AIAssistantClient,
    private val assistantId: String
) : StyleQuizGeneratorClient {

    /**
     * Streams questions for the user based on their learning preferences.
     *
     * @param preferences The user's learning preferences.
     * @param number The number of questions to generate.
     * @return A flow of results containing the generated StyleQuestion.
     */
    override fun streamQuestions(preferences: Profile.LearningPreferences, number: Int) = with(assistantClient) {
        flow {
            var thread: Thread? = null

            try {
                val message = "Generate the next question."
                val previousScenarios = mutableListOf<String>()
                var count = 0

                thread = createThread(ThreadRequestBody()).getOrThrow()

                while (count < number) {
                    processRun(thread, preferences, previousScenarios).getOrThrow().let { question ->
                        emit(Result.success(question))
                        previousScenarios.add(question.scenario)
                        count++
                    }

                    if (count < number - 1) createMessage(thread.id, message).getOrThrow()
                }
            } finally {
                thread?.let { t -> deleteThread(t.id).onFailure { emit(Result.failure(it)) } }
            }
        }
    }.catch { e ->
        emit(Result.failure(e))
    }

    /**
     * Processes a run for the specified thread, handling any required actions and ensuring completion.
     *
     * @param thread The thread associated with the run being processed.
     * @param preferences The user's learning preferences which may influence the run processing.
     * @param previousScenarios A list of previously processed scenarios to be included in the current run.
     */
    private suspend fun processRun(thread: Thread, preferences: Profile.LearningPreferences, previousScenarios: List<String>) =
        with(assistantClient) {
            var currentRun = createRun(thread.id, previousScenarios).getOrThrow()

            return@with try {
                while (RunStatus.valueOf(currentRun.status.uppercase()).isRunActive()) {
                    if (currentRun.status == RunStatus.REQUIRES_ACTION.value) {
                        RequiredActionHandler(currentRun, thread.id, preferences, ::submitToolOutput).getOrThrow()
                        delay(1000L)
                    }

                    currentRun = retrieveRun(thread.id, currentRun.id).getOrThrow()
                }

                if (currentRun.status == RunStatus.COMPLETED.value) {
                    Result.success(CompletionProcessor(assistantClient, currentRun, thread.id, ::getAssistantMessage))
                } else {
                    throw IllegalStateException("Run ended with unexpected status: ${currentRun.status}")
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Initiates the creation of a new run for a specified thread, incorporating previous scenarios into the run instructions.
     *
     * @param threadId The ID of the thread in which the run will be created.
     * @param previousScenarios A list of scenarios that were processed previously and need to be included in the instructions.
     */
    private suspend fun createRun(threadId: String, previousScenarios: List<String>) = assistantClient.createRun(
        threadId = threadId,
        requestBody = RunRequestBody(
            assistantId = assistantId,
            instructions = getRunInstructions(previousScenarios),
            tools = listOf(Tool.FunctionTool(constructFunction()))
        )
    )

    /**
     * Generates instructions for running the style quiz generator.
     *
     * @param previousScenarios A list of previous scenarios to avoid repetition.
     * @return A string containing the run instructions.
     */
    private fun getRunInstructions(previousScenarios: List<String>) = """
        Generate learning style assessment question based on the user's context. 
        Ensure the new question is completely different from the following scenarios:
        ${previousScenarios.joinToString("\n")}
    """.trimIndent()

    /**
     * Constructs a Function object with predefined parameters.
     *
     * @return The constructed Function object.
     */
    private fun constructFunction() = Function(
        name = "get_user_context",
        description = "Get the user's learning context",
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

        else -> false
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
        preferences: Profile.LearningPreferences
    ) = assistantClient.submitToolOutput(
        threadId = threadId,
        runId = runId,
        requestBody = SubmitToolOutputsRequestBody(
            listOf(
                ToolOutput(
                    toolCallId = toolCallId,
                    output = Json.encodeToString(
                        serializer = JsonObject.serializer(),
                        value = buildJsonObject {
                            put("field", JsonPrimitive(preferences.field))
                            put("level", JsonPrimitive(preferences.level))
                            put("goal", JsonPrimitive(preferences.goal))
                        }
                    )
                ))
        ))

    /**
     * Creates a message within the specified thread using the given content.
     *
     * @param threadId The ID of the thread to which the message is to be added.
     * @param message The content of the message to be created.
     */
    private suspend fun createMessage(threadId: String, message: String) = assistantClient.createMessage(
        threadId = threadId,
        requestBody = MessageRequestBody(
            role = MessageRole.USER.value,
            content = message
        )
    )

    /**
     * Retrieves the assistant message from a thread.
     *
     * @param assistant The assistant client.
     * @param threadId The ID of the thread to retrieve the message from.
     * @return The assistant message.
     */
    private suspend fun getAssistantMessage(
        assistant: AIAssistantClient,
        threadId: String
    ) = assistant.listMessages(threadId, 10, MessagesOrder.DESC)
        .getOrThrow().data
        .first { it.role == MessageRole.ASSISTANT.value }
        .let { message ->
            Json.decodeFromString(
                deserializer = StyleQuestion.serializer(),
                string = (message.content.first() as Content.TextContent).text.value
            )
        }

    /**
     * Evaluates the responses and calculates the dominant learning style and breakdown.
     *
     * @param responses The list of learning style responses.
     * @return A Result containing the StyleResult.
     * @throws IllegalArgumentException if the responses list is empty.
     */
    override fun evaluateResponses(responses: List<Style>) =
        responses.groupingBy { it.value }
            .eachCount()
            .runCatching {
                if (responses.isEmpty()) throw IllegalArgumentException("Responses cannot be empty")
                Profile.LearningStyle(
                    dominant = maxBy { it.value }.key,
                    breakdown = Profile.LearningStyleBreakdown(
                        reading = getOrDefault(Style.READING.value, 0) * 100 / responses.size,
                        kinesthetic = getOrDefault(Style.KINESTHETIC.value, 0) * 100 / responses.size
                    )
                )
            }
}