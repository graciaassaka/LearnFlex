package org.example.shared.data.remote.assistant

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.shared.data.remote.model.*
import org.example.shared.data.remote.util.ApiError
import org.example.shared.data.util.OpenAIConstants
import org.junit.After
import org.junit.Before
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@WireMockTest
@ExperimentalCoroutinesApi
class OpenAIAssistantClientTest {
    private lateinit var openAIAssistantClient: OpenAIAssistantClient
    private lateinit var httpClient: HttpClient
    private lateinit var wireMockServer: WireMockServer
    private lateinit var baseUrl: Url

    @Before
    fun setUp()
    {
        wireMockServer = WireMockServer(WireMockConfiguration.options().port(9099))
        wireMockServer.start()
        configureFor(wireMockServer.port())
        baseUrl = URLBuilder(protocol = URLProtocol.HTTP, host = "localhost", port = wireMockServer.port()).build()
        httpClient = HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60000L
                connectTimeoutMillis = 60000L
                socketTimeoutMillis = 60000L
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    classDiscriminator = "type"
                    encodeDefaults = true
                    coerceInputValues = true
                })
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.NONE
            }

            engine {
                config {
                    retryOnConnectionFailure(true)
                    connectTimeout(60_000, TimeUnit.MILLISECONDS)
                    readTimeout(60_000, TimeUnit.MILLISECONDS)
                    writeTimeout(60_000, TimeUnit.MILLISECONDS)
                }
            }
        }

        openAIAssistantClient = OpenAIAssistantClient(httpClient, baseUrl, OpenAIConstants.API_KEY)
    }

    @After
    fun tearDown()
    {
        wireMockServer.stop()
        httpClient.close()
    }

    @Test
    fun `createThread should return success when status code is 200`() = runTest {
        // Arrange
        val thread = Thread(
            id = "thread_abc123",
            objectType = "thread",
            createdAt = 1699044208,
            metadata = emptyMap()
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .withHeader("OpenAI-Beta", equalTo("assistants=v2"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "id":"${thread.id}",
                                "object": "${thread.objectType}",
                                "created_at": ${thread.createdAt},
                                "metadata": {}
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.createThread()

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads")))
        assertTrue(result.isSuccess)
        assertEquals("thread_abc123", result.getOrNull()?.id)
    }

    @Test
    fun `createThread should return failure when status code is 401`() = runTest {
        // Arrange
        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .withHeader("OpenAI-Beta", equalTo("assistants=v2"))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Invalid Authentication",
                                    "type": "invalid_request_error",
                                    "code": "invalid_api_key"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.createThread()

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.Unauthorized)
    }

    @Test
    fun `retrieveThread should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        wireMockServer.stubFor(
            get(urlEqualTo("/v1/threads/$threadId"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "id": "$threadId",
                                "object": "thread",
                                "created_at": 1699044208,
                                "metadata": {}
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.retrieveThread(threadId)

        // Assert
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/v1/threads/$threadId")))
        assertTrue(result.isSuccess)
        assertEquals(threadId, result.getOrNull()?.id)
    }

    @Test
    fun `retrieveThread should return failure when status code is 404`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        wireMockServer.stubFor(
            get(urlEqualTo("/v1/threads/$threadId"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Thread not found",
                                    "type": "invalid_request_error",
                                    "code": "thread_not_found"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.retrieveThread(threadId)

        // Assert
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/v1/threads/$threadId")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NotFound)
    }

    @Test
    fun `deleteThread should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        wireMockServer.stubFor(
            delete(urlEqualTo("/v1/threads/$threadId"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")
                )
        )

        // Act
        val result = openAIAssistantClient.deleteThread(threadId)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteThread should return failure when status code is 404`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        wireMockServer.stubFor(
            delete(urlEqualTo("/v1/threads/$threadId"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Thread not found",
                                    "type": "invalid_request_error",
                                    "code": "thread_not_found"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.deleteThread(threadId)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NotFound)
    }

    @Test
    fun `createMessage should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val requestBody = MessageRequestBody(
            role = "user",
            content = "Hello, assistant!"
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/messages"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                           {
                              "id": "msg_abc123",
                              "object": "thread.message",
                              "created_at": 1698983503,
                              "thread_id": "thread_abc123",
                              "role": "assistant",
                              "content": [
                                {
                                  "type": "text",
                                  "text": {
                                    "value": "Hi! How can I help you today?",
                                    "annotations": []
                                  }
                                }
                              ],
                              "assistant_id": "asst_abc123",
                              "run_id": "run_abc123",
                              "attachments": [],
                              "metadata": {}
                           }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.createMessage(threadId, requestBody)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/messages")))
        assertTrue(result.isSuccess)
        assertEquals("msg_abc123", result.getOrNull()?.id)
    }

    @Test
    fun `createMessage should return failure when status code is 429`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val requestBody = MessageRequestBody(
            role = "user",
            content = "Hello, assistant!"
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/messages"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Invalid request",
                                    "type": "invalid_request_error",
                                    "code": "invalid_request"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.createMessage(threadId, requestBody)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/messages")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.RateLimitExceeded)
    }

    @Test
    fun `listMessages should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/threads/$threadId/messages"))
                .withQueryParam("limit", equalTo("10"))
                .withQueryParam("order", equalTo("desc"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "object": "list",
                                "data": [
                                    {
                                        "id": "msg_abc123",
                                        "object": "thread.message",
                                        "created_at": 1699016383,
                                        "assistant_id": null,
                                        "thread_id": "thread_abc123",
                                        "run_id": null,
                                        "role": "user",
                                        "content": [
                                            {
                                            "type": "text",
                                            "text": {
                                                    "value": "How does AI work? Explain it in simple terms.",
                                                "annotations": []
                                            }
                                            }
                                        ],
                                        "attachments": [],
                                        "metadata": {}
                                    },
                                    {
                                        "id": "msg_abc456",
                                        "object": "thread.message",
                                        "created_at": 1699016383,
                                        "assistant_id": null,
                                        "thread_id": "thread_abc123",
                                        "run_id": null,
                                        "role": "user",
                                        "content": [
                                            {
                                                "type": "text",
                                                "text": {
                                                    "value": "Hello, what is AI?",
                                                    "annotations": []
                                                }
                                            }
                                        ],
                                        "attachments": [],
                                        "metadata": {}
                                    }
                                ],
                                "first_id": "msg_abc123",
                                "last_id": "msg_abc456",
                                "has_more": false
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.listMessages(threadId, 10, MessagesOrder.DESC)

        // Assert
        wireMockServer.verify(
            1, getRequestedFor(urlPathEqualTo("/v1/threads/$threadId/messages"))
                .withQueryParam("limit", equalTo("10"))
                .withQueryParam("order", equalTo("desc"))
        )
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.data?.size)
    }

    @Test
    fun `listMessages should return failure when status code is 404`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/threads/$threadId/messages"))
                .withQueryParam("limit", equalTo("10"))
                .withQueryParam("order", equalTo("desc"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Thread not found",
                                    "type": "invalid_request_error",
                                    "code": "thread_not_found"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.listMessages(threadId, 10, MessagesOrder.DESC)

        // Assert
        wireMockServer.verify(
            1, getRequestedFor(urlPathEqualTo("/v1/threads/$threadId/messages"))
                .withQueryParam("limit", equalTo("10"))
                .withQueryParam("order", equalTo("desc"))
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NotFound)
    }

    @Test
    fun `createRun should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val requestBody = RunRequestBody(
            assistantId = "asst_abc123",
            instructions = "Run instructions"
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/runs"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "id": "run_abc123",
                                "object": "thread.run",
                                "created_at": 1699063290,
                                "assistant_id": "asst_abc123",
                                "thread_id": "thread_abc123",
                                "status": "queued",
                                "started_at": 1699063290,
                                "expires_at": null,
                                "cancelled_at": null,
                                "failed_at": null,
                                "completed_at": 1699063291,
                                "last_error": null,
                                "model": "gpt-4o",
                                "instructions": null,
                                "incomplete_details": null,
                                "tools": [
                                    {
                                        "type": "code_interpreter"
                                    }
                                ],
                                "metadata": {},
                                "usage": null,
                                "temperature": 1.0,
                                "top_p": 1.0,
                                "max_prompt_tokens": 1000,
                                "max_completion_tokens": 1000,
                                "truncation_strategy": {
                                    "type": "auto",
                                    "last_messages": null
                                },
                                "response_format": "auto",
                                "tool_choice": "auto",
                                "parallel_tool_calls": true
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.createRun(threadId, requestBody)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/runs")))
        assertTrue(result.isSuccess)
        assertEquals("run_abc123", result.getOrNull()?.id)
    }

    @Test
    fun `createRun should return failure when status code is 503`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val requestBody = RunRequestBody(
            assistantId = "asst_abc123",
            instructions = "Run instructions"
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/runs"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Network error",
                                    "type": "NetworkError"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.createRun(threadId, requestBody)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/runs")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NetworkError)
    }

    @Test
    fun `retrieveRun should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val runId = "run_abc123"

        wireMockServer.stubFor(
            get(urlEqualTo("/v1/threads/$threadId/runs/$runId"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """ 
                               {
                                  "id": "run_abc123",
                                    "object": "thread.run",
                                  "created_at": 1699075072,
                                    "assistant_id": "asst_abc123",
                                  "thread_id": "thread_abc123",
                                    "status": "completed",
                                  "started_at": 1699075072,
                                  "expires_at": null,
                                  "cancelled_at": null,
                                  "failed_at": null,
                                  "completed_at": 1699075073,
                                  "last_error": null,
                                  "model": "gpt-4o",
                                    "instructions": null,
                                  "incomplete_details": null,
                                  "tools": [
                                    {
                                      "type": "code_interpreter"
                                }
                                  ],
                                  "metadata": {},
                                  "usage": {
                                    "prompt_tokens": 123,
                                    "completion_tokens": 456,
                                    "total_tokens": 579
                                  },
                                  "temperature": 1.0,
                                  "top_p": 1.0,
                                  "max_prompt_tokens": 1000,
                                  "max_completion_tokens": 1000,
                                  "truncation_strategy": {
                                    "type": "auto",
                                    "last_messages": null
                                  },
                                  "response_format": "auto",
                                  "tool_choice": "auto",
                                  "parallel_tool_calls": true
                                }
                            """.trimIndent(
                            )
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.retrieveRun(threadId, runId)

        // Assert
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/v1/threads/$threadId/runs/$runId")))
        assertTrue(result.isSuccess)
        assertEquals(runId, result.getOrNull()?.id)
    }

    @Test
    fun `retrieveRun should return failure when status code is 404`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val runId = "run_abc123"

        wireMockServer.stubFor(
            get(urlEqualTo("/v1/threads/$threadId/runs/$runId"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Run not found",
                                    "type": "invalid_request_error",
                                    "code": "run_not_found"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.retrieveRun(threadId, runId)

        // Assert
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/v1/threads/$threadId/runs/$runId")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NotFound)
    }

    @Test
    fun `submitToolOutput should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val runId = "run_abc123"
        val requestBody = SubmitToolOutputsRequestBody(
            toolOutputs = listOf(
                ToolOutput(
                    toolCallId = "call_abc123",
                    output = "Tool output"
                )
            )
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/runs/$runId/submit_tool_outputs"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "id": "run_abc123",
                                "object": "thread.run",
                              "created_at": 1699075592,
                              "assistant_id": "asst_123",
                              "thread_id": "thread_abc123",
                              "status": "queued",
                              "started_at": 1699075592,
                              "expires_at": 1699076192,
                              "cancelled_at": null,
                              "failed_at": null,
                              "completed_at": null,
                              "last_error": null,
                              "model": "gpt-4o",
                              "instructions": null,
                              "tools": [
                                {
                                  "type": "function",
                                  "function": {
                                    "name": "get_current_weather",
                                    "description": "Get the current weather in a given location",
                                    "parameters": {
                                      "type": "object",
                                      "properties": {
                                        "location": {
                                          "type": "string",
                                          "description": "The city and state, e.g. San Francisco, CA"
                                        },
                                        "unit": {
                                          "type": "string",
                                          "enum": ["celsius", "fahrenheit"]
                                        }
                                      },
                                      "required": ["location"],
                                      "additionalProperties": false
                                    }
                                  }
                                }
                              ],
                              "metadata": {},
                              "usage": null,
                              "temperature": 1.0,
                              "top_p": 1.0,
                              "max_prompt_tokens": 1000,
                              "max_completion_tokens": 1000,
                              "truncation_strategy": {
                                "type": "auto",
                                "last_messages": null
                              },
                              "response_format": "auto",
                              "tool_choice": "auto",
                              "parallel_tool_calls": true
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.submitToolOutput(threadId, runId, requestBody)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/runs/$runId/submit_tool_outputs")))
        assertTrue(result.isSuccess)
        assertEquals(runId, result.getOrNull()?.id)
    }

    @Test
    fun `cancelRun should return success when status code is 200`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val runId = "run_abc123"

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/runs/$runId/cancel"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")
                )
        )

        // Act
        val result = openAIAssistantClient.cancelRun(threadId, runId)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/runs/$runId/cancel")))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelRun should return failure when status code is 503`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val runId = "run_abc123"

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/runs/$runId/cancel"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Network error",
                                    "type": "NetworkError"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.cancelRun(threadId, runId)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/runs/$runId/cancel")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NetworkError)
    }

    @Test
    fun `cancelRun should return failure when status code is 404`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val runId = "run_abc123"

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/runs/$runId/cancel"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Run not found",
                                    "type": "invalid_request_error",
                                    "code": "run_not_found"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.cancelRun(threadId, runId)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/runs/$runId/cancel")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NotFound)
    }

    @Test
    fun `submitToolOutput should return failure when status code is 404`() = runTest {
        // Arrange
        val threadId = "thread_abc123"
        val runId = "run_abc123"
        val requestBody = SubmitToolOutputsRequestBody(
            toolOutputs = listOf(
                ToolOutput(
                    toolCallId = "call_abc123",
                    output = "Tool output"
                )
            )
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/v1/threads/$threadId/runs/$runId/submit_tool_outputs"))
                .withHeader("Authorization", equalTo("Bearer ${OpenAIConstants.API_KEY}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "error": {
                                    "message": "Run not found",
                                    "type": "invalid_request_error",
                                    "code": "run_not_found"
                                }
                            }
                            """.trimIndent()
                        )
                )
        )

        // Act
        val result = openAIAssistantClient.submitToolOutput(threadId, runId, requestBody)

        // Assert
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/v1/threads/$threadId/runs/$runId/submit_tool_outputs")))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiError.NotFound)
    }
}