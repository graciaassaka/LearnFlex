package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.SearchResponse

interface ImageSearchClient {
    fun searchImages(query: String, numResults: Int): Flow<Result<SearchResponse>>
}