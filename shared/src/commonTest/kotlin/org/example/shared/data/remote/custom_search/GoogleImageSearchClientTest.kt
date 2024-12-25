package org.example.shared.data.remote.custom_search

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer.configureFor
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.shared.domain.client.ImageSearchClient

import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@WireMockTest
class GoogleImageSearchClientTest {
    private lateinit var googleImageSearchClient: GoogleImageSearchClient
    private lateinit var httpClient: HttpClient
    private lateinit var wireMockServer: WireMockServer
    private lateinit var baseUrl: Url

    @Before
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.options().port(9099))
        wireMockServer.start()
        configureFor(wireMockServer.port())

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }

        baseUrl = URLBuilder(
            protocol = URLProtocol.HTTP,
            host = "localhost",
            port = wireMockServer.port()
        ).build()

        googleImageSearchClient = GoogleImageSearchClient(
            httpClient = httpClient,
            baseUrl = baseUrl,
            apiKey = "test_api_key",
            searchEngineId = "test_search_engine_id"
        )
    }

    @After
    fun tearDown() {
        wireMockServer.stop()
        httpClient.close()
    }

    @Test
    fun `searchImages should return success when request is successful`() = runTest {
        // Given
        val query = "test_query"
        val numResults = 10
        val searchResponse = ImageSearchClient.SearchResponse(items = listOf(ImageSearchClient.SearchItem(link = "test_link")))
        wireMockServer.stubFor(
            get(urlPathEqualTo("/"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(Json.encodeToString(searchResponse))
                )
        )

        // When
        val result = googleImageSearchClient.searchImages(query, numResults).single()

        // Then
        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withQueryParam("key", equalTo("test_api_key"))
                .withQueryParam("cx", equalTo("test_search_engine_id"))
                .withQueryParam("q", equalTo(query))
                .withQueryParam("searchType", equalTo("image"))
                .withQueryParam("num", equalTo(numResults.toString()))
        )
        assertTrue(result.isSuccess)
        assertEquals(searchResponse, result.getOrNull())
    }

    @Test
    fun `searchImages should return failure when request is unsuccessful`() = runTest {
        // Given
        val query = "test_query"
        val numResults = 10
        wireMockServer.stubFor(
            get(urlPathEqualTo("/"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                )
        )

        // When
        val result = googleImageSearchClient.searchImages(query, numResults).single()

        // Then
        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withQueryParam("key", equalTo("test_api_key"))
                .withQueryParam("cx", equalTo("test_search_engine_id"))
                .withQueryParam("q", equalTo(query))
                .withQueryParam("searchType", equalTo("image"))
                .withQueryParam("num", equalTo(numResults.toString()))
        )
        assertTrue(result.isFailure)
    }
}