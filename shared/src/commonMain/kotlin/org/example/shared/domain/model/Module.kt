package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * Module represents a part of a curriculum, including details like title, description, and quiz score.
 *
 * @property id The unique identifier of the module.
 * @property curriculumId The identifier of the curriculum this module belongs to.
 * @property title The title of the module.
 * @property description A brief description of the module.
 * @property index The index position of the module within the curriculum.
 * @property quizScore The score of the quiz associated with the module.
 * @property createdAt The timestamp when the module was created.
 * @property lastUpdated The timestamp when the module was last updated.
 */
@Serializable
open class Module(
    @SerialName("id")
    override val id: String,

    @SerialName("curriculum_id")
    open val curriculumId: String,

    @SerialName("title")
    open val title: String,

    @SerialName("description")
    open val description: String,

    @SerialName("index")
    open val index: Int,

    @SerialName("quiz_score")
    open val quizScore: Int,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord
