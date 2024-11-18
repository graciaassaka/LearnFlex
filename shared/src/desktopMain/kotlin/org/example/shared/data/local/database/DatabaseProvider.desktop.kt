package org.example.shared.data.local.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

actual class DatabaseProvider {
    actual fun getDatabase(): LearnFlexDatabase {
        val dbFile = File(System.getProperty("java.io.tmpdir"), DatabaseConfig.DATABASE_NAME)
        return Room.databaseBuilder<LearnFlexDatabase>(
            name = dbFile.absolutePath
        ).fallbackToDestructiveMigration(true)
            .setQueryCoroutineContext(Dispatchers.IO)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}