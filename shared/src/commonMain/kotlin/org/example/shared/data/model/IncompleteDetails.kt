package org.example.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents details about an incomplete message.
 */
@Serializable
@SerialName("incomplete_details")
data class IncompleteDetails(
    val reason: String,
)