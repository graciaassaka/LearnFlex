package org.example.shared.data.local.dao

import androidx.room.*
import org.example.shared.data.local.entity.UserProfileEntity

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfileEntity: UserProfileEntity)

    @Update
    suspend fun update(userProfileEntity: UserProfileEntity)

    @Delete
    suspend fun delete(userProfileEntity: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getActiveProfile(): UserProfileEntity?
}