package org.example.shared.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.example.shared.data.local.converter.PreferencesConverter
import org.example.shared.data.local.converter.StringListConverter
import org.example.shared.data.local.converter.StyleConverter
import org.example.shared.data.local.dao.*
import org.example.shared.data.local.dao.util.TimestampUpdater
import org.example.shared.data.local.entity.*

@Database(
    version = DatabaseConfig.DATABASE_VERSION,
    entities = [
        ProfileEntity::class,
        CurriculumEntity::class,
        ModuleEntity::class,
        LessonEntity::class,
        SectionEntity::class,
        SessionEntity::class
    ]
)
@TypeConverters(
    value = [
        PreferencesConverter::class,
        StyleConverter::class,
        StringListConverter::class
    ]
)
@ConstructedBy(LearnFlexDatabaseConstructor::class)
abstract class LearnFlexDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun curriculumDao(): CurriculumLocalDao
    abstract fun moduleDao(): ModuleLocalDao
    abstract fun lessonDao(): LessonLocalDao
    abstract fun sectionDao(): SectionLocalDao
    abstract fun sessionDao(): SessionLocalDao
    abstract fun profileTimestampUpdater(): TimestampUpdater.ProfileTimestampUpdater
    abstract fun curriculumTimestampUpdater(): TimestampUpdater.CurriculumTimestampUpdater
    abstract fun moduleTimestampUpdater(): TimestampUpdater.ModuleTimestampUpdater
    abstract fun lessonTimestampUpdater(): TimestampUpdater.LessonTimestampUpdater
    abstract fun sectionTimestampUpdater(): TimestampUpdater.SectionTimestampUpdater
    abstract fun sessionTimestampUpdater(): TimestampUpdater.SessionTimestampUpdater
}
