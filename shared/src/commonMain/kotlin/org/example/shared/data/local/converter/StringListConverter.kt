package org.example.shared.data.local.converter

import androidx.room.TypeConverter

/**
 * A converter class responsible for transforming a list of strings into a single string
 * and vice versa. This is useful for storing lists of strings in databases that do not
 * natively support list data types, by converting the data to a storable format.
 */
class StringListConverter {

    /**
     * Converts a list of strings into a single comma-separated string.
     *
     * @param list The list of strings to be converted.
     * @return A single string containing the elements of the list separated by commas.
     */
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    /**
     * Converts a comma-separated string into a list of strings.
     *
     * @param data The comma-separated string to be converted.
     * @return A list of strings obtained by splitting the input string on commas.
     */
    @TypeConverter
    fun toStringList(data: String): List<String> {
        return data.split(",")
    }
}