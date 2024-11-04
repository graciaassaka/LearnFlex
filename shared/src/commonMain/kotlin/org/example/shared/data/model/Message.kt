package org.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a request to create a message.
 */
@Serializable
data class MessageRequestBody(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

/**
 * Represents the response containing a list of messages from a thread.
 */
@Serializable
data class ListMessagesResponse(
    @SerialName("object") val objectType: String,
    @SerialName("data") val data: List<Message>,
    @SerialName("first_id") val firstId: String? = null,
    @SerialName("last_id") val lastId: String? = null,
    @SerialName("has_more") val hasMore: Boolean
)

/**
 * Represents a message with various attributes.
 */
@Serializable
@SerialName("message")
data class Message(
    @SerialName("id") val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("created_at") val createdAt: Int,
    @SerialName("thread_id") val threadId: String,
    @SerialName("status") val status: MessageStatus? = null,
    @SerialName("incomplete_details") val incompleteDetails: IncompleteDetails? = null,
    @SerialName("completed_at") val completedAt: Int? = null,
    @SerialName("incomplete_at") val incompleteAt: Int? = null,
    @SerialName("role") val role: String,
    @SerialName("content") val content: List<Content>,
    @SerialName("assistant_id") val assistantId: String? = null,
    @SerialName("run_id") val runId: String? = null,
    @SerialName("attachments") val attachments: List<Attachment>? = null,
    @SerialName("metadata") val metadata: Map<String, String> = emptyMap()
)

/**
 * Represents the status of a message.
 */
@Serializable
@Suppress("unused")
enum class MessageStatus
{
    @SerialName("in_progress")
    IN_PROGRESS,
    @SerialName("incomplete")
    INCOMPLETE,
    @SerialName("completed")
    COMPLETED
}

/**
 * Represents the role of a message.
 */
@Suppress("unused")
enum class MessageRole(val value: String)
{
    USER("user"),
    ASSISTANT("assistant")
}

/**
 * Represents the content of a message.
 */
@Serializable
@Suppress("unused")
sealed class Content
{
    companion object
    {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE_FILE = "image_file"
        const val TYPE_IMAGE_URL = "image_url"
        const val TYPE_REFUSAL = "refusal"
    }

    /**
     * Represents text content.
     */
    @Serializable
    @SerialName("text")
    data class TextContent(
        @SerialName("type") val type: String = TYPE_TEXT,
        @SerialName("text") val text: Text,
    ) : Content()

    /**
     * Represents image file content.
     */
    @Serializable
    @SerialName("image_file")
    data class ImageFileContent(
        @SerialName("type") val type: String = TYPE_IMAGE_FILE,
        @SerialName("image_file") val imageFile: ImageFile,
    ) : Content()

    /**
     * Represents image URL content.
     */
    @Serializable
    @SerialName("image_url")
    data class ImageUrlContent(
        @SerialName("type") val type: String = TYPE_IMAGE_URL,
        @SerialName("image_url") val imageUrl: ImageUrl,
    ) : Content()

    /**
     * Represents refusal content.
     */
    @Serializable
    @SerialName("refusal")
    data class RefusalContent(
        @SerialName("type") val type: String = TYPE_REFUSAL,
        @SerialName("refusal") val refusal: String
    ) : Content()
}

/**
 * Represents text data.
 */
@Serializable
@SerialName("text")
data class Text(
    @SerialName("value") val value: String,
    @SerialName("annotations") val textAnnotations: List<TextAnnotation> = emptyList()
)

/**
 * Represents an annotation.
 */
@Serializable
@Suppress("unused")
sealed class TextAnnotation
{
    /**
     * Represents a text file citation annotation.
     */
    @Serializable
    data class TextFileCitation(
        @SerialName("type") val type: String,
        @SerialName("text") val text: String,
        @SerialName("file_citation") val fileCitation: FileCitation,
        @SerialName("start_index") val startIndex: Int,
        @SerialName("end_index") val endIndex: Int,
    ) : TextAnnotation()

    /**
     * Represents a text file path annotation.
     */
    @Serializable
    data class TextFilePath(
        @SerialName("type") val type: String,
        @SerialName("text") val text: String,
        @SerialName("file_path") val filePath: FilePath,
        @SerialName("start_index") val startIndex: Int,
        @SerialName("end_index") val endIndex: Int
    ) : TextAnnotation()
}

/**
 * Represents a file path.
 */
@Serializable
@SerialName("file_path")
class FilePath(
    @SerialName("file_id") val fileId: String,
)

/**
 * Represents a file citation.
 */
@Serializable
@SerialName("file_citation")
data class FileCitation(
    @SerialName("file_id") val fileId: String,
)

/**
 * Represents an image file.
 */
@Serializable
@SerialName("image_file")
data class ImageFile(
    @SerialName("file_id") val fileId: String,
    @SerialName("detail") val detail: String
)

/**
 * Represents an image URL.
 */
@Serializable
@SerialName("image_url")
data class ImageUrl(
    @SerialName("url") val url: String,
    @SerialName("detail") val detail: String = "auto"
)

/**
 * Represents an attachment.
 */
@Serializable
data class Attachment(
    @SerialName("file_id") val fileId: String,
    @SerialName("tools") val tools: List<Tool>,
)

/**
 * Represents a tool.
 */
@Suppress("unused")
enum class MessagesOrder(val value: String)
{
    ASC("asc"),
    DESC("desc")
}
