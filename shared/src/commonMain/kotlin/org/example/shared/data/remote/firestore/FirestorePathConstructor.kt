package org.example.shared.data.remote.firestore

class FirestorePathConstructor {
    private val segments = mutableListOf<PathSegment>()
    private var lastSegmentType: SegmentType? = null

    sealed class PathSegment {
        data class Collection(val name: String) : PathSegment()
        data class Document(val id: String) : PathSegment()
    }

    private enum class SegmentType {
        COLLECTION,
        DOCUMENT
    }

    fun collection(name: String): FirestorePathConstructor {
        require(lastSegmentType != SegmentType.COLLECTION) {
            "Cannot add a collection segment without a preceding document segment."
        }

        segments.add(PathSegment.Collection(name))
        lastSegmentType = SegmentType.COLLECTION
        return this
    }

    fun document(name: String): FirestorePathConstructor {
        require(lastSegmentType == SegmentType.COLLECTION) {
            "Cannot add a document segment without a preceding collection segment."
        }

        segments.add(PathSegment.Document(name))
        lastSegmentType = SegmentType.DOCUMENT
        return this
    }

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