package org.example.shared.data.remote.assistant.util

import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.model.assistant.Run
import org.example.shared.domain.model.assistant.RunStatus

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
     * @param getAssistantMessage The function to get the assistant message.
     * @return The result of the completion.
     * @throws IllegalStateException If the run is in an unexpected state.
     * @throws IllegalStateException If the run failed.
     * @throws IllegalStateException If the run was cancelled.
     * @throws IllegalStateException If the run is incomplete.
     * @throws IllegalStateException If the run expired.
     */
    suspend operator fun <T> invoke(
        assistant: AIAssistantClient,
        run: Run,
        threadId: String,
        getAssistantMessage: suspend (AIAssistantClient, String) -> T,
    ): T = with(run) {
        when (RunStatus.valueOf(status.uppercase())) {
            RunStatus.COMPLETED -> getAssistantMessage(assistant, threadId)
            RunStatus.FAILED -> throw IllegalStateException("Run failed: ${lastError?.message}")
            RunStatus.CANCELLED -> throw IllegalStateException("Run cancelled: ${lastError?.message}")
            RunStatus.INCOMPLETE -> throw IllegalStateException("Run incomplete: ${incompleteDetails?.reason}")
            RunStatus.EXPIRED -> throw IllegalStateException("Run expired: ${lastError?.message}")
            else -> throw IllegalStateException("Unexpected status: $status")
        }
    }
}