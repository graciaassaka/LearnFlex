package org.example.shared.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Converts a List<String> to a JSON string for database storage, and back.
 *
 * This ensures commas inside the strings do NOT get split as separate items.
 */
class StringListConverter {

    /**
     * Convert the list of strings into a JSON string.
     */
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return Json.encodeToString(list)
    }

    /**
     * Convert the JSON string back into a list of strings.
     */
    @TypeConverter
    fun toStringList(data: String): List<String> {
        return Json.decodeFromString(data)
    }
}
