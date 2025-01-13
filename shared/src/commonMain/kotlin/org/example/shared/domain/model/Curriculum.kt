package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.DescribableRecord
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Curriculum represents a collection of learning content.
 *
 * @property id The unique identifier of the curriculum.
 * @property title The title of the curriculum.
 * @property description A brief description of the curriculum.
 * @property content The content of the curriculum.
 * @property status The status of the curriculum.
 * @property createdAt The timestamp when the curriculum was created.
 * @property lastUpdated The timestamp when the curriculum was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Curriculum(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("title")
    override val title: String,

    @SerialName("description")
    override val description: String,

    @SerialName("content")
    val content: List<String>,

    @SerialName("status")
    val status: String = Status.UNFINISHED.name,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, DescribableRecord {

    /**
     * Updates the status of the curriculum based on the completion status of the provided modules.
     *
     * @param modules The list of modules to evaluate completion status.
     * @return A new instance of the curriculum with the updated status.
     */
    fun updateStatus(modules: List<Module>) = copy(
        status = (if (modules.isNotEmpty() && modules.all { it.isCompleted() }) Status.FINISHED else Status.UNFINISHED).name
    )
}
