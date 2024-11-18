package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.model.StyleResult

@Entity(tableName = "learning_style")
data class LearningStyleEntity(
    @PrimaryKey
    override val id: String,

    @ColumnInfo(name = "learning_style")
    override val style: StyleResult,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long
) : LearningStyle(id, style, createdAt, lastUpdated)