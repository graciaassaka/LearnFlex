package org.example.shared.data.remote.assistant.generator

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*
import org.example.shared.data.remote.assistant.util.CompletionProcessor
import org.example.shared.data.remote.assistant.util.RequiredActionHandler
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.QuestionGeneratorClient
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Question
import org.example.shared.domain.model.assistant.*
import org.example.shared.domain.model.assistant.Function

/**
 * Implementation of the QuestionGeneratorClient interface.
 * This class generates questions using an AI assistant client.
 *
 * @param T The type of Question.
 * @property assistantClient The AI assistant client.
 * @property assistantId The ID of the assistant.
 * @property serializer The serializer for the Question type.
 */
class QuestionGeneratorClientImpl<T : Question>(
    private val assistantClient: AIAssistantClient,
    private val assistantId: String,
    private val serializer: KSerializer<T>
) : QuestionGeneratorClient<T> {

    /**
     * Generates a specified number of questions.
     *
     * @param context The context for generating questions.
     * @param number The number of questions to generate.
     * @return A flow emitting the results of the question generation.
     */
    override fun generateQuestion(context: QuestionGeneratorClient.Context, number: Int) =
        with(assistantClient) {
            flow {
                var count = 0
                while (count < number) {
                    emit(Result.success(processRun(context, 0)))
                    count++
                }
            }.catch { e ->
                emit(Result.failure(e))
            }
        }

    /**
     * Processes a run to generate a question.
     *
     * @param context The context for generating the question.
     * @param attempt The current attempt number.
     * @return The generated question.
     */
    private suspend fun processRun(context: QuestionGeneratorClient.Context, attempt: Int): T =
        with(assistantClient) {
            var thread: Thread? = null

            try {
                thread = createThread(ThreadRequestBody()).getOrThrow()
                var run = createRun(thread.id).getOrThrow()

                while (RunStatus.valueOf(run.status.uppercase()).isRunActive()) {
                    if (run.status == RunStatus.REQUIRES_ACTION.value) {
                        RequiredActionHandler(run, thread.id, context, ::submitToolOutputs).getOrThrow()
                        delay(1000L)
                    }
                    run = retrieveRun(thread.id, run.id).getOrThrow()
                }
                if (run.status != RunStatus.COMPLETED.value) throw IllegalStateException("Run failed: ${run.lastError?.message}")
                CompletionProcessor(this, run, thread.id, ::getAssistantMessage)
            } catch (e: Exception) {
                if (attempt >= 3) throw e
                delay(1000L * attempt)
                processRun(context, attempt + 1)
            } finally {
                thread?.let { t -> deleteThread(t.id).getOrThrow() }
            }
        }

    /**
     * Creates a run for generating a question.
     *
     * @param threadId The ID of the thread.
     * @return The created run.
     */
    private suspend fun createRun(threadId: String) = assistantClient.createRun(
        threadId = threadId,
        requestBody = RunRequestBody(
            assistantId = assistantId,
            instructions = "Generate a question",
            tools = listOf(Tool.FunctionTool(constructFunction()))
        )
    )

    /**
     * Constructs the function for getting the context of the question.
     *
     * @return The constructed function.
     */
    private fun constructFunction() = Function(
        name = "get_context",
        description = "Get the context for the question",
        strict = true,
        parameters = Parameters(
            type = "object",
            properties = buildJsonObject {
                put("context", buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("required", buildJsonArray {
                        add(JsonPrimitive("topic"))
                        add(JsonPrimitive("level"))
                    })
                    put("properties", buildJsonObject {
                        put("topic", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("The topic of the question"))
                        })
                        put("level", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("The level of the question"))
                            put("enum", buildJsonArray { Level.entries.forEach { add(JsonPrimitive(it.name)) } })
                        })
                    })
                    put("additionalProperties", JsonPrimitive(false))
                })
            },
            required = listOf("context"),
            additionalProperties = false
        )
    )

    /**
     * Submits the tool outputs.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     * @param toolCallId The ID of the tool call.
     * @param output The context for generating the question.
     */
    private suspend fun submitToolOutputs(
        threadId: String, runId: String, toolCallId: String, output: QuestionGeneratorClient.Context
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
                            put("context", buildJsonObject {
                                put("topic", JsonPrimitive(output.topic))
                                put("level", JsonPrimitive(output.level.name))
                            })
                        }
                    )
                )
            )
        )
    )

    /**
     * Checks if the run status is active.
     *
     * @return True if the run status is active, false otherwise.
     */
    private fun RunStatus.isRunActive(): Boolean = when (this) {
        RunStatus.QUEUED, RunStatus.IN_PROGRESS, RunStatus.REQUIRES_ACTION -> true
        else -> false
    }

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
                deserializer = serializer,
                string = (message.content.first() as Content.TextContent).text.value
            )
        }
}