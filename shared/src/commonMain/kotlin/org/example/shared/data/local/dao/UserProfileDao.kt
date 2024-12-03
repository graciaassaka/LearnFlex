package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.UserProfileEntity

@Dao
abstract class UserProfileDao : LocalDao<UserProfileEntity>() {
    @Query(
        """
            SELECT * FROM user_profile 
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<UserProfileEntity?>
}