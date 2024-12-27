package org.example.shared.domain.constant

import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Enum class representing different types of files.
 *
 * @property value The maximum size of the file type in bytes.
 */
enum class FileType(override val value: Long) : ValuableEnum<Long> {
    /**
     * Represents an image file type.
     */
    IMAGE(10 * 1024 * 1024),

    /**
     * Represents a document file type.
     */
    DOCUMENT(20 * 1024 * 1024),
}
