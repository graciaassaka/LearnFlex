package org.example.shared.domain.model.assistant

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.example.shared.domain.constant.interfaces.ValuableEnum
import org.example.shared.domain.model.assistant.ResponseFormat.ResponseFormatSerializer

/**
 * Represents a request to create a run.
 */
@Serializable
data class RunRequestBody(
    @SerialName("assistant_id")
    val assistantId: String,

    @SerialName("instructions")
    val instructions: String,

    @SerialName("tools")
    val tools: List<Tool> = emptyList(),

    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null
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
    @SerialName("tool_choice") @Serializable(with = ToolChoiceSerializer::class) val toolChoice: ToolChoice = ToolChoice.Auto,
    @SerialName("parallel_tool_calls") val parallelToolCalls: Boolean = false,
    @SerialName("response_format") @Serializable(with = ResponseFormatSerializer::class) val responseFormat: ResponseFormat = ResponseFormat.Auto
)

@Serializable(with = ToolChoiceSerializer::class)
sealed class ToolChoice {
    /** The model calls no tools. */
    object None : ToolChoice()

    /** The model chooses on its own whether to call tools or not. */
    object Auto : ToolChoice()

    /** The model is required to call one or more tools. */
    object Required : ToolChoice()

    /**
     * The model is forced to use a particular tool:
     * e.g., {"type": "file_search"} or {"type": "function", "function": {...}}
     */
    data class ForcedTool(
        val type: String
    ) : ToolChoice()
}

/**
 * Represents the `function` field if `type = "function"`.
 */
@Suppress("unused")
@Serializable
data class FunctionSpec(
    val name: String
)

/**
 * Custom serializer for [ToolChoice] to handle both string and object cases
 */
object ToolChoiceSerializer : KSerializer<ToolChoice> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("ToolChoice")

    override fun serialize(encoder: Encoder, value: ToolChoice) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("ToolChoice only supports Json encoding.")

        val json = when (value) {
            is ToolChoice.None -> JsonPrimitive("none")
            is ToolChoice.Auto -> JsonPrimitive("auto")
            is ToolChoice.Required -> JsonPrimitive("required")
            is ToolChoice.ForcedTool -> {
                buildJsonObject {
                    put("type", value.type)
                }
            }
        }

        output.encodeJsonElement(json)
    }

    override fun deserialize(decoder: Decoder): ToolChoice {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("ToolChoice only supports Json decoding.")

        val element = input.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> {
                val value = element.content.lowercase()
                when (value) {
                    "none" -> ToolChoice.None
                    "auto" -> ToolChoice.Auto
                    "required" -> ToolChoice.Required
                    else -> throw SerializationException("Unknown tool_choice string: $value")
                }
            }

            is JsonObject -> {
                val type = element["type"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Object is missing 'type' field.")

                ToolChoice.ForcedTool(type)
            }

            else -> {
                throw SerializationException("tool_choice must be string or object.")
            }
        }
    }
}


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

@Serializable
sealed interface ResponseFormat {
    companion object {
        const val TYPE_AUTO = "auto"
        const val TYPE_JSON_OBJECT = "json_object"
        const val TYPE_JSON_SCHEMA = "json_schema"
        const val TYPE_TEXT = "text"
    }

    val type: String
        get() = when (this) {
            is Auto -> TYPE_AUTO
            is Text -> TYPE_TEXT
            is JsonObjectFormat -> TYPE_JSON_OBJECT
            is JsonSchemaFormat -> TYPE_JSON_SCHEMA
        }

    @Serializable
    object Auto : ResponseFormat

    @Serializable
    object Text : ResponseFormat

    @Serializable
    object JsonObjectFormat : ResponseFormat

    @Serializable
    data class JsonSchemaFormat(
        @SerialName("json_schema") val jsonSchema: JsonSchema = JsonSchema()
    ) : ResponseFormat

    @OptIn(ExperimentalSerializationApi::class)
    object ResponseFormatSerializer : KSerializer<ResponseFormat> {
        @OptIn(InternalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor("ResponseFormat", SerialKind.CONTEXTUAL) {
            element("type", String.serializer().descriptor)
            element("json_schema", JsonSchema.serializer().descriptor, isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: ResponseFormat) {
            val composite = encoder.beginStructure(descriptor)
            when (value) {
                is Auto -> composite.encodeStringElement(descriptor, 0, TYPE_AUTO)
                is Text -> composite.encodeStringElement(descriptor, 0, TYPE_TEXT)
                is JsonObjectFormat -> composite.encodeStringElement(descriptor, 0, TYPE_JSON_OBJECT)
                is JsonSchemaFormat -> {
                    composite.encodeStringElement(descriptor, 0, TYPE_JSON_SCHEMA)
                    composite.encodeSerializableElement(descriptor, 1, JsonSchema.serializer(), value.jsonSchema)
                }
            }
            composite.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): ResponseFormat {
            val input = decoder.decodeSerializableValue(JsonElement.serializer())
            return when {
                input is JsonObject && input["type"]?.jsonPrimitive?.content == TYPE_AUTO -> Auto
                input is JsonObject && input["type"]?.jsonPrimitive?.content == TYPE_TEXT -> Text
                input is JsonObject && input["type"]?.jsonPrimitive?.content == TYPE_JSON_OBJECT -> JsonObjectFormat
                input is JsonObject && input["type"]?.jsonPrimitive?.content == TYPE_JSON_SCHEMA -> {
                    val jsonSchema = input["json_schema"]?.let {
                        Json.decodeFromJsonElement(JsonSchema.serializer(), it)
                    } ?: throw SerializationException("JsonSchema is missing.")
                    JsonSchemaFormat(jsonSchema = jsonSchema)
                }

                else -> throw SerializationException("Unknown ResponseFormat format: $input")
            }
        }
    }
}

/**
 * Represents the JSON schema for the response format.
 */
@Serializable
@SerialName("json_schema")
data class JsonSchema(
    @SerialName("description") val description: String? = null,
    @SerialName("name") val name: String = "",
    @SerialName("schema") val schema: JsonObject = JsonObject(emptyMap()),
    @SerialName("strict") val strict: Boolean = false
)