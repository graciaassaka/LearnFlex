package org.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a thread.
 */
@Serializable
@SerialName("thread")
data class Thread(
    @SerialName("id") val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("created_at") val createdAt: Int,
    @SerialName("metadata") val metadata: Map<String, String> = emptyMap(),
    @SerialName("tool_resources") val toolResources: ToolResourceWrapper? = null
)

@Serializable
data class ToolResourceWrapper(
    @SerialName("code_interpreter") val codeInterpreter: ToolResources.CodeInterpreter? = null,
    @SerialName("file_search") val fileSearch: ToolResources.FileSearch? = null
)

/**
 * Sealed class representing tool resources available to the assistant.
 */
@Serializable
@SerialName("tool_resources")
@Suppress("unused")
sealed class ToolResources {
    /**
     * Code interpreter tool resources.
     */
    @Serializable
    @SerialName("code_interpreter")
    data class CodeInterpreter(
        @SerialName("file_ids") val fileIds: List<String>
    ) : ToolResources()

    /**
     * File search tool resources.
     */
    @Serializable
    @SerialName("file_search")
    data class FileSearch(
        @SerialName("vector_store_ids") val vectorStoreIds: List<String>
    ) : ToolResources()
}