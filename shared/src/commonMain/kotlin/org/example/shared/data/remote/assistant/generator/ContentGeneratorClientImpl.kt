package org.example.shared.data.remote.assistant.generator

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import org.example.shared.data.remote.assistant.util.CompletionProcessor
import org.example.shared.data.remote.assistant.util.RequiredActionHandler
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.assistant.*
import org.example.shared.domain.model.assistant.Function

/**
 * Implementation of the content generator client.
 *
 * @param assistantClient The assistant client.
 * @param assistantId The ID of the assistant.
 */
class ContentGeneratorClientImpl(
    private val assistantClient: AIAssistantClient,
    private val assistantId: String
) : ContentGeneratorClient {

    /**
     * Generates content based on the provided context.
     *
     * @param context The context for content generation.
     * @return A flow emitting the result of the content generation.
     */
    override fun generateContent(context: ContentGeneratorClient.Context) = with(assistantClient) {
        flow<Result<ContentGeneratorClient.GeneratedResponse>> {
            var thread: Thread? = null

            try {
                thread = createThread(ThreadRequestBody()).getOrThrow()

                var currentRun = createRun(thread.id, context).getOrThrow()

                while (RunStatus.valueOf(currentRun.status.uppercase()).isRunActive()) {
                    if (currentRun.status == RunStatus.REQUIRES_ACTION.value) {
                        RequiredActionHandler(currentRun, thread.id, context, ::submitToolOutputs).getOrThrow()
                        delay(1000L)
                    }

                    currentRun = retrieveRun(thread.id, currentRun.id).getOrThrow()
                }

                if (currentRun.status == RunStatus.COMPLETED.value) {
                    emit(Result.success(CompletionProcessor(assistantClient, currentRun, thread.id)))
                } else {
                    emit(Result.failure(IllegalStateException("Run failed: ${currentRun.lastError?.message}")))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            } finally {
                thread?.let { t -> deleteThread(t.id).onFailure { emit(Result.failure(it)) } }
            }
        }
    }

    /**
     * Creates a new run for the given thread and context.
     *
     * @param threadId The ID of the thread.
     * @param context The context for content generation.
     * @return A result containing the created run.
     */
    private suspend fun createRun(threadId: String, context: ContentGeneratorClient.Context) = assistantClient.createRun(
        threadId = threadId,
        requestBody = RunRequestBody(
            assistantId = assistantId,
            instructions = getRunInstructions(context),
            tools = listOf(
                Tool.FunctionTool(constructFunction())
            )
        )
    )

    /**
     * Constructs the run instructions based on the provided context.
     *
     * @param context The context for content generation.
     * @return A string containing the run instructions.
     */
    private fun getRunInstructions(context: ContentGeneratorClient.Context) =
        """
            Generate ${context.type} content based on the following descriptors:
            ${context.contentDescriptors.joinToString("\n") { "- ${it.type}: ${it.title}" }}
        """.trimIndent()

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
     * Constructs the function for generating content.
     *
     * @return A [Function] object representing the content generation function.
     */
    private fun constructFunction() = Function(
        name = "get_context",
        description = "Get the curriculum context",
        strict = true,
        parameters = Parameters(
            type = "object",
            properties = buildJsonObject {
                put("context", buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("required", buildJsonArray {
                        add(JsonPrimitive("field"))
                        add(JsonPrimitive("level"))
                        add(JsonPrimitive("goal"))
                        add(JsonPrimitive("style"))
                        add(JsonPrimitive("type"))
                        add(JsonPrimitive("contentDescriptors"))
                    })
                    put("properties", buildJsonObject {
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
                        put("style", buildJsonObject {
                            put("type", JsonPrimitive("object"))
                            put("required", buildJsonArray {
                                add(JsonPrimitive("dominant"))
                                add(JsonPrimitive("breakdown"))
                            })
                            put("properties", buildJsonObject {
                                put("dominant", buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put("description", JsonPrimitive("The user's dominant style"))
                                })
                                put("breakdown", buildJsonObject {
                                    put("type", JsonPrimitive("object"))
                                    put("required", buildJsonArray {
                                        add(JsonPrimitive("reading"))
                                        add(JsonPrimitive("kinesthetic"))
                                    })
                                    put("additionalProperties", JsonPrimitive(false))
                                    put("properties", buildJsonObject {
                                        put("reading", buildJsonObject {
                                            put("type", JsonPrimitive("integer"))
                                            put("description", JsonPrimitive("The score for reading style"))
                                        })
                                        put("kinesthetic", buildJsonObject {
                                            put("type", JsonPrimitive("integer"))
                                            put("description", JsonPrimitive("The score for kinesthetic style"))
                                        })
                                    })
                                })
                            })
                            put("additionalProperties", JsonPrimitive(false))
                        })
                        put("type", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("The type of content to generate"))
                        })
                        put("contentDescriptors", buildJsonObject {
                            put("type", JsonPrimitive("array"))
                            put("description", JsonPrimitive("List of content descriptors"))
                            put("items", buildJsonObject {
                                put("type", JsonPrimitive("object"))
                                put("required", buildJsonArray {
                                    add(JsonPrimitive("type"))
                                    add(JsonPrimitive("title"))
                                    add(JsonPrimitive("description"))
                                })
                                put("properties", buildJsonObject {
                                    put("type", buildJsonObject {
                                        put("type", JsonPrimitive("string"))
                                        put("enum", buildJsonArray { ContentType.entries.forEach { add(JsonPrimitive(it.name)) } })
                                        put("description", JsonPrimitive("The type of content descriptor"))
                                    })
                                    put("title", buildJsonObject {
                                        put("type", JsonPrimitive("string"))
                                        put("description", JsonPrimitive("The content descriptor title"))
                                    })
                                    put("description", buildJsonObject {
                                        put("type", JsonPrimitive("string"))
                                        put("description", JsonPrimitive("The content descriptor description"))
                                    })
                                })
                                put("additionalProperties", JsonPrimitive(false))
                            })
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
     * Submits the tool outputs to the assistant client.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     * @param toolCallId The ID of the tool call.
     * @param output The context containing the tool outputs.
     */
    private suspend fun submitToolOutputs(
        threadId: String,
        runId: String,
        toolCallId: String,
        output: ContentGeneratorClient.Context
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
                            put("field", JsonPrimitive(output.field))
                            put("level", JsonPrimitive(output.level))
                            put("goal", JsonPrimitive(output.goal))
                            put("style", buildJsonObject {
                                put("dominant", JsonPrimitive(output.style.dominant))
                                put("breakdown", buildJsonObject {
                                    put("reading", JsonPrimitive(output.style.breakdown.reading))
                                    put("kinesthetic", JsonPrimitive(output.style.breakdown.kinesthetic))
                                })
                            })
                            put("type", JsonPrimitive(output.type))
                            put("contentDescriptors", buildJsonArray {
                                output.contentDescriptors.forEach { descriptor ->
                                    add(buildJsonObject {
                                        put("type", JsonPrimitive(descriptor.type))
                                        put("title", JsonPrimitive(descriptor.title))
                                        put("description", JsonPrimitive(descriptor.description))
                                    })
                                }
                            })
                        }
                    )
                )
            )
        )
    )
}