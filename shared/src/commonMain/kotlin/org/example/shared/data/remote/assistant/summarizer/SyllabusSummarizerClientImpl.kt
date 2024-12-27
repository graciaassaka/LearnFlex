package org.example.shared.data.remote.assistant.summarizer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.example.shared.data.remote.assistant.util.CompletionProcessor
import org.example.shared.domain.client.AIAssistantClient
import org.example.shared.domain.client.SyllabusSummarizerClient
import org.example.shared.domain.model.assistant.*
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
    override fun summarizeSyllabus(syllabus: File) = flow<Result<String>> {
        var thread: Thread? = null

        try {
            val fileUploadResponse = assistantClient.uploadFile(syllabus, FilePurpose.ASSISTANTS).getOrThrow()
            thread = createThread(fileUploadResponse.id).getOrThrow()

            val run = createRun(thread.id).getOrThrow()

            while (RunStatus.valueOf(run.status.uppercase()).isRunActive()) delay(1000)

            if (run.status == RunStatus.COMPLETED.value) {
                emit(Result.success(CompletionProcessor(assistantClient, run, thread.id)))
            } else {
                emit(Result.failure(IllegalStateException("Run failed: ${run.lastError?.message}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        } finally {
            thread?.let { t -> assistantClient.deleteThread(t.id).onFailure { emit(Result.failure(it)) } }
        }
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
                            tools = listOf(AttachmentTool.FileSearchTool)
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
            instructions = SUMMARIZE_INSTRUCTIONS
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

    companion object {
        private const val SUMMARIZE_INSTRUCTIONS = "Summarize the syllabus in the uploaded file."
    }
}