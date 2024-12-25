package org.example.shared.data.remote.assistant

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.example.shared.data.remote.model.*
import org.example.shared.data.remote.util.HttpResponseHandler
import org.example.shared.domain.client.AIAssistantClient
import java.io.File

/**
 * Client for interacting with the OpenAI Assistant API.
 *
 * @property apiKey The API key for authenticating requests.
 * @property httpClient The HTTP client used for making requests.
 */
class OpenAIAssistantClient(
    private val httpClient: HttpClient,
    private val baseUrl: Url,
    private val apiKey: String
) : AIAssistantClient {
    /**
     * Creates a new thread.
     *
     * @param threadRequestBody The request body containing the details for creating the thread.
     * @return A [Result] containing the created [Thread].
     */
    override suspend fun createThread(threadRequestBody: ThreadRequestBody) = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads")
            setBody(threadRequestBody)
        }.run {
            HttpResponseHandler<Thread>(this).invoke { body<Thread>() }
        }
    }

    /**
     * Retrieves a thread by its ID.
     *
     * @param threadId The ID of the thread to retrieve.
     * @return A [Result] containing the retrieved [Thread].
     */
    override suspend fun retrieveThread(threadId: String) = runCatching {
        httpClient.get {
            setUpAssistantRequest("/v1/threads/$threadId")
        }.run {
            HttpResponseHandler<Thread>(this).invoke { body<Thread>() }
        }
    }

    /**
     * Deletes a thread by its ID.
     *
     * @param threadId The ID of the thread to delete.
     * @return A [Result] indicating the success or failure of the operation.
     */
    override suspend fun deleteThread(threadId: String) = runCatching {
        httpClient.delete {
            setUpAssistantRequest("/v1/threads/$threadId")
        }.run {
            HttpResponseHandler<Unit>(this).invoke { }
        }
    }

    /**
     * Creates a new message in a thread.
     *
     * @param threadId The ID of the thread.
     * @param requestBody The request body containing the message details.
     * @return A [Result] containing the created [Message].
     */
    override suspend fun createMessage(threadId: String, requestBody: MessageRequestBody) = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads/$threadId/messages")
            setBody(requestBody)
        }.run {
            HttpResponseHandler<Message>(this).invoke { body<Message>() }
        }
    }

    /**
     * Lists messages in a thread.
     *
     * @param threadId The ID of the thread.
     * @param limit The maximum number of messages to retrieve.
     * @param order The order in which to retrieve the messages.
     * @return A [Result] containing the list of messages.
     */
    override suspend fun listMessages(threadId: String, limit: Int, order: MessagesOrder) = runCatching {
        httpClient.get {
            setUpAssistantRequest("/v1/threads/$threadId/messages")
            parameter("limit", limit)
            parameter("order", order.value)
        }.run {
            HttpResponseHandler<ListMessagesResponse>(this).invoke { body<ListMessagesResponse>() }
        }
    }

    /**
     * Creates a new run in a thread.
     *
     * @param threadId The ID of the thread.
     * @param requestBody The request body containing the run details.
     * @return A [Result] containing the created [Run].
     */
    override suspend fun createRun(threadId: String, requestBody: RunRequestBody) = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads/$threadId/runs")
            setBody(requestBody)
        }.run {
            HttpResponseHandler<Run>(this).invoke { body<Run>() }
        }
    }

    /**
     * Retrieves a run by its ID.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run to retrieve.
     * @return A [Result] containing the retrieved [Run].
     */
    override suspend fun retrieveRun(threadId: String, runId: String) = runCatching {
        httpClient.get {
            setUpAssistantRequest("/v1/threads/$threadId/runs/$runId")
        }.run {
            HttpResponseHandler<Run>(this).invoke { body<Run>() }
        }
    }

    /**
     * Cancels a run by its ID.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run to cancel.
     * @return A [Result] indicating the success or failure of the operation.
     */
    override suspend fun cancelRun(threadId: String, runId: String) = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads/$threadId/runs/$runId/cancel")
        }.run {
            HttpResponseHandler<Unit>(this).invoke { }
        }
    }

    /**
     * Uploads a file to the OpenAI Assistant API.
     *
     * @param file The file to upload.
     * @param purpose The purpose of the file.
     * @return A [Result] containing the uploaded [FileUploadResponse].
     */
    override suspend fun uploadFile(file: File, purpose: FilePurpose) = kotlin.runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/files")
            setBody(MultiPartFormDataContent(formData {
                append("purpose", purpose.value)
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"${file.name}\"")
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                })
            }))
        }.run {
            HttpResponseHandler<FileUploadResponse>(this).invoke { body<FileUploadResponse>() }
        }
    }

    /**
     * Submits tool output for a run.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     * @param requestBody The request body containing the tool output details.
     * @return A [Result] containing the updated [Run].
     */
    override suspend fun submitToolOutput(threadId: String, runId: String, requestBody: SubmitToolOutputsRequestBody) =
        runCatching {
            httpClient.post {
                setUpAssistantRequest("/v1/threads/$threadId/runs/$runId/submit_tool_outputs")
                setBody(requestBody)
            }.run {
                HttpResponseHandler<Run>(this).invoke { body<Run>() }
            }
        }

    /**
     * Sets up the HTTP request for the OpenAI Assistant API.
     *
     * @param path The API endpoint path.
     */
    private fun HttpRequestBuilder.setUpAssistantRequest(path: String) {
        url {
            host = baseUrl.host
            protocol = baseUrl.protocol
            port = baseUrl.port
            encodedPath = path
        }
        headers {
            append("Authorization", "Bearer $apiKey")
            append("OpenAI-Beta", "assistants=v2")
        }
        contentType(ContentType.Application.Json)
    }
}