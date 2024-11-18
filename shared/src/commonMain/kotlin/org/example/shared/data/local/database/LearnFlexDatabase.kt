package org.example.shared.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.example.shared.data.local.converter.PreferencesConverter
import org.example.shared.data.local.converter.StyleConverter
import org.example.shared.data.local.dao.LearningStyleDao
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.LearningStyleEntity
import org.example.shared.data.local.entity.UserProfileEntity

@Database(
    version = DatabaseConfig.DATABASE_VERSION,
    entities = [
        UserProfileEntity::class,
        LearningStyleEntity::class
    ]
)
@TypeConverters(
    value = [
        PreferencesConverter::class,
        StyleConverter::class
    ]
)
@ConstructedBy(LearnFlexDatabaseConstructor::class)
abstract class LearnFlexDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun learningStyleDao(): LearningStyleDao
}

