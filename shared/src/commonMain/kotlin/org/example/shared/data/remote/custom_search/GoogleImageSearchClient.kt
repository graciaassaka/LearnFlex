package org.example.shared.data.remote.custom_search

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.flow
import org.example.shared.data.remote.util.HttpResponseHandler
import org.example.shared.domain.client.ImageSearchClient
import org.example.shared.domain.model.SearchResponse

/**
 * A client for performing image searches using the Google Custom Search API.
 *
 * @property httpClient The HTTP client used to make requests.
 * @property baseUrl The base URL for the Google Custom Search API.
 * @property apiKey The API key for authenticating requests.
 * @property searchEngineId The search engine ID for the custom search.
 */
class GoogleImageSearchClient(
    private val httpClient: HttpClient,
    private val baseUrl: Url,
    private val apiKey: String,
    private val searchEngineId: String
) : ImageSearchClient {

    /**
     * Searches for images based on the given query and number of results.
     *
     * @param query The search query.
     * @param numResults The number of search results to return.
     * @return A flow emitting the search response.
     */
    override fun searchImages(query: String, numResults: Int) = flow {
        emit(
            runCatching {
                httpClient.get {
                    url(baseUrl)
                    parameter("key", apiKey)
                    parameter("cx", searchEngineId)
                    parameter("q", query)
                    parameter("searchType", "image")
                    parameter("num", numResults)
                }.run {
                    HttpResponseHandler<SearchResponse>(this).invoke {
                        body<SearchResponse>()
                    }
                }
            }
        )
    }
}
