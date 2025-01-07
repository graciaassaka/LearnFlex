package org.example.shared.domain.storage_operations.util

import org.example.shared.domain.constant.Collection

/**
 * Type alias for CollectionConst.
 */
typealias CollectionConst = Collection

/**
 * A utility class for building paths consisting of collections and documents.
 */
class PathBuilder() {
    private val segments = mutableListOf<PathSegment>()

    /**
     * Initializes the PathBuilder with segments from an existing Path.
     * @param existingPath The Path to extend.
     */
    constructor(existingPath: Path) : this() {
        existingPath.value.split("/").forEachIndexed { index, segment ->
            if (index % 2 == 0) {
                segments.add(PathSegment.Collection(Collection.valueOf(segment.uppercase())))
            } else {
                segments.add(PathSegment.Document(segment))
            }
        }
    }

    private sealed class PathSegment {
        abstract fun toPath(): String

        /**
         * Represents a collection segment in the path.
         *
         * @property name The name of the collection.
         */
        data class Collection(val name: CollectionConst) : PathSegment() {
            override fun toPath(): String = name.value
        }

        /**
         * Represents a document segment in the path.
         *
         * @property id The ID of the document.
         */
        data class Document(val id: String) : PathSegment() {
            override fun toPath(): String = id
        }
    }

    /**
     * Adds a collection segment to the path.
     *
     * @param name The name of the collection.
     * @return The current instance of PathBuilder.
     * @throws IllegalStateException If the last segment is not a document (unless starting a new path).
     */
    fun collection(name: CollectionConst): PathBuilder {
        check(segments.isEmpty() || segments.last() is PathSegment.Document) {
            "Cannot add a collection segment without a preceding document segment."
        }
        segments.add(PathSegment.Collection(name))
        return this
    }

    /**
     * Adds a document segment to the path.
     *
     * @param id The ID of the document.
     * @return The current instance of PathBuilder.
     * @throws IllegalStateException If the last segment is not a collection.
     */
    fun document(id: String): PathBuilder {
        check(segments.lastOrNull() is PathSegment.Collection) {
            "Cannot add a document segment without a preceding collection segment."
        }
        segments.add(PathSegment.Document(id))
        return this
    }

    /**
     * Builds the path from the added segments.
     *
     * @return The constructed Path.
     * @throws IllegalStateException If no segments have been added.
     */
    fun build(): Path {
        check(segments.isNotEmpty()) {
            "Cannot build an empty path."
        }
        val pathValue = segments.joinToString("/") { it.toPath() }
        return Path(pathValue)
    }
}
