package org.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a request to create a run.
 */
@Serializable
data class RunRequestBody(
    @SerialName("assistant_id") val assistantId: String,
    @SerialName("instructions") val instructions: String
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
    @SerialName("status") val status: RunStatus,
    @SerialName("required_action") val requiredAction: RequiredAction? = null,
    @SerialName("last_error") val lastError: LastError? = null,
    @SerialName("expires_at") val expiresAt: Int? = null,
    @SerialName("started_at") val startedAt: Int? = null,
    @SerialName("cancelled_at") val cancelledAt: Int? = null,
    @SerialName("failed_at") val failedAt: Int? = null,
    @SerialName("completed_at") val completedAt: Int? = null,
    @SerialName("incomplete_details") val incompleteDetails: IncompleteDetails? = null,
    @SerialName("model") val model: String,
    @SerialName("instructions") val instructions: String,
    @SerialName("tools") val tools: List<Tool>,
    @SerialName("metadata") val metadata: Map<String, String> = emptyMap(),
    @SerialName("usage") val usage: Usage? = null,
    @SerialName("temperature") val temperature: Float? = null,
    @SerialName("top_p") val topP: Float? = null,
    @SerialName("max_prompt_tokens") val maxPromptTokens: Int? = null,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
    @SerialName("truncation_strategy") val truncationStrategy: TruncationStrategy,
    @SerialName("tool_choice") val toolChoice: ToolChoice = ToolChoice.Auto,
    @SerialName("parallel_tool_calls") val parallelToolCalls: Boolean = false,
    @SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat.Auto
)

/**
 * Enum representing the status of a run.
 */
@Suppress("unused")
@Serializable
enum class RunStatus {
    @SerialName("queued")
    QUEUED,

    @SerialName("in_progress")
    IN_PROGRESS,

    @SerialName("requires_action")
    REQUIRES_ACTION,

    @SerialName("cancelling")
    CANCELLING,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("failed")
    FAILED,

    @SerialName("completed")
    COMPLETED,

    @SerialName("incomplete")
    INCOMPLETE,

    @SerialName("expired")
    EXPIRED
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
    @SerialName("code") val code: LastErrorCode
)

/**
 * Enum representing the error codes for the last error.
 */
@Serializable
@Suppress("unused")
enum class LastErrorCode {
    @SerialName("server_error")
    SERVER_ERROR,

    @SerialName("rate_limit_exceeded")
    RATE_LIMIT_EXCEEDED,

    @SerialName("invalid_prompt")
    INVALID_PROMPT,
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
    @SerialName("type") val type: TruncationType = TruncationType.AUTO,
    @SerialName("last_messages") val lastMessages: Int? = null
)

/**
 * Enum representing the types of truncation strategies.
 */
@Serializable
@Suppress("unused")
enum class TruncationType {
    @SerialName("auto")
    AUTO,
    @SerialName("last_messages")
    LAST_MESSAGES
}

/**
 * Sealed class representing the choice of tools.
 */
@Serializable
@Suppress("unused")
sealed class ToolChoice {

    /**
     * Represents no tool choice.
     */
    @Serializable
    @SerialName("none")
    data object None : ToolChoice()

    /**
     * Represents an automatic tool choice.
     */
    @Serializable
    @SerialName("auto")
    data object Auto : ToolChoice()

    /**
     * Represents a required tool choice.
     */
    @Serializable
    @SerialName("required")
    data object Required : ToolChoice()

    /**
     * Represents a specific tool choice with an optional function.
     */
    @Serializable
    data class SpecificTool(
        @SerialName("type") val type: String,
        @SerialName("function") val function: ToolChoiceFunction? = null
    ) : ToolChoice()
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
@Suppress("unused")
sealed class ResponseFormat {

    companion object {
        const val TYPE_JSON_OBJECT = "json_object"
        const val TYPE_JSON_SCHEMA = "json_schema"
    }

    /**
     * Represents an automatic response format.
     */
    @Serializable
    @SerialName("auto")
    data object Auto : ResponseFormat()

    /**
     * Represents a JSON object response format.
     */
    @Serializable
    data class JsonObject(
        @SerialName("type") val type: String = TYPE_JSON_OBJECT
    ) : ResponseFormat()

    /**
     * Represents a JSON schema response format.
     */
    @Serializable
    data class JsonSchema(
        @SerialName("type") val type: String = TYPE_JSON_SCHEMA,
        @SerialName("json_schema") val jsonSchema: JsonElement
    ) : ResponseFormat()
}