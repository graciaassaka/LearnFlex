package org.example.shared.data.remote.assistant.util

import kotlinx.serialization.json.Json
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.model.assistant.*

/**
 * Processes the completion of a run.
 */
object CompletionProcessor {

    /**
     * Processes the completion of a run.
     *
     * @param assistant The assistant client.
     * @param run The run to process.
     * @param threadId The ID of the thread associated with the run.
     * @return The result of the completion.
     * @throws IllegalStateException If the run is in an unexpected state.
     * @throws IllegalStateException If the run failed.
     * @throws IllegalStateException If the run was cancelled.
     * @throws IllegalStateException If the run is incomplete.
     * @throws IllegalStateException If the run expired.
     */
    suspend inline operator fun <reified T> invoke(assistant: AIAssistantClient, run: Run, threadId: String): T = with(run) {
        when (RunStatus.valueOf(status.uppercase())) {
            RunStatus.COMPLETED -> getAssistantMessage(assistant, threadId)
            RunStatus.FAILED -> throw IllegalStateException("Run failed: ${lastError?.message}")
            RunStatus.CANCELLED -> throw IllegalStateException("Run cancelled: ${lastError?.message}")
            RunStatus.INCOMPLETE -> throw IllegalStateException("Run incomplete: ${incompleteDetails?.reason}")
            RunStatus.EXPIRED -> throw IllegalStateException("Run expired: ${lastError?.message}")
            else -> throw IllegalStateException("Unexpected status: $status")
        }
    }

    /**
     * Retrieves the assistant message from a thread.
     *
     * @param assistant The assistant client.
     * @param threadId The ID of the thread to retrieve the message from.
     * @return The assistant message.
     */
    suspend inline fun <reified T> getAssistantMessage(assistant: AIAssistantClient, threadId: String): T = assistant
        .listMessages(threadId, 10, MessagesOrder.DESC)
        .getOrThrow().data
        .first { it.role == MessageRole.ASSISTANT.value }
        .let { message ->
            val textValue = (message.content.first() as Content.TextContent).text.value
            if (T::class == String::class) textValue as T else Json.decodeFromString<T>(textValue)
        }
}