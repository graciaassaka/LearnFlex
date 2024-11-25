package org.example.shared.domain.client

import org.example.shared.data.remote.model.*

/**
 * Interface for AI Assistant Client operations.
 */
interface AIAssistantClient {
    /**
     * Creates a new thread.
     * @return Result containing the created Thread.
     */
    suspend fun createThread(): Result<Thread>

    /**
     * Retrieves a thread by its ID.
     * @param threadId The ID of the thread to retrieve.
     * @return Result containing the retrieved Thread.
     */
    suspend fun retrieveThread(threadId: String): Result<Thread>

    /**
     * Deletes a thread by its ID.
     * @param threadId The ID of the thread to delete.
     * @return Result indicating the success or failure of the operation.
     */
    suspend fun deleteThread(threadId: String): Result<Unit>

    /**
     * Creates a new message in a thread.
     * @param threadId The ID of the thread to add the message to.
     * @param requestBody The request containing the message content and role.
     * @return Result containing the created Message.
     */
    suspend fun createMessage(threadId: String, requestBody: MessageRequestBody): Result<Message>

    /**
     * Lists messages in a thread.
     * @param threadId The ID of the thread to list messages from.
     * @param limit Optional limit on the number of messages to retrieve.
     * @param order The order in which to list the messages (default is DESC).
     * @return Result containing the list of messages.
     */
    suspend fun listMessages(
        threadId: String,
        limit: Int = 20,
        order: MessagesOrder = MessagesOrder.DESC
    ): Result<ListMessagesResponse>

    /**
     * Creates a new run in a thread.
     * @param threadId The ID of the thread to create the run in.
     * @param requestBody The request containing the run instructions.
     * @return Result containing the created Run.
     */
    suspend fun createRun(threadId: String, requestBody: RunRequestBody): Result<Run>

    /**
     * Retrieves a run by its ID.
     * @param threadId The ID of the thread containing the run.
     * @param runId The ID of the run to retrieve.
     * @return Result containing the retrieved Run.
     */
    suspend fun retrieveRun(threadId: String, runId: String): Result<Run>

    /**
     * Cancels a run by its ID.
     * @param threadId The ID of the thread containing the run.
     * @param runId The ID of the run to be canceled.
     * @return Result indicating the success or failure of the operation.
     */
    suspend fun cancelRun(threadId: String, runId: String): Result<Unit>

    /**
     * Submits tool output for a run.
     * @param threadId The ID of the thread containing the run.
     * @param runId The ID of the run to submit the tool output for.
     * @param requestBody The request containing the tool output.
     * @return Result containing the updated Run.
     */
    suspend fun submitToolOutput(
        threadId: String,
        runId: String,
        requestBody: SubmitToolOutputsRequestBody
    ): Result<Run>
}