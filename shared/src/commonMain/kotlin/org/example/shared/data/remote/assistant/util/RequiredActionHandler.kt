package org.example.shared.data.remote.assistant.util

import org.example.shared.domain.model.assistant.RequiredActionType
import org.example.shared.domain.model.assistant.Run

/**
 * Handles required actions for a run.
 */
object RequiredActionHandler {

    /**
     * Handles the required action for a run.
     *
     * @param run The run containing the required action.
     * @param threadId The ID of the thread associated with the run.
     * @param output The output to submit.
     * @param submitToolOutput The function to submit tool outputs.
     * @return The result of the operation.
     * @throws IllegalStateException If no required action is found.
     */
    suspend operator fun <T> invoke(
        run: Run,
        threadId: String,
        output: T,
        submitToolOutput: suspend (threadId: String, runId: String, toolCallId: String, output: T) -> Result<Run>,
    ) = run.runCatching {
        requiredAction?.let { action ->
            when (RequiredActionType.valueOf(action.type.uppercase())) {
                RequiredActionType.SUBMIT_TOOL_OUTPUTS -> {
                    requiredAction.submitToolOutputs?.toolCalls
                        ?.map { call -> submitToolOutput(threadId, id, call.id, output) }
                        ?.forEach { it.getOrThrow() }
                        ?: throw IllegalStateException("No tool calls found")
                }
            }
        } ?: throw IllegalStateException("No required action found")
    }
}