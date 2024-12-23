package org.example.shared.data.repository.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.repository.util.QueryStrategy
import org.example.shared.domain.storage_operations.QueryByCurriculumIdOperation

class QueryByCurriculumIdRepositoryComponent<Model : DatabaseRecord, Entity : RoomEntity>(
    private val config: RepositoryConfig<Model, Entity>
) : QueryByCurriculumIdOperation<Model> {

    class CurriculumQueryStrategy<Entity : RoomEntity>(
        private val strategy: (String) -> Flow<List<Entity>>
    ) : QueryStrategy<List<Entity>> {
        private var curriculumId: String? = null

        fun configure(newCurriculumId: String): CurriculumQueryStrategy<Entity> {
            curriculumId = newCurriculumId
            return this
        }

        override fun execute(): Flow<List<Entity>> {
            requireNotNull(curriculumId) { "Curriculum ID must be set before executing query" }
            return strategy(curriculumId!!)
        }
    }

    override suspend fun getByCurriculumId(curriculumId: String) = runCatching {
        val strategy = config.queryStrategies.getCustomStrategy<List<Entity>>(CURRICULUM_STRATEGY_KEY)
                as CurriculumQueryStrategy

        strategy.configure(curriculumId)
            .execute()
            .map { entities -> entities.map(config.modelMapper::toModel) }
            .first()
    }

    companion object {
        const val CURRICULUM_STRATEGY_KEY = "curriculum_query_strategy"
    }
}