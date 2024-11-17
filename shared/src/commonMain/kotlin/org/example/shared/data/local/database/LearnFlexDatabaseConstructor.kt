package org.example.shared.data.local.database

import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object LearnFlexDatabaseConstructor: RoomDatabaseConstructor<LearnFlexDatabase> {
    override fun initialize(): LearnFlexDatabase
}