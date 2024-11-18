package org.example.shared.domain.model

import kotlinx.serialization.Serializable
import org.example.shared.domain.model.contract.DatabaseRecord

@Serializable
open class LearningStyle(
    override val id: String,
    open val style: StyleResult,
    override val createdAt: Long = System.currentTimeMillis(),
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord