package org.example.shared.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.example.shared.data.local.converter.PreferencesConverter
import org.example.shared.data.local.converter.StyleConverter
import org.example.shared.data.local.dao.*
import org.example.shared.data.local.entity.*

@Database(
    version = DatabaseConfig.DATABASE_VERSION,
    entities = [
        UserProfileEntity::class,
        CurriculumEntity::class,
        ModuleEntity::class,
        LessonEntity::class,
        SectionEntity::class,
        CachedImageEntity::class
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
    abstract fun curriculumDao(): CurriculumDao
    abstract fun moduleDao(): ModuleDao
    abstract fun lessonDao(): LessonDao
    abstract fun sectionDao(): SectionDao
    abstract fun cachedImageDao(): CachedImageDao
}
