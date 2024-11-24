package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.shared.domain.model.contract.DatabaseRecord

@Entity(tableName = "cached_image")
data class CachedImageEntity(
    @PrimaryKey
    override val id: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "local_path")
    val localPath: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long,

    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long,
) : DatabaseRecord