package org.example.shared.data.assistant

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.shared.data.model.*
import org.example.shared.domain.service.AIAssistantClient

/**
 * Client for interacting with the OpenAI Assistant API.
 *
 * @property apiKey The API key for authenticating requests.
 * @property httpClient The HTTP client used for making requests.
 */
class OpenAIAssistantClient(
    private val httpClient: HttpClient
) : AIAssistantClient {

    private var apiKey: String = System.getenv("OPENAI_API_KEY") ?: throw Exception("OpenAI API key not found")

    /**
     * Creates a new thread.
     *
     * @return A [Result] containing the created [Thread].
     */
    override suspend fun createThread(): Result<Thread> = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads")
        }.run {
            if (status.isSuccess()) body<Thread>() else throw Exception()
        }
    }

    /**
     * Retrieves a thread by its ID.
     *
     * @param threadId The ID of the thread to retrieve.
     * @return A [Result] containing the retrieved [Thread].
     */
    override suspend fun retrieveThread(threadId: String): Result<Thread> = runCatching {
        httpClient.get {
            setUpAssistantRequest("/v1/threads/$threadId")
        }.run {
            if(status.isSuccess()) body<Thread>() else throw Exception("Failed to retrieve thread: $status")
        }
    }

    /**
     * Deletes a thread by its ID.
     *
     * @param threadId The ID of the thread to delete.
     * @return A [Result] indicating the success or failure of the operation.
     */
    override suspend fun deleteThread(threadId: String): Result<Unit> = runCatching {
        httpClient.delete {
            setUpAssistantRequest("/v1/threads/$threadId")
        }.run {
            if(status.isSuccess().not()) throw Exception("Failed to delete thread: $status")
        }
    }

    /**
     * Creates a new message in a thread.
     *
     * @param threadId The ID of the thread.
     * @param requestBody The request body containing the message details.
     * @return A [Result] containing the created [Message].
     */
    override suspend fun createMessage(threadId: String, requestBody: MessageRequestBody): Result<Message> = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads/$threadId/messages")
            setBody(requestBody)
        }.run {
            if (status.isSuccess()) body<Message>() else throw Exception("Failed to create message: $status")
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
    override suspend fun listMessages(threadId: String, limit: Int, order: MessagesOrder): Result<ListMessagesResponse> = runCatching {
        httpClient.get {
            setUpAssistantRequest("/v1/threads/$threadId/messages")
            parameter("limit", limit)
            parameter("order", order.name)
        }.run {
            if (status.isSuccess()) body<ListMessagesResponse>() else throw Exception("Failed to list messages: $status")
        }
    }

    /**
     * Creates a new run in a thread.
     *
     * @param threadId The ID of the thread.
     * @param requestBody The request body containing the run details.
     * @return A [Result] containing the created [Run].
     */
    override suspend fun createRun(threadId: String, requestBody: RunRequestBody): Result<Run> = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads/$threadId/runs")
            setBody(requestBody)
        }.run {
            if (status.isSuccess()) body<Run>() else throw Exception("Failed to create run: $status")
        }
    }

    /**
     * Retrieves a run by its ID.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run to retrieve.
     * @return A [Result] containing the retrieved [Run].
     */
    override suspend fun retrieveRun(threadId: String, runId: String): Result<Run> = runCatching {
        httpClient.get {
            setUpAssistantRequest("/v1/threads/$threadId/runs/$runId")
        }.run {
            if (status.isSuccess()) body<Run>() else throw Exception("Failed to retrieve run: $status")
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
    override suspend fun submitToolOutput(threadId: String, runId: String,requestBody: SubmitToolOutputsRequestBody): Result<Run> = runCatching {
        httpClient.post {
            setUpAssistantRequest("/v1/threads/$threadId/runs/$runId/tool_outputs")
            setBody(requestBody)
        }.run {
            if (status.isSuccess()) body<Run>() else throw Exception("Failed to submit tool output: $status")
        }
    }

    /**
     * Sets up the HTTP request for the OpenAI Assistant API.
     *
     * @param path The API endpoint path.
     */
    private fun HttpRequestBuilder.setUpAssistantRequest(path: String) {
        url {
            protocol = URLProtocol.HTTPS
            host = "api.openai.com"
            encodedPath = path
        }
        headers {
            append("Authorization", "Bearer $apiKey")
            append("OpenAI-Beta", "assistants=v2")
        }
        contentType(ContentType.Application.Json)
    }
}