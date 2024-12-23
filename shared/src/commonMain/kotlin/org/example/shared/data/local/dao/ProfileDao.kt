package org.example.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.ProfileEntity

@Dao
abstract class ProfileDao : LocalDao<ProfileEntity>() {
    @Query(
        """
            SELECT * FROM profiles 
            WHERE id = :id
        """
    )
    abstract fun get(id: String): Flow<ProfileEntity?>
}