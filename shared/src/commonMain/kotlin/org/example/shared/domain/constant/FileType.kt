package org.example.shared.domain.constant

/**
 * Enum class representing different types of files.
 */
enum class FileType(val maxFileSize: Long) {
    /**
     * Represents an image file type.
     */
    IMAGE(10 * 1024 * 1024),

    /**
     * Represents a document file type.
     */
    DOCUMENT(20 * 1024 * 1024),
}
