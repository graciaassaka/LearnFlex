package org.example.shared.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import org.example.shared.domain.model.Profile

/**
 * Converter class for transforming LearningPreferences objects to and from JSON strings.
 */
class PreferencesConverter {

    /**
     * Converts a LearningPreferences object to a JSON string.
     *
     * @param preferences The LearningPreferences object to be converted.
     * @return The JSON string representation of the LearningPreferences object.
     */
    @TypeConverter
    fun fromPreferences(preferences: Profile.LearningPreferences): String {
        return Json.encodeToString(Profile.LearningPreferences.serializer(), preferences)
    }

    /**
     * Converts a JSON string to a LearningPreferences object.
     *
     * @param preferences The JSON string to be converted.
     * @return The LearningPreferences object represented by the JSON string.
     */
    @TypeConverter
    fun toPreferences(preferences: String): Profile.LearningPreferences {
        return Json.decodeFromString(Profile.LearningPreferences.serializer(), preferences)
    }
}