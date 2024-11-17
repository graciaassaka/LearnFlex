package org.example.shared.data.local.converter

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.json.Json
import org.example.shared.domain.model.LearningPreferences

class PreferencesConverter {
    @TypeConverter
    fun fromPreferences(preferences: LearningPreferences): String {
        return Json.encodeToString(LearningPreferences.serializer(), preferences)
    }

    @TypeConverter
    fun toPreferences(preferences: String): LearningPreferences {
        return Json.decodeFromString(LearningPreferences.serializer(), preferences)
    }
}