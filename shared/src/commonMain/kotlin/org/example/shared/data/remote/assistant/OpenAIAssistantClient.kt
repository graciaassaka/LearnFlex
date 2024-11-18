package org.example.shared.data.remote.assistant

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.example.shared.data.remote.model.*
import org.example.shared.data.remote.util.ApiError
import org.example.shared.data.remote.util.ErrorContainer
import org.example.shared.domain.service.AIAssistantClient

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
     * @return A [Result] containing the created [Thread].
     */
    override suspend fun createThread() = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads")
        }.run {
            handleAssistantResponse { body<Thread>() }
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
            handleAssistantResponse { body<Thread>() }
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
            handleAssistantResponse { }
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
            handleAssistantResponse { body<Message>() }
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
            handleAssistantResponse { body<ListMessagesResponse>() }
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
            handleAssistantResponse { body<Run>() }
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
            handleAssistantResponse { body<Run>() }
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
                setUpAssistantRequest("/v1/threads/$threadId/runs/$runId/tool_outputs")
                setBody(requestBody)
            }.run {
                handleAssistantResponse { body<Run>() }
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

    /**
     * Handles the response from the OpenAI Assistant API.
     *
     * @param handleSuccess The lambda to execute if the request is successful.
     */
    private suspend fun <T> HttpResponse.handleAssistantResponse(handleSuccess: (suspend () -> T)): T {
        val errorContainer = if (status.isSuccess()) null else body<ErrorContainer>()
        return when (status.value) {
                200  -> handleSuccess()
                400  -> throw ApiError.BadRequest(requestPath = request.url.encodedPath, errorContainer = errorContainer)
                401  -> throw ApiError.Unauthorized(requestPath = request.url.encodedPath, errorContainer = errorContainer)
                403  -> throw ApiError.Forbidden(requestPath = request.url.encodedPath,errorContainer = errorContainer)
                404  -> throw ApiError.NotFound(requestPath = request.url.encodedPath, errorContainer = errorContainer)
                429  -> throw ApiError.RateLimitExceeded(requestPath = request.url.encodedPath, errorContainer = errorContainer)
                503  -> throw ApiError.NetworkError(requestPath = request.url.encodedPath, errorContainer = errorContainer)
                else -> throw ApiError.ServerError(requestPath = request.url.encodedPath, errorCode = status.value, errorContainer = errorContainer)
            }
        }
}