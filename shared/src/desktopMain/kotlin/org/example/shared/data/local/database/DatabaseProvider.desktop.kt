package org.example.shared.data.local.database

import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import java.io.File

actual class DatabaseProvider {
    actual fun getDatabase(): LearnFlexDatabase {
        val dbFile = File(System.getProperty("java.io.tmpdir"), DatabaseConfig.DATABASE_NAME)
        return Room.databaseBuilder<LearnFlexDatabase>(
            name = dbFile.absolutePath
        ).addMigrations(*DatabaseMigration.ALL_MIGRATIONS)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}