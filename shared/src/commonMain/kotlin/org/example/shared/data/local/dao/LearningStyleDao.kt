package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.dao.contract.BaseDao
import org.example.shared.data.local.entity.LearningStyleEntity

@Dao
interface LearningStyleDao : BaseDao<LearningStyleEntity> {
    @Query("SELECT * FROM learning_style WHERE id = :id")
    override suspend fun get(id: String): LearningStyleEntity?
}