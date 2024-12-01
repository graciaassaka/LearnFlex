package org.example.shared.data.remote.firestore

import org.example.shared.data.remote.util.FirestoreCollection
import org.example.shared.domain.data_source.PathBuilder

class FirestorePathBuilder : PathBuilder {
    override fun buildUserPath() = FirestorePathConstructor()
        .collection(FirestoreCollection.USERS.value)
        .build()
}