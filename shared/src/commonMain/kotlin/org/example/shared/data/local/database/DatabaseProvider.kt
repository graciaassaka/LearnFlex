package org.example.shared.data.local.database

expect class DatabaseProvider {
    fun getDatabase(): LearnFlexDatabase
}