package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Interface for performing image search operations.
 */
interface ImageSearchClient {
    /**
     * Represents the response of a search operation.
     *
     * @property items The list of search items.
     */
    @Serializable
    data class SearchResponse(
        val items: List<SearchItem>
    )

    /**
     * Represents an individual search item.
     *
     * @property link The link associated with the search item.
     */
    @Serializable
    data class SearchItem(
        val link: String
    )

    /**
     * Searches for images based on the provided query and the number of requested results.
     *
     * @param query The search query used to fetch relevant images.
     * @param numResults The number of images to retrieve.
     * @return A [Flow] emitting a [Result] containing a [SearchResponse] with the search results.
     */
    fun searchImages(query: String, numResults: Int): Flow<Result<SearchResponse>>
}