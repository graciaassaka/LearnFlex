package org.example.shared.data.remote.firestore

/**
 * A class to construct Firestore paths by adding collection and document segments.
 */
class FirestorePathConstructor {
    private val segments = mutableListOf<PathSegment>()
    private var lastSegmentType: SegmentType? = null

    /**
     * Represents a segment in the Firestore path.
     */
    sealed class PathSegment {
        /**
         * Represents a collection segment in the Firestore path.
         * @param name The name of the collection.
         */
        data class Collection(val name: String) : PathSegment()

        /**
         * Represents a document segment in the Firestore path.
         * @param id The ID of the document.
         */
        data class Document(val id: String) : PathSegment()
    }

    /**
     * Enum class to represent the type of the last segment added.
     */
    private enum class SegmentType {
        COLLECTION,
        DOCUMENT
    }

    /**
     * Adds a collection segment to the Firestore path.
     * @param name The name of the collection.
     * @return The FirestorePathConstructor instance.
     * @throws IllegalArgumentException if the last segment is already a collection.
     */
    fun collection(name: String): FirestorePathConstructor {
        require(lastSegmentType != SegmentType.COLLECTION) {
            "Cannot add a collection segment without a preceding document segment."
        }

        segments.add(PathSegment.Collection(name))
        lastSegmentType = SegmentType.COLLECTION
        return this
    }

    /**
     * Adds a document segment to the Firestore path.
     * @param name The ID of the document.
     * @return The FirestorePathConstructor instance.
     * @throws IllegalArgumentException if the last segment is not a collection.
     */
    fun document(name: String): FirestorePathConstructor {
        require(lastSegmentType == SegmentType.COLLECTION) {
            "Cannot add a document segment without a preceding collection segment."
        }

        segments.add(PathSegment.Document(name))
        lastSegmentType = SegmentType.DOCUMENT
        return this
    }

    /**
     * Builds the Firestore path from the added segments.
     * @return The constructed Firestore path as a string.
     * @throws IllegalArgumentException if no segments have been added.
     */
    fun build(): String {
        require(segments.isNotEmpty()) {
            "Cannot build an empty path."
        }

        return segments.joinToString("/") { segment ->
            when (segment) {
                is PathSegment.Collection -> segment.name
                is PathSegment.Document   -> segment.id
            }
        }
    }
}