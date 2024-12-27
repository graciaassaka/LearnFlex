package org.example.shared.domain.model.assistant

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Represents a request to create a run.
 */
@Serializable
data class RunRequestBody(
    @SerialName("assistant_id") val assistantId: String,
    @SerialName("instructions") val instructions: String,
    @SerialName("tools") val tools: List<Tool> = emptyList(),
)

/**
 * Represents a run with various attributes and metadata.
 */
@Serializable
data class Run(
    @SerialName("id") val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("created_at") val createdAt: Int,
    @SerialName("thread_id") val threadId: String,
    @SerialName("assistant_id") val assistantId: String,
    @SerialName("status") val status: String,
    @SerialName("required_action") val requiredAction: RequiredAction? = null,
    @SerialName("last_error") val lastError: LastError? = null,
    @SerialName("expires_at") val expiresAt: Int? = null,
    @SerialName("started_at") val startedAt: Int? = null,
    @SerialName("cancelled_at") val cancelledAt: Int? = null,
    @SerialName("failed_at") val failedAt: Int? = null,
    @SerialName("completed_at") val completedAt: Int? = null,
    @SerialName("incomplete_details") val incompleteDetails: IncompleteRunDetails? = null,
    @SerialName("model") val model: String,
    @SerialName("instructions") val instructions: String? = null,
    @SerialName("additional_instructions") val additionalInstructions: String? = null,
    @SerialName("tools") val tools: List<Tool>,
    @SerialName("metadata") val metadata: Map<String, String> = emptyMap(),
    @SerialName("usage") val usage: Usage? = null,
    @SerialName("temperature") val temperature: Float? = null,
    @SerialName("top_p") val topP: Float? = null,
    @SerialName("max_prompt_tokens") val maxPromptTokens: Int? = null,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
    @SerialName("truncation_strategy") val truncationStrategy: TruncationStrategy,
    @SerialName("tool_choice") val toolChoice: String = ToolChoice.Auto.value,
    @SerialName("parallel_tool_calls") val parallelToolCalls: Boolean = false,
    @SerialName("response_format")
    @Serializable(with = ResponseFormat.ResponseFormatSerializer::class)
    val responseFormat: ResponseFormat = ResponseFormat.Auto
)

/**
 * Enum representing the status of a run.
 */
@Suppress("unused")
enum class RunStatus(override val value: String) : ValuableEnum<String> {
    QUEUED("queued"),
    IN_PROGRESS("in_progress"),
    REQUIRES_ACTION("requires_action"),
    CANCELLING("cancelling"),
    CANCELLED("cancelled"),
    FAILED("failed"),
    COMPLETED("completed"),
    INCOMPLETE("incomplete"),
    EXPIRED("expired")
}

/**
 * Represents an action required to proceed with the run.
 */
@Serializable
@SerialName("required_action")
data class RequiredAction(
    @SerialName("type") val type: String,
    @SerialName("submit_tool_outputs") val submitToolOutputs: SubmitToolOutputs? = null,
)

enum class RequiredActionType(val value: String) {
    SUBMIT_TOOL_OUTPUTS("submit_tool_outputs")
}

/**
 * Details about tool outputs that need to be submitted.
 */
@Serializable
@SerialName("submit_tool_outputs")
data class SubmitToolOutputs(
    @SerialName("tool_calls") val toolCalls: List<ToolCall>
)

/**
 * Represents a specific tool call that needs to be made.
 */
@Serializable
data class ToolCall(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String = Tool.FUNCTION,
    @SerialName("function") val function: ToolCallFunction
)

/**
 * Function details for a tool call.
 */
@Serializable
@SerialName("function")
data class ToolCallFunction(
    @SerialName("name") val name: String,
    @SerialName("arguments") val arguments: String
)

/**
 * Represents details about an incomplete run.
 */
@Serializable
@SerialName("incomplete_details")
data class IncompleteRunDetails(
    val reason: String,
)

/**
 * Represents a request body for submitting tool outputs.
 */
@Serializable
data class SubmitToolOutputsRequestBody(
    @SerialName("tool_outputs") val toolOutputs: List<ToolOutput>,
    @SerialName("stream") val stream: Boolean? = null
)

/**
 * Represents the output of a specific tool call.
 */
@Serializable
data class ToolOutput(
    @SerialName("tool_call_id") val toolCallId: String,
    @SerialName("output") val output: String
)

/**
 * Represents the last error that occurred during the run.
 */
@Serializable
@SerialName("last_error")
data class LastError(
    @SerialName("message") val message: String,
    @SerialName("code") val code: String
)

/**
 * Enum representing the error codes for the last error.
 */
@Suppress("unused")
enum class LastErrorCode(val value: String) {
    SERVER_ERROR("server_error"),
    RATE_LIMIT_EXCEEDED("rate_limit_exceeded"),
    INVALID_PROMPT("invalid_prompt"),
}

/**
 * Represents the usage details of the run.
 */
@Serializable
@SerialName("usage")
data class Usage(
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

/**
 * Represents the truncation strategy for the run.
 */
@Serializable
@SerialName("truncation_strategy")
data class TruncationStrategy(
    @SerialName("type") val type: String = TruncationType.AUTO.value,
    @SerialName("last_messages") val lastMessages: Int? = null
)

/**
 * Enum representing the types of truncation strategies.
 */
@Serializable
@Suppress("unused")
enum class TruncationType(val value: String) {
    AUTO("auto"),
    LAST_MESSAGES("last_messages")
}

@Suppress("unused")
enum class ToolChoice(val value: String) {
    None("none"),
    Auto("auto"),
    Required("required")
}

/**
 * Represents a function associated with a tool choice.
 */
@Serializable
data class ToolChoiceFunction(
    @SerialName("name") val name: String
)

/**
 * Sealed class representing the response format.
 */
@Serializable
sealed interface ResponseFormat {
    companion object {
        const val TYPE_JSON_OBJECT = "json_object"
        const val TYPE_JSON_SCHEMA = "json_schema"
    }

    /**
     * Custom serializer for ResponseFormat to handle both string and object cases
     */
    @OptIn(ExperimentalSerializationApi::class)
    object ResponseFormatSerializer : KSerializer<ResponseFormat> {
        @OptIn(InternalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor("ResponseFormat", SerialKind.CONTEXTUAL) {
            element("type", String.serializer().descriptor)
            element("json_schema", JsonElement.serializer().descriptor, isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: ResponseFormat) {
            when (value) {
                is Auto -> encoder.encodeString("auto")
                is JsonObjectFormat -> {
                    val composite = encoder.beginStructure(descriptor)
                    composite.encodeStringElement(descriptor, 0, TYPE_JSON_OBJECT)
                    composite.endStructure(descriptor)
                }

                is JsonSchemaFormat -> {
                    val composite = encoder.beginStructure(descriptor)
                    composite.encodeStringElement(descriptor, 0, TYPE_JSON_SCHEMA)
                    composite.encodeSerializableElement(descriptor, 1, JsonElement.serializer(), value.jsonSchema)
                    composite.endStructure(descriptor)
                }
            }
        }

        override fun deserialize(decoder: Decoder): ResponseFormat {
            val input = decoder.decodeSerializableValue(JsonElement.serializer())
            return when {
                input is JsonPrimitive && input.isString && input.content == "auto" -> Auto
                input is JsonObject && input["type"]?.jsonPrimitive?.content == TYPE_JSON_OBJECT ->
                    JsonObjectFormat()

                input is JsonObject && input["type"]?.jsonPrimitive?.content == TYPE_JSON_SCHEMA ->
                    JsonSchemaFormat(jsonSchema = input["json_schema"] ?: JsonObject(emptyMap()))

                else -> throw SerializationException("Unknown ResponseFormat format")
            }
        }
    }

    @Serializable
    data object Auto : ResponseFormat

    @Serializable
    data class JsonObjectFormat(
        @SerialName("type") val type: String = TYPE_JSON_OBJECT
    ) : ResponseFormat

    @Serializable
    data class JsonSchemaFormat(
        @SerialName("type") val type: String = TYPE_JSON_SCHEMA,
        @SerialName("json_schema") val jsonSchema: JsonElement
    ) : ResponseFormat
}