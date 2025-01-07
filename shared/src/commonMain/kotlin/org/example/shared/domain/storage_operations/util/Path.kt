package org.example.shared.domain.storage_operations.util

/**
 * Represents an immutable Firestore path.
 * @param value The constructed Firestore path as a string.
 */
class Path internal constructor(val value: String) {
    val length: Int

    init {
        require(value.isNotBlank()) { "Path must not be blank." }
        value.split("/").let { segments ->
            segments.forEach { require(isValidSegment(it)) { "Invalid path segment: $it" } }
            length = segments.size
        }
    }

    /**
     * Validates if a given string can be used as a path segment.
     *
     * @param segment The string to validate as a path segment.
     * @return `true` if the segment is valid, `false` otherwise.
     */
    private fun isValidSegment(segment: String) =
        segment.isNotBlank() && segment.none { it in setOf('/', '\\', '?', '#', '[', ']') }

    /**
     * Determines if the path represents a collection path in Firestore.
     *
     * @return True if the path represents a collection, false otherwise.
     */
    fun isCollectionPath(): Boolean = length % 2 == 1

    /**
     * Determines if the path is a document path.
     *
     * @return True if the path represents a document, false otherwise.
     */
    fun isDocumentPath(): Boolean = length % 2 == 0

    /**
     * Extracts the last segment of a path, typically representing an identifier.
     *
     * @return The last segment of the path as a string.
     */
    fun getId() = value.split("/").last()

    /**
     * Extracts the parent ID from the path.
     *
     * @return The parent ID as a string, or null if the path does not have a parent.
     */
    fun getParentId() = value.split("/").run { if (size >= 3) get(size - (if (isDocumentPath()) 3 else 2)) else null }

    /**
     * Returns the string representation of the path.
     */
    override fun toString(): String = value

    /**
     * Checks whether this object is equal to another object.
     *
     * @param other The object to compare with this instance for equality.
     * @return `true` if the specified object is equal to this object, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return value == (other as Path).value
    }

    /**
     * Computes the hash code for this Path instance based on its string value.
     *
     * @return The hash code of the string representation of the path.
     */
    override fun hashCode(): Int = value.hashCode()
}