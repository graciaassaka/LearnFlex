package org.example.shared.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import org.example.shared.domain.model.LearningStyle

/**
 * A converter class for converting `StyleResult` objects to and from JSON strings.
 */
class StyleConverter {

    /**
     * Converts a `StyleResult` object to a JSON string.
     *
     * @param style The `StyleResult` object to be converted.
     * @return The JSON string representation of the `StyleResult` object.
     */
    @TypeConverter
    fun fromStyle(style: LearningStyle): String {
        return Json.encodeToString(LearningStyle.serializer(), style)
    }

    /**
     * Converts a JSON string to a `StyleResult` object.
     *
     * @param style The JSON string to be converted.
     * @return The `StyleResult` object represented by the JSON string.
     */
    @TypeConverter
    fun toStyle(style: String): LearningStyle {
        return Json.decodeFromString(LearningStyle.serializer(), style)
    }
}