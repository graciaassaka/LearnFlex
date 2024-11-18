package org.example.shared.data.sync.handler.delagate

import org.example.shared.data.local.dao.contract.BaseDao
import org.example.shared.data.sync.util.ModelHelper
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation

class SyncDelegate<Model : DatabaseRecord, Entity : DatabaseRecord>(
    private val remoteDataSource: RemoteDataSource<Model>,
    private val dao: BaseDao<Entity>,
    private val modelHelper: ModelHelper<Model, Entity>
) : SyncHandler<Model> {
    override suspend fun handleSync(operation: SyncOperation<Model>) {
        when (operation.type) {
            SyncOperationType.CREATE,
            SyncOperationType.UPDATE -> remoteDataSource.create(operation.data).getOrThrow()

            SyncOperationType.DELETE -> remoteDataSource.delete(operation.data.id).getOrThrow()

            SyncOperationType.SYNC   -> operation.data.sync()
        }
    }

    private suspend fun Model.sync() {
        val remote = remoteDataSource.fetch(id).getOrThrow()

        if (lastUpdated < remote.lastUpdated) {
            dao.update(modelHelper.toEntity(remote))
        } else {
            remoteDataSource.create(this).getOrThrow()
        }
    }
}