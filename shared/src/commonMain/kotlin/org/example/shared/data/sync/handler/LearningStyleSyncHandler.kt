package org.example.shared.data.sync.handler

import org.example.shared.data.local.dao.LearningStyleDao
import org.example.shared.data.local.entity.LearningStyleEntity
import org.example.shared.data.sync.handler.delagate.SyncDelegate
import org.example.shared.data.sync.util.ModelHelper
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.sync.SyncHandler

class LearningStyleSyncHandler(
    remoteDataSource: RemoteDataSource<LearningStyle>,
    learningStyleDao: LearningStyleDao
) : SyncHandler<LearningStyle> by SyncDelegate(
    remoteDataSource,
    learningStyleDao,
    object : ModelHelper<LearningStyle, LearningStyleEntity> {
        override fun getId(model: LearningStyle) = model.id
        override fun getLastUpdated(model: LearningStyle) = model.lastUpdated
        override fun toEntity(model: LearningStyle) =
            with(model) { LearningStyleEntity(id, style, createdAt, lastUpdated) }
    }
)