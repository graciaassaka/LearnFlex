package org.example.shared.data.remote.assistant.summarizer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import org.example.shared.data.remote.assistant.util.CompletionProcessor
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.SyllabusSummarizerClient
import org.example.shared.domain.model.assistant.*
import org.example.shared.domain.model.assistant.AttachmentTool.FileSearchTool
import java.io.File

/**
 * Implementation of the SyllabusSummarizerClient interface.
 *
 * @property assistantClient The AI assistant client used for communication.
 * @property assistantId The ID of the assistant to be used.
 */
class SyllabusSummarizerClientImpl(
    private val assistantClient: AIAssistantClient,
    private val assistantId: String,
) : SyllabusSummarizerClient {

    /**
     * Summarizes the given syllabus file.
     *
     * @param syllabus The file containing the syllabus to be summarized.
     * @return A flow emitting the result of the summarization.
     */
    override fun summarizeSyllabus(syllabus: File) = flow {
        var thread: Thread? = null

        try {
            val fileUploadResponse = assistantClient.uploadFile(syllabus, FilePurpose.ASSISTANTS).getOrThrow()
            thread = createThread(fileUploadResponse.id).getOrThrow()

            var run = createRun(thread.id).getOrThrow()

            while (RunStatus.valueOf(run.status.uppercase()).isRunActive()) {
                delay(1000)
                run = assistantClient.retrieveRun(thread.id, run.id).getOrThrow()
            }

            if (run.status == RunStatus.COMPLETED.value) {
                emit(Result.success(CompletionProcessor(assistantClient, run, thread.id, ::getAssistantMessage)))
            } else {
                throw IllegalStateException("Run failed: ${run.lastError?.message}")
            }
        } finally {
            thread?.let { t -> assistantClient.deleteThread(t.id).onFailure { emit(Result.failure(it)) } }
        }
    }.retry(3) {
        it is IllegalStateException
    }.catch { e ->
        emit(Result.failure(e))
    }

    /**
     * Creates a new thread for the given file ID.
     *
     * @param fileId The ID of the file to be used in the thread.
     * @return The result of the thread creation.
     */
    private suspend fun createThread(fileId: String) = assistantClient.createThread(
        ThreadRequestBody(
            messages = listOf(
                ThreadRequestMessage(
                    role = MessageRole.USER.value,
                    content = SUMMARIZE_INSTRUCTIONS,
                    attachments = listOf(
                        Attachment(
                            fileId = fileId,
                            tools = listOf(FileSearchTool)
                        )
                    )
                )
            )
        )
    )

    /**
     * Creates a new run for the given thread ID.
     *
     * @param threadId The ID of the thread to be used in the run.
     * @return The result of the run creation.
     */
    private suspend fun createRun(threadId: String) = assistantClient.createRun(
        threadId = threadId,
        requestBody = RunRequestBody(
            assistantId = assistantId,
            instructions = SUMMARIZE_INSTRUCTIONS,
            maxCompletionTokens = 600
        )
    )

    /**
     * Checks if the run status is active.
     *
     * @return True if the run is active, false otherwise.
     */
    private fun RunStatus.isRunActive(): Boolean = when (this) {
        RunStatus.QUEUED,
        RunStatus.IN_PROGRESS -> true

        else -> false
    }

    /**
     * Retrieves the assistant message from a thread.
     *
     * @param assistant The assistant client.
     * @param threadId The ID of the thread to retrieve the message from.
     * @return The assistant message.
     */
    private suspend fun getAssistantMessage(
        assistant: AIAssistantClient,
        threadId: String
    ) = assistant.listMessages(threadId, 10, MessagesOrder.DESC)
        .getOrThrow().data
        .first { it.role == MessageRole.ASSISTANT.value }
        .let { message -> (message.content.first() as Content.TextContent).text.value }

    companion object {
        private const val SUMMARIZE_INSTRUCTIONS = "Summarize the syllabus in the uploaded file."
    }
}