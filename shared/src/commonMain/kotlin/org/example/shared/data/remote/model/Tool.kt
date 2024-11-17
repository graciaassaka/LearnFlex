package org.example.shared.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a tool that can be used by the assistant.
 */
@Serializable
sealed class Tool {
    companion object {
        const val CODE_INTERPRETER = "code_interpreter"
        const val FILE_SEARCH = "file_search"
        const val FUNCTION = "function"
    }

    /**
     * Represents a code interpreter tool.
     */
    @Serializable
    @SerialName(CODE_INTERPRETER)
    data object CodeInterpreterTool : Tool()

    /**
     * Represents a file search tool with optional configuration.
     */
    @Serializable
    @SerialName(FILE_SEARCH)
    data class FileSearchTool(
        @SerialName("file_search")
        val fileSearch: FileSearch? = null
    ) : Tool()

    /**
     * Represents a function tool that can be called by the assistant.
     */
    @Serializable
    @SerialName(FUNCTION)
    data class FunctionTool(
        @SerialName("function")
        val function: Function
    ) : Tool()
}

/**
 * Configuration for file search behavior.
 */
@Serializable
data class FileSearch(
    @SerialName("max_num_results")
    val maxNumResults: Int? = null,
    @SerialName("ranking_options")
    val rankingOptions: RankingOptions? = null
)

/**
 * Options for ranking file search results.
 */
@Serializable
data class RankingOptions(
    @SerialName("ranker")
    val ranker: String? = null,
    @SerialName("score_threshold")
    val scoreThreshold: Float? = null
)

/**
 * Represents a function that can be called by the assistant.
 */
@Serializable
data class Function(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("parameters")
    val parameters: Parameters? = null,
    @SerialName("strict")
    val strict: Boolean? = null
)

/**
 * Represents the parameters of a function as a JSON schema.
 */
@Serializable
data class Parameters(
    @SerialName("type")
    val type: String,
    @SerialName("properties")
    val properties: JsonObject,
    @SerialName("required")
    val required: List<String>? = null,
    @SerialName("additionalProperties")
    val additionalProperties: Boolean
)