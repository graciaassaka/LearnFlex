package org.example.shared.data.local.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers

actual class DatabaseProvider(private val context: Context) {
    actual fun getDatabase() = Room.databaseBuilder(
        context = context.applicationContext,
        klass = LearnFlexDatabase::class.java,
        name = context.getDatabasePath(DatabaseConfig.DATABASE_NAME).absolutePath,
    ).addMigrations(*DatabaseMigration.ALL_MIGRATIONS)
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}