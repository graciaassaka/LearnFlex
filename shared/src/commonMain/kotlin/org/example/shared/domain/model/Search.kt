package org.example.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val items: List<SearchItem>
)

@Serializable
data class SearchItem(
    val link: String
)