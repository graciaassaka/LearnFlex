package org.example.shared.data.remote.firestore

import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FirestorePathConstructorTest {

    @Test
    fun buildPathWithSingleCollectionAndDocument() {
        val path = FirestorePathConstructor()
            .collection("users")
            .document("user123")
            .build()
        assertEquals("users/user123", path)
    }

    @Test
    fun buildPathWithMultipleCollectionsAndDocuments() {
        val path = FirestorePathConstructor()
            .collection("users")
            .document("user123")
            .collection("posts")
            .document("post456")
            .build()
        assertEquals("users/user123/posts/post456", path)
    }

    @Test
    fun cannotAddCollectionWithoutPrecedingDocument() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FirestorePathConstructor()
                .collection("users")
                .collection("posts")
        }
        assertEquals("Cannot add a collection segment without a preceding document segment.", exception.message)
    }

    @Test
    fun cannotAddDocumentWithoutPrecedingCollection() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FirestorePathConstructor()
                .document("user123")
        }
        assertEquals("Cannot add a document segment without a preceding collection segment.", exception.message)
    }

    @Test
    fun cannotBuildEmptyPath() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FirestorePathConstructor().build()
        }
        assertEquals("Cannot build an empty path.", exception.message)
    }
}