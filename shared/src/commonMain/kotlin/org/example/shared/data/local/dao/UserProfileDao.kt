package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.example.shared.data.local.entity.UserProfileEntity

@Dao
abstract class UserProfileDao : BaseDao<UserProfileEntity>() {
    @Query(
        """
            SELECT * FROM user_profile 
            WHERE id = :id
        """
    )
    abstract suspend fun get(id: String): UserProfileEntity?
}