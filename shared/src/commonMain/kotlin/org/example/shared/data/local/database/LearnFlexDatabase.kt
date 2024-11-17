package org.example.shared.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.example.shared.data.local.converter.PreferencesConverter
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.UserProfileEntity

@Database(
    version = DatabaseConfig.DATABASE_VERSION,
    entities = [
        UserProfileEntity::class
    ]
)
@TypeConverters(PreferencesConverter::class)
@ConstructedBy(LearnFlexDatabaseConstructor::class)
abstract class LearnFlexDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}

