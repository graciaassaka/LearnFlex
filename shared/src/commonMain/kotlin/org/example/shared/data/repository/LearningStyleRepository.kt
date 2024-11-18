package org.example.shared.data.repository

import org.example.shared.data.local.dao.LearningStyleDao
import org.example.shared.data.local.entity.LearningStyleEntity
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.sync.SyncOperation

class LearningStyleRepository(
    remoteDataSource: RemoteDataSource<LearningStyle>,
    learningStyleDao: LearningStyleDao,
    syncManager: SyncManager<LearningStyle>
) : BaseRepository<LearningStyle, LearningStyleEntity>(
    remoteDataSource = remoteDataSource,
    dao = learningStyleDao,
    syncManager = syncManager,
    syncOperationFactory = { model, type -> SyncOperation(model, type) },
    modelMapper = object : ModelMapper<LearningStyle, LearningStyleEntity> {
        override fun toModel(entity: LearningStyleEntity) =
            with(entity) { LearningStyle(id, style, createdAt, lastUpdated) }

        override fun toEntity(model: LearningStyle) =
            with(model) { LearningStyleEntity(id, style, createdAt, lastUpdated) }
    }
)